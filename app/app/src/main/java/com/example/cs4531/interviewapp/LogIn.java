package com.example.cs4531.interviewapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class LogIn extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static String username;
    private SignInButton signIn;
    private Button signOut;
    private TextView accountInfo;
    GoogleSignInOptions signInOptions;
    private static final int REQ_CODE = 9001;
    GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        signIn = (SignInButton)findViewById(R.id.googleLogIn);
        signIn.setOnClickListener(this);
        signOut = (Button)findViewById(R.id.googleLogOut);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
        accountInfo = (TextView)findViewById(R.id.accountInfo);
        accountInfo.setText("Tech Prep");

    }

    @Override
    public void onClick(View view) {

        //either logs in or logs out depending on the id
        if(view.getId() == R.id.googleLogIn)
        {
            signIn();
        }

        else {
            signOut(view);
        }
    }

    //signs user in
    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    //signs user out
    protected void signOut(View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Toast.makeText(getApplicationContext()," Logged out", Toast.LENGTH_SHORT).show();
                updateUI(false);
            }
        });
    }

    //signOut function that doesn't need a view parameter
    protected void logout() {
        Auth.GoogleSignInApi.signOut(googleApiClient);
        updateUI(false);

    }

    public void updateUI(boolean isLogin) {
        if(isLogin) {
            //brings app to main activity
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
        }
        else{
            //stays at login screen
            Intent logInIntent = new Intent(this, LogIn.class);
            startActivity(logInIntent);

        }

    }

    private void handleResult(GoogleSignInResult result){
        if(result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String email = account.getEmail();
            username = account.getEmail();

            //if not UMD email, display toast, updateUI to stay on login screen and logout
            if(email.endsWith("umn.edu")) {
                updateUI(true);
            }
            else {
                Toast.makeText(LogIn.this,"Please use a d.umn.edu email",
                        Toast.LENGTH_LONG).show();
                updateUI(false);
                logout();
            }
        }
        else {
            updateUI(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);

        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static String getusername(){
        String user = "";
        for (int i = 0; i < username.length(); i++){
            if(username.charAt(i) == '@')
                return user;
            user += username.charAt(i);
        }
        return username;
    }


}
