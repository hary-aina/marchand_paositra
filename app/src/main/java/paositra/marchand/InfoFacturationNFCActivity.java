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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import paositra.marchand.utils.NetworkChangeReceiver;

public class InfoFacturationNFCActivity extends AppCompatActivity implements NetworkChangeReceiver.OnNetworkChangeListener {

    private final static String confPref = "conf_client";
    SharedPreferences preferences;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_facturation_nfc);
        networkChangeReceiver = new NetworkChangeReceiver(this);

        //changement des informations automatiques
        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
        TextView numero_caisse = (TextView) findViewById(R.id.numero_caisse);
        numero_caisse.setText("AR "+preferences.getString("numero_caisse", ""));
        TextView marchand = (TextView) findViewById(R.id.marchand);
        marchand.setText("AR "+preferences.getString("marchand", ""));

        //start NFC card reader
        Button launch_NFC_Reader = (Button)findViewById(R.id.launch_NFC_Reader);
        launch_NFC_Reader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNfcCardReader(v);
            }
        });

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

            Button launch_NFC_Reader = findViewById(R.id.launch_NFC_Reader);
            launch_NFC_Reader.setEnabled(true);
            launch_NFC_Reader.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary));

        }else{
            Toast.makeText(this, "Non connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
            LinearLayout lost_connexion = findViewById(R.id.lost_connexion);
            lost_connexion.setVisibility(View.VISIBLE);

            Button launch_NFC_Reader = findViewById(R.id.launch_NFC_Reader);
            launch_NFC_Reader.setEnabled(false);
            launch_NFC_Reader.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.neutral));
        }
    }

    //start NFC card reader
    public void startNfcCardReader(View v){
        Intent nfcCardReaderActivity = new Intent(v.getContext().getApplicationContext(), NfcCardReaderActivity.class);
        startActivity(nfcCardReaderActivity);
    }
}