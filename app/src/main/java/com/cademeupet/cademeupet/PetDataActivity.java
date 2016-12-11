package com.cademeupet.cademeupet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

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

        final SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
        final String petID = intent.getStringExtra("PET_DATA");

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

        // Create database reference from app
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Recover pet data from database
        DatabaseReference ref = database.getReference("pets/" + petID);
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final PetInfo pet = dataSnapshot.getValue(PetInfo.class);
                System.out.println("Pet Name: " + pet.getName());

                // Save pet name for notification sending
                prefs.edit().putString("petName", pet.getName()).commit();

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

                // Set pet display data
                TextView textPetName = (TextView) findViewById(R.id.textPetName);
                textPetName.setText(pet.getName());

                TextView textPetSex = (TextView) findViewById(R.id.textPetSex);
                textPetSex.setText(pet.getSex());

                TextView textPetSpecie = (TextView) findViewById(R.id.textPetSpecie);
                textPetSpecie.setText(pet.getSpecie());

                // Recupera dados do dono para enviar email e notificacao
                DatabaseReference ref = database.getReference("users/" + pet.getUserID());

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = (String) dataSnapshot.child("name").getValue();
                        String email = (String) dataSnapshot.child("email").getValue();
                        String phone = (String) dataSnapshot.child("phoneNumber").getValue();
                        String notificationID = (String) dataSnapshot.child("NotificationID").getValue();

                        // Set owner display data
                        TextView textOwnerName = (TextView) findViewById(R.id.textOwnerName);
                        textOwnerName.setText(name);

                        TextView textOwnerEmail = (TextView) findViewById(R.id.textOwnerEmail);
                        textOwnerEmail.setText(email);

                        TextView textOwnerPhone = (TextView) findViewById(R.id.textOwnerPhone);
                        textOwnerPhone.setText(phone);

                        // Save data on SharedPreferences
                        prefs.edit().putString("ownerID", pet.getUserID()).commit();
                        prefs.edit().putString("ownerName", name).commit();
                        prefs.edit().putString("ownerEmail", email).commit();
                        prefs.edit().putString("ownerNotificationID", notificationID).commit();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        /* *************** */
        /* LOCATION MODULE */
        /* *************** */
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Log.d("Location Changes", location.toString());
                System.out.println("GPS Latitude: " + String.valueOf(location.getLatitude()));
                System.out.println("GPS Longitude: " + String.valueOf(location.getLongitude()));

                // Atualiza a localizacao do ultimo acesso aos dados do animal
                DatabaseReference ref = database.getReference("pets/" + petID + "/lastLocation");
                ref.setValue(location.getLatitude() + "," + location.getLongitude());

                /* ****************** */
                /* Send Notifications */
                /* ****************** */
                // Generate url with parameters
                String url ="http://lasid.sor.ufscar.br/twittersearch/country/found.php?email=" + prefs.getString("ownerEmail", "dominik.reller@gmail.com") + "&petName=" + prefs.getString("petName", "Unknown Pet Name").replaceAll(" ", "%20") + "&location=" + location.getLatitude() + "," + location.getLongitude() + "&userName=" + prefs.getString("ownerName", "Unknown Name").replaceAll(" ", "%20");

                System.out.println("[Email Notification] URL: " + url);

                AsyncHttpClient client = new AsyncHttpClient();
                client.get(url, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        System.out.println("[Email Notification] Success");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        System.out.println("[Email Notification] Failure");
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        System.out.println("[Email Notification] Retrying");
                    }
                });

                // Envio de notificação para o celular
                String notificationUrl ="http://lasid.sor.ufscar.br/twittersearch/country/sendnotification.php?id=" + prefs.getString("ownerNotificationID", "MISSING") + "&name=" + prefs.getString("petName", "Unknown Pet Name").replaceAll(" ", "%20") + "&latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude();

                System.out.println("[Phone Notification] URL: " + notificationUrl);
                client.get(notificationUrl, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        System.out.println("[Phone Notification] Success");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        System.out.println("[Phone Notification] Failure");
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        System.out.println("[Phone Notification] Retrying");
                    }
                });

                // Envio de notificação para facebook
                String facebookUrl ="http://lasid.sor.ufscar.br/twittersearch/country/facebooknotif.php?facebookID=" + prefs.getString("ownerID", "1431781136836494") + "&location=" + location.getLatitude() + "," + location.getLongitude();

                System.out.println("[Facebook Notification] URL: " + facebookUrl);
                client.get(facebookUrl, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        System.out.println("[Facebook Notification] Success");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        System.out.println("[Facebook Notification] Failure");
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        System.out.println("[Facebook Notification] Retrying");
                    }
                });

                /* ********************** */
                /* END Send Notifications */
                /* ********************** */

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
        /* ******************* */
        /* END LOCATION MODULE */
        /* ******************* */
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
