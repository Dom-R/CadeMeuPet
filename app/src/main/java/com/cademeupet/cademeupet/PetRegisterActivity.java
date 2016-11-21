package com.cademeupet.cademeupet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;

public class PetRegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_RESULT = 2845;
    private static final int PAYPAL_REQUEST = 4958;
    private EditText inputPetName;
    private RadioGroup inputPetSex;
    private String petID;
    private Uri file;
    private StorageReference storageRef;
    private Spinner spinnerSpecie;
    private Button paypalButton;

    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId("AVReue7hj6TCzhfSv07LoHyJF9jncDYcEjDIRrloQiscEVBVLU57PWtpvNb3oojJxpdLPwkw4jyNTqAV");

    private boolean hasPayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_register);

        Intent paypalIntent = new Intent(this, PayPalService.class);

        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(paypalIntent);

        inputPetName = (EditText) findViewById(R.id.inputPetName);
        inputPetSex = (RadioGroup) findViewById(R.id.radioGroupSex);
        spinnerSpecie = (Spinner) findViewById(R.id.spinnerSpecie);
        paypalButton = (Button) findViewById(R.id.paypalButton);

        // Create a storage reference from our app
        storageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        petID = intent.getStringExtra("PET_ID");
        if(petID != null) {
            TextView button = (TextView) findViewById(R.id.register);
            button.setText("Atualizar");

            final PetRegisterActivity currentClass = this;

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("pets/" + petID);
            // Attach a listener to read the data at our posts reference
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PetInfo pet = dataSnapshot.getValue(PetInfo.class);
                    System.out.println(pet.getName());

                    // Setting pet name
                    EditText textPetName = (EditText) findViewById(R.id.inputPetName);
                    textPetName.setText(pet.getName());

                    // Setting pet sex
                    RadioButton radioButton;
                    if(pet.getSex().equals("Macho"))
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
        } else {
            paypalButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public void registerUser(View view) {
        if (inputPetName.getText().toString().trim().equals("")) {
            inputPetName.setError("Por favor insira o nome completo");
        } else if (inputPetSex.getCheckedRadioButtonId() == -1) {
            TextView temp = (TextView) findViewById(R.id.textPetSex);
            temp.setError("Por favor selecione uma opção");
        } else if(hasPayed == false) {
            new AlertDialog.Builder(this)
                    .setTitle("Registro de Animal de Estimação!")
                    .setMessage("Você precisa pagar a taxa única para poder registrar seu animal de estimação!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .setNegativeButton("OK", null)
                    .show();
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
        startActivityForResult(Intent.createChooser(intent, "Selecione uma foto"), PICK_IMAGE_RESULT);
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

        if(requestCode == PAYPAL_REQUEST) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    hasPayed = true;
                    paypalButton.setVisibility(View.GONE);
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                Log.i("paymentExample", "O usuário cancelou.");
            }
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "Um pagamento invalido ou configuração invalida foi submetida. Por favor leia o doc.");
            }
        }
    }

    public void onBuyPressed(View pressed) {

        // PAYMENT_INTENT_SALE will cause the payment to complete immediately.
        // Change PAYMENT_INTENT_SALE to
        //   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
        //   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
        //     later via calls from your server.

        PayPalPayment payment = new PayPalPayment(new BigDecimal("9.99"), "BRL", "Taxa para cadastrar um animal",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQUEST);
    }

}