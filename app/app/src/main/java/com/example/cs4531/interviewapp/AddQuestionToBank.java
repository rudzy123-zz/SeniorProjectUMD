package com.example.cs4531.interviewapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class AddQuestionToBank extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private RestRequests requests;
    private Spinner sourceSpinner;
    private Spinner targetSpinner;
    private CheckBox[] checkBoxes;
    private  final String setDefault = "SELECT A BANK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question_to_bank);

        mDrawerLayout= (DrawerLayout) findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(AddQuestionToBank.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        sourceSpinner = (Spinner)findViewById(R.id.sourceBank);
        targetSpinner = (Spinner)findViewById(R.id.targetBank);

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
     * @author Sam Schmidt
     * @param v
     * Fetches the question bank names, then calls createDropdown to populate the spinner
     * with the bank names.
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
     * @author Sam Schmidt
     * @param arr
     * Takes an array of question bank names, and populates the source and
     * target spinners with the bank names.
     * This renames the "users" bank and sets it to the default selection.
     */
    public void createDropdown(String[] arr){
        swap(arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr);
        int spinnerDefault = adapter.getPosition(setDefault);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr);

        sourceSpinner.setAdapter(adapter);
        sourceSpinner.setSelection(spinnerDefault);

        targetSpinner.setAdapter(adapter2);
        targetSpinner.setSelection(spinnerDefault);
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

    /**
     * @author Sam Schmidt
     * @param v
     * This function retrieves the questions in a selected bank and then calls the createCheckboxes function to create
     * checkboxes for each question in a bank.
     */
    public void getQuestions(View v){
        if(sourceSpinner == null || sourceSpinner.getSelectedItem() == null){
            String notConnected = "Not connected to database!";
            Toast.makeText(AddQuestionToBank.this, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        String targetURL = getString(R.string.serverURL) + "/getFromBank";

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
                            }
                            createCheckBoxes(arr);
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

    /**
     * @author Sam Schmidt
     * @param arr Array of questions
     * This function takes in an array of questions, and creates checkboxes for each question.
     */
    public void createCheckBoxes(String[] arr){
        LinearLayout ll = (LinearLayout) findViewById(R.id.questionList);
        ll.removeAllViews();
        checkBoxes = new CheckBox[arr.length];
        for(int i = 0; i < arr.length; i++){
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button getButton = (Button) findViewById(R.id.getSourceButton);
                    if(checkBox.isChecked()){
                        sourceSpinner.setEnabled(false);
                        getButton.setEnabled(false);
                    }
                    else {
                        sourceSpinner.setEnabled(true);
                        getButton.setEnabled(true);
                    }
                }
            });
            checkBox.setText(arr[i]);
            checkBoxes[i] = checkBox;
            ll.addView(checkBox);
        }
    }

    /**
     * @author Sam Schmidt
     * @param v
     * Sends selected question that are checked to the specified question bank.
     */
    public void sendQuestions(View v){
        if(targetSpinner.getSelectedItem() == null){
            String notConnected = "Not connected to database!";
            Toast.makeText(AddQuestionToBank.this, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        String targetURL = getString(R.string.serverURL) + "/addToOtherBank";
        for (int i = 0; i < checkBoxes.length; i++){
            if(checkBoxes[i].isChecked()){
                JSONObject parameters = new JSONObject();
                try {
                    parameters.put("question",checkBoxes[i].getText().toString());
                    parameters.put("targetCollection", targetSpinner.getSelectedItem().toString());
                    parameters.put("sourceCollection", sourceSpinner.getSelectedItem().toString());
                    parameters.put("username", LogIn.getusername());
                }
                catch(JSONException e){}


                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, targetURL, parameters, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                requests.addToRequestQueue(jsonRequest);
            }
        }
        for(int i = 0; i < checkBoxes.length; i++){
            if(checkBoxes[i].isChecked()){
                checkBoxes[i].setChecked(false);
            }
        }
        sourceSpinner.setSelection(0);
        targetSpinner.setSelection(0);

        LinearLayout ll = (LinearLayout) findViewById(R.id.questionList);
        ll.removeAllViews();

        AlertDialog.Builder alertDialog= new  AlertDialog.Builder(AddQuestionToBank.this);
        alertDialog
                .setTitle("Add Question")
                .setMessage("Successfully added!")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }


}
