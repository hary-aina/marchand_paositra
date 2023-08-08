package paositra.marchand;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import paositra.marchand.clientApi.RetrofitClient;
import paositra.marchand.service.ApiService;
import paositra.marchand.utils.NetworkChangeReceiver;
import paositra.marchand.utils.NfcUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.io.IOException;

public class NfcCardReaderActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback, NetworkChangeReceiver.OnNetworkChangeListener{

    private final static String confPref = "conf_client";
    SharedPreferences preferences;
    private NfcUtils Utils = new NfcUtils();
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private NetworkChangeReceiver networkChangeReceiver;

    private String montant;
    private String detail_achat;

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
            Toast.makeText(this, "reseau wi-fi opérationel", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Non connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_card_reader);
        networkChangeReceiver = new NetworkChangeReceiver(this);

        //get text from intent
        Intent intent = getIntent();
        montant = intent.getStringExtra("montant");
        detail_achat = intent.getStringExtra("detail_achat");

        //finish
        ImageButton returnBtn = (ImageButton)findViewById(R.id.retour);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //verification de l'existence d'appareil NFC sur le mobile
        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…
            Toast.makeText(this, "NFC NON RECONU", Toast.LENGTH_LONG).show();
            finish();
        }else{
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC N'EST PAS ACTIVE EN MODE STANDARDS", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // single top flag avoids activity multiple instances launching

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //Enable NFC foreground detection
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC N'EST PAS ACTIVE EN MODE STANDARDS", Toast.LENGTH_LONG).show();
                finish();
            }
            nfcAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        } else {
            Toast.makeText(this, "NFC NON RECONU", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        Activity here = this;
        try {
            isoDep.connect();
            byte[] responseAPDU = isoDep.transceive(Utils.hexStringToByteArray(
                    "00A4040007A0000002471001"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String response =  Utils.toHex(responseAPDU);
                    String[] responseArray = response.split("|");

                    if(responseArray.length == 2 && responseArray[0].equals(Utils.STATUS_SUCCESS) ){

                        /** initialisation code retrait (ajout endpoint ici) **/

                        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
                        String token = preferences.getString("token", "");
                        //initialisation de la connexion vers le serveur
                        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                        // Create the JSON string you want to send
                        String jsonString = "{\"telephone\":\""+responseArray[1]+"\",\"montant\":"+montant+"}";
                        // Convert the JSON string to RequestBody
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);
                        Call<JsonObject> call = apiService.initialisationRetraitEspeceWithCodeRetrait(token, requestBody);
                        call.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if(response.isSuccessful()){
                                    JsonObject responsebody = response.body();
                                    boolean error = responsebody.get("error").getAsBoolean();
                                    int code = responsebody.get("code").getAsInt();

                                    if(code == 401 || code == 403){
                                        //erreur token refaire l'authentification

                                        Toast.makeText(getApplication(), "RECONNEXION REQUIS", Toast.LENGTH_LONG).show();
                                        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.clear();
                                        editor.commit();
                                        Intent MainActivity = new Intent(getApplication(), MainActivity.class);
                                        startActivity(MainActivity);
                                        finish();

                                    } else if (error == true) {
                                        //erreu de service
                                        JsonObject data = responsebody.get("data").getAsJsonObject();
                                        String message = data.get("errorMessage").getAsString();
                                        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();

                                    }else{
                                        //success
                                        //send some extra data if success
                                        Intent validationCodeActivity = new Intent(here, ValidationCodeActivity.class);
                                        validationCodeActivity.putExtra("montant", montant);
                                        validationCodeActivity.putExtra("detail_achat", detail_achat);
                                        validationCodeActivity.putExtra("telephone", responseArray[1]);
                                        startActivity(validationCodeActivity);
                                        finish();
                                    }

                                }else{
                                    Toast.makeText(getApplication(), "ERREUR DE SERVICE", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                Toast.makeText(getApplication(), "ERREUR SERVEUR", Toast.LENGTH_LONG).show();
                            }
                        });

                    }else if(responseArray[0].equals(Utils.STATUS_FAILED)){
                        Toast.makeText(here, "Echec de la lecture", Toast.LENGTH_LONG).show();
                    }else if(responseArray[0].equals(Utils.CLA_NOT_SUPPORTED)){
                        Toast.makeText(here, "classe de commande non supportée", Toast.LENGTH_LONG).show();
                    }else if(responseArray[0].equals(Utils.INS_NOT_SUPPORTED)){
                        Toast.makeText(here, "code d'instruction non supporté", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(here, "\nCard Response: " + response, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isoDep.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}