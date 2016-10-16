package com.cademeupet.cademeupet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class PetRegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_RESULT = 2845;
    private EditText inputPetName;
    private RadioGroup inputPetSex;
    private String petID;
    private Uri file;
    private StorageReference storageRef;
    private Spinner spinnerSpecie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_register);

        inputPetName = (EditText) findViewById(R.id.inputPetName);
        inputPetSex = (RadioGroup) findViewById(R.id.radioGroupSex);
        spinnerSpecie = (Spinner) findViewById(R.id.spinnerSpecie);

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

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

                    // Setting pet name
                    EditText textPetName = (EditText) findViewById(R.id.inputPetName);
                    textPetName.setText(pet.getName());

                    // Setting pet sex
                    RadioButton radioButton;
                    if(pet.getSex().equals("Male"))
                        radioButton = (RadioButton) findViewById(R.id.maleSex);
                    else
                        radioButton = (RadioButton) findViewById(R.id.femaleSex);
                    radioButton.setChecked(true);

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(currentClass, R.array.array_species, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSpecie.setAdapter(adapter);
                    if (!pet.getSpecie().equals(null)) {
                        int spinnerPosition = adapter.getPosition(pet.getSpecie());
                        spinnerSpecie.setSelection(spinnerPosition);
                    }

                    // Recover Image
                    StorageReference petImage = storageRef.child("images/" + petID);

                    final long ONE_MEGABYTE = 16384 * 16384;
                    petImage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for "images/island.jpg" is returns, use this as needed
                            ImageButton btn = (ImageButton)findViewById(R.id.imageButton);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            btn.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 512, 512, false));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
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
            if(file != null)
                data.putExtra("PET_IMAGE", file.toString());
            data.putExtra("PET_SPECIE", spinnerSpecie.getSelectedItem().toString());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public void uploadPetImage(View view) {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_RESULT && resultCode == RESULT_OK && data != null && data.getData() != null) {

            file = data.getData();

            try {
                ImageButton btn = (ImageButton)findViewById(R.id.imageButton);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
                btn.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 512, 512, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
