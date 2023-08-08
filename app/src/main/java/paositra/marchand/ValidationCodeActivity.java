package paositra.marchand;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import paositra.marchand.clientApi.RetrofitClient;
import paositra.marchand.service.ApiService;
import paositra.marchand.utils.NetworkChangeReceiver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValidationCodeActivity extends AppCompatActivity implements NetworkChangeReceiver.OnNetworkChangeListener{

    private final static String confPref = "conf_client";
    SharedPreferences preferences;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_code);
        networkChangeReceiver = new NetworkChangeReceiver(this);

        //changement des informations automatiques
        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
        TextView numero_caisse = (TextView) findViewById(R.id.numero_caisse);
        numero_caisse.setText(preferences.getString("numero_caisse", ""));
        TextView marchand = (TextView) findViewById(R.id.marchand);
        marchand.setText(preferences.getString("marchand", ""));

        //get intent extra
        Intent intent = getIntent();
        String montant = intent.getStringExtra("montant");
        TextView montantTextView = (TextView) findViewById(R.id.montant);
        montantTextView.setText(montant);
        String detail_achat = intent.getStringExtra("detail_achat");
        TextView detail_achatTextView = (TextView) findViewById(R.id.detail_achat);
        detail_achatTextView.setText(detail_achat);
        String telephone = intent.getStringExtra("telephone");
        TextView telephoneTextView = (TextView) findViewById(R.id.telephone);
        telephoneTextView.setText(telephone);

        //finish
        ImageButton returnBtn = (ImageButton)findViewById(R.id.retour);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }
    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
            //Toast.makeText(getContext(), "Connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
            LinearLayout lost_connexion = findViewById(R.id.lost_connexion);
            lost_connexion.setVisibility(View.GONE);

        }else{
            Toast.makeText(this, "Non connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
            LinearLayout lost_connexion = findViewById(R.id.lost_connexion);
            lost_connexion.setVisibility(View.VISIBLE);
        }
    }

    private void validationAchat(){
        TextView montantTextView = (TextView) findViewById(R.id.montant);
        String montant = montantTextView.getText().toString();
        TextView detail_achatTextView = (TextView) findViewById(R.id.detail_achat);
        String detail_achat = detail_achatTextView.getText().toString();
        TextView telephoneTextView = (TextView) findViewById(R.id.telephone);
        String telephone = telephoneTextView.getText().toString();
        EditText code_retraitEditText = (EditText) findViewById(R.id.code_retrait);
        String code_retrait = code_retraitEditText.getText().toString();

        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");

        //initialisation de la connexion vers le serveur
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        // Create the JSON string you want to send
        String jsonString = "{\"telephone\":\""+telephone+"\",\"montant\":\""+montant+"\",\"code\":\""+code_retrait+"\",\"commentaire\":\""+detail_achat+"\"}";
        // Convert the JSON string to RequestBody
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);
        Call<JsonObject> call = apiService.validationRetraitEspeceByCodeRetrait(token, requestBody);

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
                        //generer une facture

                        //consultation de nouveau solde
                        Call<JsonObject> call2 = apiService.getSoldeMarchand(token);
                        call2.enqueue(new Callback<JsonObject>() {
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
                                        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        JsonObject data = responsebody.get("data").getAsJsonObject();
                                        editor.putString("solde_compte", ""+data.get("solde").getAsString());
                                        editor.putString("solde_carte", ""+data.get("solde_carte").getAsString());
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


    }

}