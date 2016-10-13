package com.cademeupet.cademeupet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;

public class PetVaultActivity extends AppCompatActivity {

    private static final String TAG = "PetVault Activity";
    private String userToken;
    private String petName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_vault);

        Intent intent = getIntent();
        userToken = intent.getStringExtra("USER_TOKEN");

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.add_pet);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewPet();
            }
        });

        // Get registered pets
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/" + userToken + "/pets");

        final PetVaultActivity thisClass = this;

        final View.OnClickListener buttonClick = new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println(v.getTag());

                Intent intent = new Intent(thisClass, PetDataActivity.class);
                intent.putExtra("PET_DATA", (String) v.getTag());
                startActivity(intent);
            }
        };

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String name = (String) dataSnapshot.getValue();
                //System.out.println(name);

                LinearLayout ll = (LinearLayout) findViewById(R.id.hsvLinearLayout);
                ll.removeAllViews();

                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    PetInfo pet = messageSnapshot.getValue(PetInfo.class);

                    Button btn = new Button(thisClass);
                    btn.setText(pet.getName());
                    btn.setOnClickListener(buttonClick);
                    ll.addView(btn);
                    btn.setTag(messageSnapshot.getKey());
                }

                //String abc = (String) dataSnapshot.getValue();
                //System.out.println(abc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    public void addNewPet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert Pet Name:");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                petName = input.getText().toString();
                registerNewPet();
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


    public void registerNewPet() {
        /*
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/" + userToken);

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String name = (String) dataSnapshot.getValue();
                //System.out.println(name);
                UserInfo user = dataSnapshot.getValue(UserInfo.class);
                System.out.println(user.getName());

                // Add o pet
                PetInfo pet = new PetInfo(petName);

                Random r = new Random(System.currentTimeMillis() / 1000);
                String petID = Integer.toString(r.nextInt(32768));

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("pets").child(petID).setValue(pet);

                user.addPet(petID);

                mDatabase.child("users").child("109566306045830493130").setValue(user);

                System.out.println("Pet inserted!");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });*/

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        PetInfo pet = new PetInfo(petName);

        SecureRandom random = new SecureRandom();

        String petID = new BigInteger(130, random).toString(32);

        petID = petID.substring(0, Math.min(petID.length(), 6));

        mDatabase.child("pets").child(petID).setValue(pet);

        mDatabase.child("users").child(userToken).child("pets").child(petID).setValue(pet);

        System.out.println("Pet inserted!");

    }
}
