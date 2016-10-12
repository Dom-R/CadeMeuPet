package com.cademeupet.cademeupet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.facebook.FacebookSdk;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleLogin";
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mFacebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_sign_in_button);
        loginButton.setReadPermissions("public_profile");
        mFacebookCallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker mProfileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            // profile2 is the new profile
                            Log.v("facebook - profile", profile2.getFirstName());
                            mProfileTracker.stopTracking();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.

                    Intent data = new Intent();
                    data.putExtra("USER_ID", Profile.getCurrentProfile().getId());

                    setResult(RESULT_OK, data);
                    finish();
                }
                else {
                    Profile profile = Profile.getCurrentProfile();
                    Log.v("facebook - profile", profile.getFirstName());
                }
            }

            @Override
            public void onCancel() {
                Log.v("facebook - onCancel", "cancelled");
            }

            @Override
            public void onError(FacebookException e) {
                Log.v("facebook - onError", e.getMessage());
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    public void onGoogleLogin() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            if (mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());

        if(result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String email = acct.getEmail();
            String name = acct.getDisplayName();
            String token = acct.getId();
            Log.d(TAG, "Login result: " + email + " " + name + " " + token);

            Intent data = new Intent();
            data.putExtra("USER_ID", token);
            setResult(RESULT_OK, data);
            finish();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                onGoogleLogin();
                break;
        }
    }

}