package com.cademeupet.cademeupet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PetRegisterActivity extends AppCompatActivity {

    private EditText inputPetName;
    private RadioGroup inputPetSex;
    private String petSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_register);

        inputPetName = (EditText) findViewById(R.id.inputPetName);
        inputPetSex = (RadioGroup) findViewById(R.id.radioGroupSex);
    }


    public void registerUser(View view) {
        if(inputPetName.getText().toString().trim().equals("")) {
            inputPetName.setError("Full name is required");
        } else if (inputPetSex.getCheckedRadioButtonId() == -1) {
            TextView temp = (TextView) findViewById(R.id.textPetSex);
            temp.setError("Please select a option");
        } else {
            // Get selected RadioButton to get the sex
            int selectedId = inputPetSex.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) findViewById(selectedId);

            Intent data = getIntent();
            data.putExtra("PET_NAME", inputPetName.getText().toString());
            data.putExtra("PET_SEX", radioButton.getText());
            setResult(RESULT_OK, data);
            finish();
        }
    }

}
