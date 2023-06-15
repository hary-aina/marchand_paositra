package paositra.marchand;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    private final static String confPref = "conf_client";
    SharedPreferences preferences;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
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

                    EditText editLoginText = (EditText) view.findViewById(R.id.editLoginText);
                    EditText editTextPassword = (EditText) view.findViewById(R.id.editTextPassword);

                    //stockage des informations utilisateurs
                    preferences = getActivity().getSharedPreferences(confPref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("nom", "RAVAOARISOA");
                    editor.putString("prenom", "Marcel");
                    editor.putString("adresse", "Lot II A Bis Ivandry");
                    editor.putString("telephone", "+261 32 48 965 90");
                    editor.putString("marchand", "Antaninarenina");
                    editor.putString("numero_caisse", "006");

                    editor.putString("login", editLoginText.getText().toString());
                    editor.putString("password", editTextPassword.getText().toString());
                    editor.putString("idCaissier", "1");
                    editor.putString("solde_compte", "20000000");
                    editor.putString("solde_carte", "200000");
                    editor.putString("token", "ujubeizhevuzjvbezrhbvzoerbveezouvbrvhrvizbr");

                    editor.commit();

                    ((MainActivity)getActivity()).loadHome();
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

}