package com.cademeupet.cademeupet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }


    public void registerUser(View view) {
        Intent intent = getIntent();
        String token = intent.getStringExtra("TOKEN");

        EditText fullname  = (EditText) findViewById(R.id.fullname);

        UserInfo user = new UserInfo(fullname.getText().toString());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(token).setValue(user);

        finish();
    }

}
