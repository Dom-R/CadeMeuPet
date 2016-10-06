package com.cademeupet.cademeupet;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Main Activity";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Loga usuario com Google automaticamente
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            GoogleSignInResult result = pendingResult.get();
            Log.d(TAG, "Login result: " + result.isSuccess());
            if(result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                //String email = acct.getEmail();
                //String name = acct.getDisplayName();
                String token = acct.getId();
                Log.d(TAG, "Google token: " + token);
                findViewById(R.id.login_button).setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "Failed to auth user with Google!");
        }

        // Verificacao se o usuario esta autenticado pelo facebook
        Log.d(TAG, "Facebook user token: " + Profile.getCurrentProfile().getId());
        if (Profile.getCurrentProfile().getId() != null) {
            findViewById(R.id.login_button).setVisibility(View.GONE);
        }

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

    public void insertCode(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insira o código");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                Log.d(TAG, "Inserted Text: " + m_Text);
                showPetActivity(m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                //new AlertDialog.Builder(this).setMessage(contents).setTitle("Result").setIcon(android.R.drawable.ic_dialog_alert).show();
                showPetActivity(contents);
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

    public void showPetActivity(String code) {
        Intent intent = new Intent(this, PetDataActivity.class);
        intent.putExtra("PET_DATA", code);
        startActivity(intent);
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }
}