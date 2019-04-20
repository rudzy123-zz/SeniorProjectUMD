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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.util.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeleteBank extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private CheckBox[] checkBoxes;
    public RestRequests requests;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_bank);

        requests = RestRequests.getInstance(getApplicationContext());
        mDrawerLayout= (DrawerLayout) findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(DeleteBank.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button button = (Button)findViewById(R.id.deleteButton);
        getBanks(button);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @param item This function takes a boolean value to transition between different activities.
     *             It holds all the logic necessary for the navigation side bar.
     * @author smatthys
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_recordVideo) {
            Intent intent = new Intent(this, RecordVideoActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_flashcards) {
            Intent intent = new Intent(this, FlashcardsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_resources) {
            Intent intent = new Intent(this, ResourcesActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_myAccount) {
            Intent intent = new Intent(this, LogIn.class);
            startActivity(intent);
        }
        if (id == R.id.nav_admin) {
            Intent intent = new Intent(this, AdminMenu.class);
            startActivity(intent);
        }
        return false;
    }

    public void getBanks(View v){
        String targetURL = getString(R.string.serverURL) + "/listBanks";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, targetURL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String[] banks = new String[response.length()];
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject bank = response.getJSONObject(i);
                                banks[i] = bank.getString("name");
                            }
                            makeCheckBoxes(banks);
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
    public void makeCheckBoxes(String[] arr){
        LinearLayout ll = (LinearLayout) findViewById(R.id.checkboxes);
        ll.removeAllViews();
        checkBoxes = new CheckBox[arr.length];
        for(int i = 0; i < arr.length; i++){
            if(arr[i].equals("flashQuestions") || arr[i].equals("users")) {
                continue;
            }
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(arr[i]);
                checkBoxes[i] = checkBox;
                ll.addView(checkBox);
        }
    }

    public void onClick(final View v){
        if(checkBoxes == null || checkBoxes.length == 0){
            String notConnected = "Not connected to database!";
            Toast.makeText(DeleteBank.this, notConnected, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete Bank")
                .setMessage("Are you sure you want to delete this bank(s)?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String targetURL = getString(R.string.serverURL) + "/deleteBank";
                        for(int i = 0; i < checkBoxes.length; i++){
                            if(checkBoxes[i] == null)
                                continue;
                            else if(checkBoxes[i].isChecked()){
                                JSONObject parameters = new JSONObject();
                                try {
                                    parameters.put("bankName",checkBoxes[i].getText());
                                    parameters.put("username", LogIn.getusername());
                                }
                                catch(JSONException e){}
                                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, targetURL, parameters, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {}
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });
                                requests.addToRequestQueue(jsonRequest);
                                checkBoxes = ArrayUtils.removeAll(checkBoxes);
                                try{
                                    Thread.sleep(200);
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                                getBanks(v);
                            }
                        }
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
}
