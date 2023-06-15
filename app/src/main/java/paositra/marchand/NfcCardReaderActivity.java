package paositra.marchand;

import androidx.appcompat.app.AppCompatActivity;

import paositra.marchand.utils.NfcUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

public class NfcCardReaderActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback{

    private NfcUtils Utils = new NfcUtils();
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_card_reader);

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
            byte[] response = isoDep.transceive(Utils.hexStringToByteArray(
                    "00A4040007A0000002471001"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(here, "\nCard Response: " + Utils.toHex(response), Toast.LENGTH_LONG).show();
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