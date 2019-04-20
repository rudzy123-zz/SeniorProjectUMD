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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AdminVerification extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    public RestRequests requests; //our RestRequests class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verification);
        requests = RestRequests.getInstance(getApplicationContext());
        mDrawerLayout= (DrawerLayout) findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(AdminVerification.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    /**
     * @author Danny Boie
     * @param v
     *
     *  Function to verify if user is an administrator. Makes a call to
     *  the server to retrieve access level of user to then decide
     *  whether to advance to amdin activity or deliver a warning error.
     */
    public void verify(final View v) {
        //Variables
        String url = getString(R.string.serverURL) + "/verify";
        JSONObject parameters = new JSONObject();
        final String correctString = "{\"access\":\"admin\"}";
        // Gets username from LogIn Activity
        try {
            parameters.put("username", LogIn.getusername());
        }
        catch(JSONException e){}
        //Send to server
            JsonObjectRequest jsonRequest =
                    new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            TextView res = findViewById(R.id.textView6);
                            res.setVisibility(View.INVISIBLE);
                            res.setText(response.toString());
                            String compareString = response.toString();
                            if (correctString.equals(compareString)) {
                                launchAdminMenu(v);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            TextView res = findViewById(R.id.textView6);
                            res.setText("Not Authorized");
                            res.setVisibility(View.VISIBLE);
                        }
                    });


        requests.addToRequestQueue(jsonRequest);
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

    public void launchAdminMenu(View v){
        Intent intent = new Intent(this, AdminMenu.class);
        startActivity(intent);
    }
}

