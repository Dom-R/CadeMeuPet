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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText inputFullname;
    private EditText inputEmail;
    private EditText inputNumber;
    private String userToken;
    private String userFullName;
    private String userEmail;
    private String userTelephone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        userToken = intent.getStringExtra("USER_TOKEN");
        userFullName = intent.getStringExtra("USER_NAME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        userTelephone = intent.getStringExtra("USER_PHONE_NUMBER");

        inputFullname = (EditText) findViewById(R.id.inputFullname);
        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputNumber = (EditText) findViewById(R.id.inputNumber);
        inputNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        inputNumber.setText("+55");

        inputFullname.setText(userFullName);
        inputEmail.setText(userEmail);
        if(userTelephone != null) { inputNumber.setText(userTelephone); }
    }

    public void registerUser(View view) {
        if(inputFullname.getText().toString().trim().equals("")) {
            inputFullname.setError("Por favor insira o nome completo");
        } else if (!isValidEmail(inputEmail.getText().toString())) {
            inputEmail.setError("Email inválido");
        } else if (inputNumber.getText().toString().trim().equals("") || !android.util.Patterns.PHONE.matcher(inputNumber.getText().toString().trim()).matches() || !Pattern.matches("^[+][5]{2}[ ][0-9]{2}[ ][0-9]{4,5}[-][0-9]{4}$", inputNumber.getText().toString().trim())) {
            System.out.println(inputNumber.getText());
            inputNumber.setError("Número de telefone obrigatório no seguinte formato: +55 DDD número");
        } else {
            UserInfo user = new UserInfo(inputFullname.getText().toString(), inputEmail.getText().toString(), inputNumber.getText().toString());

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(userToken).setValue(user);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cademeupet-4379e.firebaseio.com/users/" + userToken + "/NotificationID");
            ref.setValue(FirebaseInstanceId.getInstance().getToken());

            finish();
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}
