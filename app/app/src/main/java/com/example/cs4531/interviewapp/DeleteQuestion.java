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
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.util.Hashtable;
import java.util.Map;


public class DeleteQuestion extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private RestRequests requests;
    private Spinner sourceSpinner;
    private CheckBox[] checkBoxes;
    boolean good = true;
    private  final String setDefault = "SELECT A BANK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_question);

        sourceSpinner = findViewById(R.id.deleteBank4);

        requests = RestRequests.getInstance(getApplicationContext());
        getBanks(sourceSpinner);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @author musik013 & wil00543 04.13.2019
     * @param v
     * getQuestion lets the admin access all the questions in the database preeminently.
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
     * @author  wil00543 04.13.2019
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
     * @author musik013 & wil00543 04.13.2019
     * @param arr
     * getQuestion lets the admin access all the questions in the database preeminently.
     */

    public void createCheckBoxes(String[] arr){
        LinearLayout questionList = findViewById(R.id.questionList4);
        questionList.removeAllViews();
        checkBoxes = new CheckBox[arr.length];
        for(int i = 0; i < arr.length; i++){
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(arr[i]);
            checkBoxes[i] = checkBox;
            questionList.addView(checkBox);
        }
    }
    /**
     * @author musik013 & wil00543 04.13.2019
     * @param v
     * getQuestion lets the admin access all the questions in the database preeminently.
     */
    public void getQuestions(View v){
        if(sourceSpinner == null || sourceSpinner.getSelectedItem() == null){
            String notConnected = "Not connected to database!";
            Toast.makeText(DeleteQuestion.this, notConnected, Toast.LENGTH_SHORT).show();
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
                                System.out.println("Question: " + question);
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
     * @author musik013  04.13.2019
     * @param v
     * getQuestion lets the admin access all the questions in the database preeminently.
     */
    public void deleteQuestions(View v){
        if(sourceSpinner.getSelectedItem() == null){
            String notConnected = "Not connected to database!";
            Toast.makeText(DeleteQuestion.this, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question (s)?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


        String targetURL = getString(R.string.serverURL) + "/deleteQuestion";
        for (int i = 0; i < checkBoxes.length; i++){
            if(checkBoxes[i].isChecked()){
                JSONObject parameters = new JSONObject();
                try {
                    parameters.put("question",checkBoxes[i].getText().toString());
                    parameters.put("bank", sourceSpinner.getSelectedItem().toString());
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
                        good = false;
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
        alert(good);
        LinearLayout questionList =  findViewById(R.id.questionList4);
        questionList.removeAllViews();
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    /**
     * @author musik013 & wil00543  04.13.2019
     * @param v
     * getQuestion lets the admin access all the questions in the database preeminently.
     */

    private void alert(boolean good)
    {
        if(good)
        {
            AlertDialog.Builder alertDialog= new  AlertDialog.Builder(DeleteQuestion.this);
            alertDialog
                    .setTitle("Delete Questions")
                    .setMessage("Successfully Deleted!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        else
        {
            AlertDialog.Builder alertDialog= new  AlertDialog.Builder(DeleteQuestion.this);
            alertDialog
                    .setTitle("Delete Questions")
                    .setMessage("Failed to Delete!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }

    }

}
