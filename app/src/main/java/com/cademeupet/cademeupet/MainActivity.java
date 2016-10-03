package com.cademeupet.cademeupet;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void readQRCode(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                //new AlertDialog.Builder(this).setMessage(contents).setTitle("Result").setIcon(android.R.drawable.ic_dialog_alert).show();
                Intent intent = new Intent(this, PetDataActivity.class);
                intent.putExtra("PET_DATA", contents);
                startActivity(intent);
            }
            /*
            // Evento ação cancelada
            if(resultCode == RESULT_CANCELED){
                new AlertDialog.Builder(this)
                        .setTitle("Action canceled")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            */
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}