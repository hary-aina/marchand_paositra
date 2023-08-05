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

import paositra.marchand.utils.NetworkChangeReceiver;

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
        TextView detail_achatTextView = (TextView) findViewById(R.id.detail_achat);
        TextView telephoneTextView = (TextView) findViewById(R.id.telephone);
        EditText code_retrait = (EditText) findViewById(R.id.code_retrait);

        //appel endpoint ici


    }

}