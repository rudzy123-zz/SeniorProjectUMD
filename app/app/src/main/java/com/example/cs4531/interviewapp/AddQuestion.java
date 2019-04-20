package com.example.cs4531.interviewapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddQuestion extends AppCompatActivity {

    public RestRequests requests; //our RestRequests class
    private Spinner sourceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        requests = RestRequests.getInstance(getApplicationContext());
        TextView res = findViewById(R.id.Response);
        res.setVisibility(View.INVISIBLE);
        TextView ques = findViewById(R.id.EditQuestion); //Question view
        ques.setText("");
        TextView ans = findViewById(R.id.EditAnswer);
        ans.setText("");
        sourceSpinner = (Spinner)findViewById(R.id.sourceBank2);

        getBanks(sourceSpinner);
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
                                arr[i] = name;
                                if(name.equals("users"))
                                    continue;
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr);
        sourceSpinner.setAdapter(adapter);
    }


    public void addQuestion(View v)
    {
        String url = getString(R.string.serverURL) + "/makeFlash";

        JSONObject parameters = new JSONObject();
        TextView ques = findViewById(R.id.EditQuestion); //Question view
        TextView ans = findViewById(R.id.EditAnswer);
        try {
            parameters.put("question",ques.getText());
            parameters.put("answer", ans.getText());
            parameters.put("username", LogIn.getusername());
            parameters.put("bank", sourceSpinner.getSelectedItem().toString());

        }
        catch(JSONException e){}


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                TextView res = findViewById(R.id.Response);
                res.setText("Your question has been added to the bank");
                res.setVisibility(View.VISIBLE);
                TextView ques = findViewById(R.id.EditQuestion); //Question view
                ques.setText("");
                TextView ans = findViewById(R.id.EditAnswer);
                ans.setText("");

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                TextView res = findViewById(R.id.Response);
                res.setText("Fill the boxes or Not Authorized");
                res.setVisibility(View.VISIBLE);
            }
        });

        requests.addToRequestQueue(jsonRequest);
    }


}





