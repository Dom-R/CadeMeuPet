package com.cademeupet.cademeupet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PetRegisterActivity extends AppCompatActivity {

    private EditText inputPetName;
    private RadioGroup inputPetSex;
    private String petID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_register);

        inputPetName = (EditText) findViewById(R.id.inputPetName);
        inputPetSex = (RadioGroup) findViewById(R.id.radioGroupSex);

        Intent intent = getIntent();
        petID = intent.getStringExtra("PET_ID");
        if(petID != null) {
            TextView button = (TextView) findViewById(R.id.register);
            button.setText("Update");

            final PetRegisterActivity currentClass = this;

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("pets/" + petID);
            // Attach a listener to read the data at our posts reference
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PetInfo pet = dataSnapshot.getValue(PetInfo.class);
                    System.out.println(pet.getName());

                    EditText textPetName = (EditText) findViewById(R.id.inputPetName);
                    textPetName.setText(pet.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        }
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
            if(petID != null)
                data.putExtra("PET_ID", petID);
            setResult(RESULT_OK, data);
            finish();
        }
    }

}
