package paositra.marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class InfoFacturationNFCActivity extends AppCompatActivity {

    private final static String confPref = "conf_client";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_facturation_nfc);

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

    //start NFC card reader
    public void startNfcCardReader(View v){
        Intent nfcCardReaderActivity = new Intent(v.getContext().getApplicationContext(), NfcCardReaderActivity.class);
        startActivity(nfcCardReaderActivity);
    }
}