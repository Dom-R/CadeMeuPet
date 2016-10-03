package com.cademeupet.cademeupet;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class PetDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_data);

        Intent intent = getIntent();
        String petId = intent.getStringExtra("PET_DATA");
        //new AlertDialog.Builder(this).setMessage(content).setTitle("Result").setIcon(android.R.drawable.ic_dialog_alert).show();
        TextView textView = new TextView(this);
        textView.setText(petId);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_pet_data);
        layout.addView(textView);
    }
}
