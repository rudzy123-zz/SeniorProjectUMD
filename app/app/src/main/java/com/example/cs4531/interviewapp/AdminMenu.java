package com.example.cs4531.interviewapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

public class AdminMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.nav_drawer);
        mToggle = new ActionBarDrawerToggle(AdminMenu.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
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
    /**
     * @author dannyboie
     * @param myview
     * This function switches to the add question activity
     */
    public void switchToAdd(View myview){
        Intent myIntent = new Intent(this, AddQuestion.class);
        startActivity(myIntent);
    }

    public void switchToView(View myview){
        Intent myIntent = new Intent(this, ViewQuestion.class);
        startActivity(myIntent);
    }

    public void switchToBanks(View v){
        Intent intent = new Intent(this, ViewBank.class);
        startActivity(intent);
    }
    public void switchToBanksToDelete(View v){
        Intent intent = new Intent(this, DeleteQuestion.class);
        startActivity(intent);
    }


}


