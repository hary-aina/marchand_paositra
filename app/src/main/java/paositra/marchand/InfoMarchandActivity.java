package paositra.marchand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class InfoMarchandActivity extends AppCompatActivity {

    private final static String confPref = "conf_client";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_marchand);

        //affichage des info perso
        preferences = getSharedPreferences(confPref, Context.MODE_PRIVATE);
        TextView nom = (TextView) findViewById(R.id.nom);
        nom.setText(preferences.getString("nom", ""));
        TextView prenom = (TextView) findViewById(R.id.prenom);
        prenom.setText(preferences.getString("prenom", ""));
        TextView adresse = (TextView) findViewById(R.id.adresse);
        adresse.setText(preferences.getString("adresse", ""));
        TextView telephone = (TextView) findViewById(R.id.telephone);
        telephone.setText(preferences.getString("telephone", ""));
        TextView marchand = (TextView) findViewById(R.id.marchand);
        marchand.setText(preferences.getString("marchand", ""));
        TextView numero_caisse = (TextView) findViewById(R.id.numero_caisse);
        numero_caisse.setText(preferences.getString("numero_caisse", ""));

        //finish info perso
        ImageButton returnBtn = (ImageButton)findViewById(R.id.retour);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}