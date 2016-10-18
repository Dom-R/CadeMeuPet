package com.cademeupet.cademeupet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

public class PetDataActivity extends AppCompatActivity {

    private static final int REQUEST_GPS = 8572;
    private LocationManager locationManager;
    private Criteria criteria;
    private Looper looper;
    private LocationListener locationListener;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_data);

        Intent intent = getIntent();
        final String petID = intent.getStringExtra("PET_DATA");

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("pets/" + petID);
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String name = (String) dataSnapshot.getValue();
                //System.out.println(name);
                PetInfo pet = dataSnapshot.getValue(PetInfo.class);
                System.out.println(pet.getName());

                // Recover Image
                StorageReference petImage = storageRef.child("images/" + petID);

                final long ONE_MEGABYTE = 16384 * 16384;
                petImage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        ImageView btn = (ImageView)findViewById(R.id.imagePetImage);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        btn.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 550, 550, false));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        // Handle any errors
                    }
                });

                TextView textPetName = (TextView) findViewById(R.id.textPetName);
                textPetName.setText(pet.getName());

                TextView textPetSex = (TextView) findViewById(R.id.textPetSex);
                textPetSex.setText(pet.getSex());

                TextView textPetSpecie = (TextView) findViewById(R.id.textPetSpecie);
                textPetSpecie.setText(pet.getSpecie());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        final PetDataActivity currentClass = this;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Log.d("Location Changes", location.toString());
                System.out.println("GPS Latitude: " + String.valueOf(location.getLatitude()));
                System.out.println("GPS Longitude: " + String.valueOf(location.getLongitude()));

                // Insere no Banco de Dados o acesso ao ID de um cachorro
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("pets/" + petID);

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //String name = (String) dataSnapshot.getValue();
                        //System.out.println(name);

                        final PetInfo pet = dataSnapshot.getValue(PetInfo.class);

                        String userID = pet.getUserID();

                        // Recupera dados do dono para enviar email
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference("users/" + userID);

                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //String name = (String) dataSnapshot.getValue();
                                //System.out.println(name);

                                String name = (String) dataSnapshot.child("name").getValue();
                                String email = (String) dataSnapshot.child("email").getValue();
                                String phone = (String) dataSnapshot.child("phoneNumber").getValue();
                                String requestID = (String) dataSnapshot.child("NotificationID").getValue();

                                TextView textOwnerName = (TextView) findViewById(R.id.textOwnerName);
                                textOwnerName.setText(name);

                                TextView textOwnerEmail = (TextView) findViewById(R.id.textOwnerEmail);
                                textOwnerEmail.setText(email);

                                TextView textOwnerPhone = (TextView) findViewById(R.id.textOwnerPhone);
                                textOwnerPhone.setText(phone);

                                // Instantiate the RequestQueue.
                                RequestQueue queue = Volley.newRequestQueue(currentClass);
                                String url ="http://lasid.sor.ufscar.br/twittersearch/country/registration.php?email=" + email + "&petName=" + pet.getName() + "&location=" + pet.getLastLocation() + "&userName=" + name;

                                System.out.println("URL: " + url);

                                // Request a string response from the provided URL.
                                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                // Display the first 500 characters of the response string.
                                                System.out.println("Worked!");
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println("That didn't work!");
                                    }
                                });
                                // Add the request to the RequestQueue.
                                queue.add(stringRequest);

                                // Envio de notificação por cURL
                                // Instantiate the RequestQueue.
                                String notificationUrl ="http://lasid.sor.ufscar.br/twittersearch/country/sendnotification.php?id=" + requestID + "&title=CadêMeuPet!&body=Dados%20de%20" + pet.getName() + "acabaram%20de%20ser%20acessados!";

                                System.out.println("URL: " + notificationUrl);

                                // Request a string response from the provided URL.
                                StringRequest stringRequest2 = new StringRequest(Request.Method.GET, notificationUrl,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                // Display the first 500 characters of the response string.
                                                System.out.println("Worked!");
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println("That didn't work!");
                                    }
                                });
                                // Add the request to the RequestQueue.
                                queue.add(stringRequest2);

                                // curl --header "Authorization: key=AIzaSyD56xONeA1zPaEojB0lVgg69cmTNgz6YjY" --header Content-Type:"application/json" https://fcm.googleapis.com/fcm/send -d "{\"notification\": { \"title\": \"Portugal vs. Denmark\",\"body\": \"5 to 1\", \"sound\": \"default\"},\"to\" : \"djVv1NK7LV0:APA91bH7OkmYuwf4B1xuF8rLLWaGEYWRyWZiCR3SfkOxQGyFAN94QsJ0fi__oCprv5mwWZ24rQfpjU_IBofDyRb0o9OXtfbPYER9km3cuND0-_hLwSghlbwzTINwj_kjF9NA5t_Vk9mU\"}"
                                // https://firebase.google.com/docs/cloud-messaging/server
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });

                        pet.setLastLocation(location.getLatitude() + "," + location.getLongitude());

                        DatabaseReference reference = database.getReference("pets/" + petID);
                        reference.setValue(pet);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };

        // Now create a location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Now first make a criteria with your requirements
        // this is done to save the battery life of the device
        // there are various other other criteria you can search for..
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // This is the Best And IMPORTANT part
        looper = null;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_GPS);
            return;
        }
        locationManager.requestSingleUpdate(criteria, locationListener, looper);
        //System.out.println(location.get("longitude") + " " + location.get("latitude"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_GPS);
                        return;
                    }
                    locationManager.requestSingleUpdate(criteria, locationListener, looper);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}