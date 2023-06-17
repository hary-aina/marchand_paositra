package paositra.marchand;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import paositra.marchand.utils.NetworkChangeReceiver;

public class HomeFragment extends Fragment implements NetworkChangeReceiver.OnNetworkChangeListener{

    private final static String confPref = "conf_client";
    SharedPreferences preferences;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public void onResume() {
        super.onResume();
        networkChangeReceiver = new NetworkChangeReceiver(this);
        requireActivity().registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(networkChangeReceiver);
        networkChangeReceiver = null;
    }
    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
            //Toast.makeText(getContext(), "Connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
            LinearLayout lost_connexion = getActivity().findViewById(R.id.lost_connexion);
            lost_connexion.setVisibility(View.GONE);

            preferences = getActivity().getSharedPreferences(confPref, Context.MODE_PRIVATE);

            TextView epoketra = (TextView) getActivity().findViewById(R.id.epoketra);
            epoketra.setText("AR "+preferences.getString("solde_carte", ""));
            TextView paositra_money = (TextView) getActivity().findViewById(R.id.paositra_money);
            paositra_money.setText("AR "+preferences.getString("solde_compte", ""));

        }else{
            Toast.makeText(getContext(), "Non connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
            LinearLayout lost_connexion = getActivity().findViewById(R.id.lost_connexion);
            lost_connexion.setVisibility(View.VISIBLE);

            TextView epoketra = (TextView) getActivity().findViewById(R.id.epoketra);
            epoketra.setText("solde inconnu");
            TextView paositra_money = (TextView) getActivity().findViewById(R.id.paositra_money);
            paositra_money.setText("solde inconnu");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getActivity().getSharedPreferences(confPref, Context.MODE_PRIVATE);

        TextView epoketra = (TextView) view.findViewById(R.id.epoketra);
        epoketra.setText("AR "+preferences.getString("solde_carte", ""));
        TextView paositra_money = (TextView) view.findViewById(R.id.paositra_money);
        paositra_money.setText("AR "+preferences.getString("solde_compte", ""));

        //start Info marchand Activity
        ImageButton profilBtn = view.findViewById(R.id.profil);
        profilBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInfoMarchand(v);
            }
        });

        //start info facture nfc Activity
        ImageButton nfcBtn = view.findViewById(R.id.nfcBtn);
        nfcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInfoFacturationNFC(v);
            }
        });

        //logout
        ImageButton logoutBtn = view.findViewById(R.id.logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deconnexion();
            }
        });

        return view;
    }
    //start info marchand
    public void startInfoMarchand(View v){
        Intent infoMarchandActivity = new Intent(v.getContext().getApplicationContext(), InfoMarchandActivity.class);
        startActivity(infoMarchandActivity);
    }
    //start nfc paiement
    public void startInfoFacturationNFC(View v){
        Intent infoFacturationActivity = new Intent(v.getContext().getApplicationContext(), InfoFacturationNFCActivity.class);
        startActivity(infoFacturationActivity);
    }
    public void deconnexion(){

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Deconnection");
        alert.setMessage("Etes-vous sur de quitter la session ?")
                .setPositiveButton("Rester", null)
                .setNegativeButton("Quitter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences = getActivity().getSharedPreferences(confPref, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        ((MainActivity)getActivity()).loadLogin();
                    }
                });
        AlertDialog alertLogout = alert.create();
        alertLogout.show();
    }

}