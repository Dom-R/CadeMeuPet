package com.cademeupet.cademeupet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import cz.msebera.android.httpclient.Header;

public class PetVaultActivity extends AppCompatActivity {

    private static final String TAG = "PetVault Activity";
    private String userToken;
    private int PET_REGISTRATION_RESULT = 6948;
    private int PET_DATACHANGE_RESULT = 9482;
    private StorageReference  storageRef;
    private UploadTask uploadTask;

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
        DatabaseReference ref = database.getReference("pets");

        final PetVaultActivity thisClass = this;

        final View.OnClickListener buttonClick = new View.OnClickListener() {
            public void onClick(View v) {
            System.out.println(v.getTag());

            Intent intent = new Intent(thisClass, PetRegisterActivity.class);
            intent.putExtra("PET_ID", (String) v.getTag());
            startActivityForResult(intent, PET_DATACHANGE_RESULT);
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
//                ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    PetInfo pet = messageSnapshot.getValue(PetInfo.class);

                    // Pet belongs to the user
                    if(pet.getUserID().equals(userToken)) {
                        Button btn = new Button(thisClass);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 0, 20);
                        btn.setLayoutParams(params);
                        btn.setBackgroundResource(R.drawable.mainbuttonshape);
                        btn.setTextColor(Color.WHITE);
                        btn.setText(pet.getName());
                        btn.setOnClickListener(buttonClick);
                        ll.addView(btn);
                        btn.setTag(messageSnapshot.getKey());
                    }
                }

                //String abc = (String) dataSnapshot.getValue();
                //System.out.println(abc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void addNewPet() {
        Intent intent = new Intent(this, PetRegisterActivity.class);
        startActivityForResult(intent, PET_REGISTRATION_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Managed to Log in
        if(requestCode == PET_REGISTRATION_RESULT) {
            if (resultCode == RESULT_OK) {
                // Create User
                Uri uri = null;
                if(data.getStringExtra("PET_IMAGE") != null)
                    uri = Uri.parse(data.getStringExtra("PET_IMAGE"));
                registerNewPet(uri, data.getStringExtra("PET_NAME"), data.getStringExtra("PET_SEX"), data.getStringExtra("PET_SPECIE"));
            }
        }
        if(requestCode == PET_DATACHANGE_RESULT) {
            if(resultCode == RESULT_OK) {
                Uri uri = null;
                if(data.getStringExtra("PET_IMAGE") != null)
                    uri = Uri.parse(data.getStringExtra("PET_IMAGE"));
                updatePetInfo(uri, data.getStringExtra("PET_ID"), data.getStringExtra("PET_NAME"), data.getStringExtra("PET_SEX"), data.getStringExtra("PET_SPECIE"));
            }
        }
    }

    private void updatePetInfo(Uri file, String petID, String petName, String petSex, String petSpecie) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        PetInfo pet = new PetInfo(userToken, petName, petSex, petSpecie);

        mDatabase.child("pets").child(petID).setValue(pet);

        //mDatabase.child("users").child(userToken).child("pets").child(petID).setValue(pet);

        // Upload de imagem
        if(file != null) {
            uploadImagem(file, petID);
        }

        System.out.println("Pet inserted!");
    }

    public void registerNewPet(Uri file, final String petName, String petSex, String petSpecie) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        final PetInfo pet = new PetInfo(userToken, petName, petSex, petSpecie);

        // Gera cod unico do animal
        SecureRandom random = new SecureRandom();
        String petID = new BigInteger(130, random).toString(32);
        petID = petID.substring(0, Math.min(petID.length(), 6)).toUpperCase();

        // Insere pet no firebase
        mDatabase.child("pets").child(petID).setValue(pet);

        //mDatabase.child("users").child(userToken).child("pets").child(petID).setValue(pet);

        // Upload de imagem
        if(file != null) {
            uploadImagem(file, petID);
        }

        final PetVaultActivity currentClass = this;

        // Recupera dados do dono para enviar email
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/" + userToken);

        final String finalPetID = petID;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String name = (String) dataSnapshot.getValue();
                //System.out.println(name);

                String name = (String) dataSnapshot.child("name").getValue();
                String email = (String) dataSnapshot.child("email").getValue();

                // Instantiate the RequestQueue.
                String url ="http://lasid.sor.ufscar.br/twittersearch/country/qrcode.php?email=" + email + "&userName=" + name.replaceAll(" ", "%20") + "&petName=" + petName.replaceAll(" ", "%20") + "&petToken=" + finalPetID;

                System.out.println("URL: " + url);

                AsyncHttpClient client = new AsyncHttpClient();
                client.get(url, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        System.out.println("[Registration Notification] Success");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        System.out.println("[Registration Notification] Failure");
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        System.out.println("[Registration Notification] Retrying");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        System.out.println("Pet inserted!");

    }

    public void uploadImagem(Uri file, String petID) {
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 1;
        //Bitmap bitmap = BitmapFactory.decodeFile(file.toString(), options);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference riversRef = storageRef.child("images/" + petID);
        uploadTask = riversRef.putBytes(data);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                System.out.println(downloadUrl);
            }
        });
    }

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
}
