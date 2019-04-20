package com.example.cs4531.interviewapp;


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
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;


public class ViewQuestion extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public RestRequests requests;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;


    private Spinner sourceSpinner;
    private  final String setDefault = "SELECT A BANK";
    private TextView listQuestions;

    /**
     * @author musik013 04.13.2019
     * @param savedInstanceState
     * getQuestion lets the admin access all the questions in the database preeminently.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question);

        mDrawerLayout=  findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(ViewQuestion.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView= findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        sourceSpinner = findViewById(R.id.viewBankQns);
        listQuestions =  findViewById(R.id.questionInBank);
        listQuestions.setMovementMethod(new ScrollingMovementMethod());
        listQuestions.setText("");
        requests = RestRequests.getInstance(getApplicationContext());
        getBanks(sourceSpinner);
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
            Intent intent = new Intent(this, AdminMenu.class);
            startActivity(intent);
        }
        return false;
    }


    /**
     * @author musik013 & wil00543 & schm4400 04.13.2019
     * @param v
     * getQuestion lets the admin access all the questions in the database preeminently.
     */
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
    /**
     * @author  musik013 & schm4400 04.13.2019
     * @param arr
     * getQuestion lets the admin access all the questions in the database preeminently.
     */

    public void createDropdown(String[] arr){
        swap(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr);
        int spinnerDefault = adapter.getPosition(setDefault);

        sourceSpinner.setAdapter(adapter);
        sourceSpinner.setSelection(spinnerDefault);

    }
    /**
     * @author  musik013 04.13.2019
     * @param arr
     * getQuestion lets the admin access all the questions in the database preeminently.
     */
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

    /**
     * @author musik013  04.13.2019
     * @param v
     * getQuestion lets the admin access all the questions in the database preeminently.
     */
    public void getQuestionsToBeViewed(View v){
        if(sourceSpinner == null || sourceSpinner.getSelectedItem() == null){
            String notConnected = "Not connected to database!";
            Toast.makeText(ViewQuestion.this, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        String targetURL = getString(R.string.serverURL) + "/getFromBank";
        listQuestions.setText("");
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("bankName", sourceSpinner.getSelectedItem().toString());
        }catch (JSONException e){e.printStackTrace();}
        StringRequest postRequest = new StringRequest(Request.Method.POST,targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Response: " + response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            String[] arr = new String[jsonArray.length()];
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject bank = jsonArray.getJSONObject(i);

                                String question = bank.getString("question");
                                arr[i] = question;
                                listQuestions.append(question);
                                listQuestions.append("\n\n");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.toString());
                        listQuestions.setText(error.toString());
                    }
                }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new Hashtable<String, String>();
                params.put("bankName", sourceSpinner.getSelectedItem().toString());
                return params;
            }
        };
        requests.addToRequestQueue(postRequest);
    }

}
