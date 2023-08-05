package paositra.marchand;

import android.content.Context;
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
import android.widget.EditText;
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

public class LoginFragment extends Fragment implements NetworkChangeReceiver.OnNetworkChangeListener {

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
            Button loginBtn = getActivity().findViewById(R.id.loginBtn);
            loginBtn.setEnabled(true);
            loginBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.danger));
        }else{
            Button loginBtn = getActivity().findViewById(R.id.loginBtn);
            loginBtn.setEnabled(false);
            loginBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.neutral));
            Toast.makeText(getContext(), "Non connecter au reseau wi-fi", Toast.LENGTH_SHORT).show();
        }
    }

    public LoginFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginBtn = view.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificationChamp(view)){
                    authetification(view);
                }
            }
        });

        return view;
    }
    public boolean verificationChamp(View v){
        boolean result = true;
        //verification du champ login
        TextView loginText = (TextView) v.findViewById(R.id.loginText);
        EditText editLoginText = (EditText) v.findViewById(R.id.editLoginText);
        if(editLoginText.getText().toString().equals("")){
            loginText.setText("Identifiant obligatoire");
            loginText.setTextColor(getResources().getColor(R.color.danger));
            result = false;
        }else{
            loginText.setText("Identifiant");
            loginText.setTextColor(getResources().getColor(R.color.neutral));
        }
        //verification du champ login
        TextView passwordText = (TextView) v.findViewById(R.id.passwordText);
        EditText editTextPassword = (EditText) v.findViewById(R.id.editTextPassword);
        if(editTextPassword.getText().toString().equals("")){
            passwordText.setText("Mots de passe obligatoire");
            passwordText.setTextColor(getResources().getColor(R.color.danger));
            result = false;
        }else{
            passwordText.setText("Mots de passe");
            passwordText.setTextColor(getResources().getColor(R.color.neutral));
        }

        return result;
    }

    //access au serveur a revoir
    private void authetification(View v){

        EditText editTextPassword = (EditText) v.findViewById(R.id.editTextPassword);
        EditText editLoginText = (EditText) v.findViewById(R.id.editLoginText);
        String login = editLoginText.getText().toString();
        String mot_de_passe = editTextPassword.getText().toString();

        //initialisation de la connexion vers le serveur
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Create the JSON string you want to send
        String jsonString = "{\"login\":\""+login+"\",\"password\":"+mot_de_passe+"}";
        // Convert the JSON string to RequestBody
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);

        Call<JsonObject> call = apiService.login(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                System.out.println(response);

                if(response.isSuccessful()){

                    JsonObject responsebody = response.body();

                    boolean error = responsebody.get("error").getAsBoolean();
                    int code = responsebody.get("code").getAsInt();
                    if((!error) && code == 200){

                        JsonObject data = responsebody.get("data").getAsJsonObject();

                        //recuperation token
                        String token = data.get("access_token").getAsString();

                        //recuperation info beneficiaire
                        JsonObject user_marchand = data.get("info_user_marchand").getAsJsonObject();
                        String idUser = user_marchand.get("iduser").getAsString();
                        String nom = user_marchand.get("nom").getAsString();
                        String prenom = user_marchand.get("prenom").getAsString();
                        String telephone = user_marchand.get("telephone").getAsString();
                        String login = user_marchand.get("login").getAsString();

                        /*String quichet = "";
                        if(user_marchand.get("guichet").isJsonNull()){
                            quichet = user_marchand.get("guichet").getAsString();
                        }
                        String fk_marchand = user_marchand.get("fk_marchand").getAsString();*/

                        //information caisse
                        JsonObject info_caisse = data.get("info_caisse").getAsJsonObject();
                        //String code_marchand = info_caisse.get("code_marchand").getAsString();
                        String numero_caisse = info_caisse.get("numero_caisse").getAsString();

                        //information solde
                        JsonObject marchand = data.get("marchand").getAsJsonObject();
                        double solde = marchand.get("solde").getAsDouble();
                        double solde_carte = marchand.get("solde_carte").getAsDouble();

                        //stockage des informations utilisateurs
                        preferences = getActivity().getSharedPreferences(confPref, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("nom", nom);
                        editor.putString("prenom", prenom);
                        editor.putString("adresse", "");
                        editor.putString("telephone", telephone);
                        editor.putString("marchand", "Shop Liantsoa");
                        editor.putString("numero_caisse", numero_caisse);

                        editor.putString("login", login);
                        editor.putString("password", mot_de_passe);
                        editor.putString("idUser", idUser);
                        editor.putString("solde_compte", ""+solde);
                        editor.putString("solde_carte", ""+solde_carte);
                        editor.putString("token", token);

                        editor.commit();

                        ((MainActivity)getActivity()).loadHome();

                    }else{
                        String message = responsebody.get("msg").getAsString();
                        Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(v.getContext(), "ERREUR DE SERVICE", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println(t);
                Toast.makeText(v.getContext(), "ERREUR SERVEUR", Toast.LENGTH_LONG).show();
            }
        });
    }

}
