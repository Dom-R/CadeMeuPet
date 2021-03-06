package com.cademeupet.cademeupet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull; //COMENTAR PARA FAZER TESTE
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Main Activity";
    private static final int LOGIN_RESULT = 3627;
    private static final int QRCODE_RESULT = 3849;
    private GoogleApiClient mGoogleApiClient;
    private String userToken;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Aguarde enquanto tentamos autenticar sua conta.");
        loadingDialog.setCancelable(false);
        loadingDialog.setInverseBackgroundForced(false);
        loadingDialog.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hide();
            }
        }, 5000);

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
            Log.d(TAG, "Resultado do login Google: " + result.isSuccess());
            if(result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                //String email = acct.getEmail();
                //String name = acct.getDisplayName();
                String token = acct.getId();
                Log.d(TAG, "Google token: " + token);
                createUserIfNotExist(acct.getId(), acct.getDisplayName(), acct.getEmail());
            }
        } else {
            Log.d(TAG, "Falha ao autenticar conta Google!");
            loadingDialog.hide();
        }

        // Verificacao se o usuario esta autenticado pelo facebook
        //Log.d(TAG, "Facebook user token: " + Profile.getCurrentProfile().getId());
        if (isFacebookLoggedIn()) {
            createUserIfNotExist(Profile.getCurrentProfile().getId(), Profile.getCurrentProfile().getName(), "");
        } else {
            Log.d(TAG, "Usuário não logado com Facebook!");
            loadingDialog.hide();
        }
    }

    public void readQRCode(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            startActivityForResult(intent, QRCODE_RESULT);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        }
    }

    public boolean isFacebookLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public void insertCode(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insira um código");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Ir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                Log.d(TAG, "Texto inserido: " + m_Text);
                showPetActivity(m_Text);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
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
        if (requestCode == QRCODE_RESULT) {
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

        // Managed to Log in
        if(requestCode == LOGIN_RESULT) {
            loadingDialog.hide();
            if (resultCode == RESULT_OK) {
                // Create User
                System.out.println(data.getStringExtra("USER_ID"));
                createUserIfNotExist(data.getStringExtra("USER_ID"), data.getStringExtra("USER_NAME"), data.getStringExtra("USER_EMAIL"));

            }
        }
    }

    private void changeLoginButtonToProfile() {
        findViewById(R.id.buttonLoginORProfile).setOnClickListener(new View.OnClickListener()
           {
               public void onClick(View v)
               {
                   openPetsVault();
               }
           }
        );
        TextView tv = (TextView) findViewById(R.id.buttonLoginORProfile);
        tv.setText("Perfil");
    }

    public void databaseTest(View view) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://lasid.sor.ufscar.br/twittersearch/country/sendnotification.php?id=" + FirebaseInstanceId.getInstance().getToken() + "&title=CadeMeuPet!&body=Dados%20de%20Ahri%20foram%20acessados!", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                System.out.println("Sucesso");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                System.out.println("Falha");
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

        /*Intent intent = new Intent(this, PetLocationActivity.class);
        intent.putExtra("LAT", "-23.569574");
        intent.putExtra("LONG", "-46.730172");
        startActivity(intent);*/
    }

    public void createUserIfNotExist(final String token, final String name, final String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cademeupet-4379e.firebaseio.com/users/" + token);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    System.out.println("User Exist in db");

                    // Get Firebase Token to send Notification and update it on the profile
                    System.out.println("Firebase Instance ID: " + FirebaseInstanceId.getInstance().getToken());

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cademeupet-4379e.firebaseio.com/users/" + token + "/NotificationID");
                    ref.setValue(FirebaseInstanceId.getInstance().getToken());

                    userToken = token;

                    changeLoginButtonToProfile();
                }
                else {
                    // TODO: handle the case where the data does not yet exist
                    System.out.println("Does not exist in db");

                    startRegistration(token, name, email);

                    userToken = token;

                    changeLoginButtonToProfile();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void startRegistration(String token, String name, String email) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("USER_TOKEN", token);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
    }

    public void showPetActivity(final String code) {

        final MainActivity currentClass = this;

        // Validate if pet exists
        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cademeupet-4379e.firebaseio.com/pets/" + code);
        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    System.out.println("Pet exist in db");

                    Intent intent = new Intent(currentClass, PetDataActivity.class);
                    intent.putExtra("PET_DATA", code);
                    startActivity(intent);
                }
                else {
                    // TODO: handle the case where the data does not yet exist
                    System.out.println("Pet does not exist in db");

                    new AlertDialog.Builder(currentClass)
                            .setTitle("Código inválido!")
                            .setMessage("Código inválido! Por favor tente novamente!")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .setNegativeButton("Sair", null)
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void login(View view) {
        loadingDialog.show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_RESULT);
    }

    public void openPetsVault() {
        Intent intent = new Intent(this, PetVaultActivity.class);
        intent.putExtra("USER_TOKEN", userToken);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }
}