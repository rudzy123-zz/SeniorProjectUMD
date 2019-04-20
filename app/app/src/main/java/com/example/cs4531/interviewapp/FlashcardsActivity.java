package com.example.cs4531.interviewapp;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.cs4531.interviewapp.LogIn;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Once database is up and running, we can set functions to the getQuestion
 * button to retrieve a sample question from the database, and then put it in
 * the textview. The getAnswer should have the same functionality.
 */
public class FlashcardsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    public RestRequests requests; //our RestRequests class
    public String answerString; //the answer response
    private String setDefault;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(FlashcardsActivity.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView tv = (TextView)findViewById(R.id.qAView); //Question view
        TextView answerView = (TextView)findViewById(R.id.answerView);
        requests = RestRequests.getInstance(getApplicationContext());

        setDefault = getString(R.string.setDefault);
        spinner = (Spinner)findViewById(R.id.bankSpinner);
        getBanks(spinner);

        tv.setText("");
        final Button answerButton = (Button)findViewById(R.id.getAnswer);

        //getQuestion(tv);


    }

    /**
     * @author smatthys
     * @param item
     * This function allows the menu toggle button and other menu buttons
     * properly function when clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * @author smatthys
     * @param item
     * This function takes a boolean value to transition between different activities.
     * It holds all the logic necessary for the navigation side bar.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_recordVideo){
            Intent intent = new Intent(this, RecordVideoActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_flashcards){
            Intent intent = new Intent(this, FlashcardsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_resources){
            Intent intent = new Intent(this, ResourcesActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_myAccount){
            Intent intent = new Intent(this, LogIn.class);
            startActivity(intent);
        }
        if (id == R.id.nav_admin) {
            Intent intent = new Intent(this, AdminVerification.class);
            startActivity(intent);
        }
        return false;
    }

    /**
     * getQuestion changes the name of the buttons, textViews, and updates the view with a new flashcard question.
     * @author Jaron
     * @param v the view
     */
    public void getQuestion(View v)
    {
        if(spinner == null || spinner.getSelectedItem() == null){
            String notConnected = "Not connected to database!";
            Toast.makeText(FlashcardsActivity.this, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        if(spinner.getSelectedItem().toString().equals(setDefault)) {
            String validBank = "Please select a valid bank!";
            Toast.makeText(FlashcardsActivity.this, validBank, Toast.LENGTH_SHORT).show();
            return;
        }
        String targetURL = getString(R.string.serverURL) + "/getFlash";
        JSONObject parameters = new JSONObject();
        try{
            parameters.put("bankName", spinner.getSelectedItem().toString());
        } catch (JSONException e){
            e.printStackTrace();
        }

        final TextView tv = (TextView)findViewById(R.id.qAView);
        TextView answerView = (TextView) findViewById(R.id.answerView);
        answerView.setText(""); //Resets the answer field to default on click
        Button getQuestionButton = (Button)findViewById(R.id.get_question);
        getQuestionButton.setText(R.string.new_Question);
        Button answerButton = (Button)findViewById(R.id.getAnswer);
            if(answerButton.getText().toString() == getString(R.string.hide_Answer)) {
                hideAnswer(v);
            }

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, targetURL, parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                        public void onResponse (JSONObject response){
                        try {
                       // JSONObject flashcard = response.getJSONObject();
                        answerString = response.getString("answer");

//                        Log.d("GET", response.toString());
                        tv.setText(response.getString("question"));

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                        }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.toString());
                        tv.setText(error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("bankName", spinner.getSelectedItem().toString());
                return params;
            }
        };
        requests.addToRequestQueue(sr);
    }

    public void showAnswer(View v)
    {
        TextView answerView = (TextView)findViewById(R.id.answerView);
        answerView.setText(answerString);

        Button answerButton = (Button)findViewById(R.id.getAnswer);
        answerButton.setText(R.string.hide_Answer); //change button to hide Answer
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                hideAnswer(v);
            }
        });
    }

    public void hideAnswer(View v)
    {
        TextView answerView = (TextView)findViewById(R.id.answerView);
        answerView.setText("");

        Button answerButton = (Button)findViewById(R.id.getAnswer);
        answerButton.setText(R.string.Get_Answer); //change button to hide Answer
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                showAnswer(v);
            }
        });
    }

    public void getBanks(View v){
        String targetURL = getString(R.string.serverURL) + "/listBanks";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, targetURL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String[] arr = new String[response.length()];
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject bank = response.getJSONObject(i);

                                String name = bank.getString("name");

                                if(name.equals("users")) {
                                    arr[i] = setDefault;
                                    continue;
                                }
                                arr[i] = name;
                            }
                            createDropdown(arr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.toString());
                    }
                });

        requests.addToRequestQueue(jsonArrayRequest);
    }

    public void createDropdown(String[] arr){
        swap(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr);
        int spinnerDefault = adapter.getPosition(setDefault);

        spinner.setAdapter(adapter);
        spinner.setSelection(spinnerDefault);
    }

    private void swap(String[] arr){
        String temp = arr[0];
        for(int i = 0; i < arr.length; i++){
            if(arr[i].equals(setDefault)){
                arr[0] = setDefault;
                arr[i] = temp;
                return;
            }
        }
    }

}
