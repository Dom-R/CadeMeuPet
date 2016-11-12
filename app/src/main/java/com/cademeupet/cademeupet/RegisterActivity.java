package com.cademeupet.cademeupet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText inputFullname;
    private EditText inputEmail;
    private EditText inputNumber;
    private String userToken;
    private String userFullName;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        userToken = intent.getStringExtra("USER_TOKEN");
        userFullName = intent.getStringExtra("USER_NAME");
        userEmail = intent.getStringExtra("USER_EMAIL");

        inputFullname = (EditText) findViewById(R.id.inputFullname);
        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputNumber = (EditText) findViewById(R.id.inputNumber);
        inputNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        inputNumber.setText("+55");

        inputFullname.setText(userFullName);
        inputEmail.setText(userEmail);

    }


    public void registerUser(View view) {
        if(inputFullname.getText().toString().trim().equals("")) {
            inputFullname.setError("Full name is required");
        } else if (!isValidEmail(inputEmail.getText().toString())) {
            inputEmail.setError("Invalid Email");
        } else if (inputNumber.getText().toString().trim().equals("") || !android.util.Patterns.PHONE.matcher(inputNumber.getText().toString().trim()).matches() || !Pattern.matches("^[+][5]{2}[ ][0-9]{2}[ ][0-9]{4,5}[-][0-9]{4}$", inputNumber.getText().toString().trim())) {
            System.out.println(inputNumber.getText());
            inputNumber.setError("Phone number is required in the following style: +55 CityCode TelephoneNumber");
        } else {
            UserInfo user = new UserInfo(inputFullname.getText().toString(), inputEmail.getText().toString(), inputNumber.getText().toString());

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(userToken).setValue(user);

            finish();
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
