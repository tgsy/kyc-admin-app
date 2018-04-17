package com.example.tessa.kyc_admin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class WriteTokenActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_token);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = getIntent();
        token = intent.getStringExtra("KEY");
    }

    public void onClick(View view) {
        startActivity(new Intent(this, LoggedInActivity.class));
        finish();
    }

    /*
       If token is detected, write the token
   */
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            NdefMessage ndefMessage = createNdefMessage(token);
            writeNdefMessage(tag,ndefMessage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDipatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    /*
    enableForegroundDispatchSystem and disableForegroundDispatchSystem is for detecting if the token is present
    */
    private void enableForegroundDipatchSystem() {
        Intent intent = new Intent(this, WriteTokenActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }

    /*
        This method format the tag to NDEF format and write the message
     */
    private void formatTag(Tag tag, NdefMessage ndefMessage){
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null){
                Toast.makeText(this, "Error: Tag is not ndef formatable!", Toast.LENGTH_LONG).show();
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
        } catch (Exception e) {
           Log.e("formatTag",e.getMessage());
        }
    }

  /*
        Write info to the token
  */
    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage){
        try {
            if (tag == null) {
                Toast.makeText(this, "Error: Tag object cannot be null", Toast.LENGTH_LONG).show();
                return;
            }
            Ndef ndef = Ndef.get(tag);

            if (ndef == null){
                //format tag with the ndef format and writes the message
                formatTag(tag,ndefMessage);
            }
            else {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Error: Tag is not writable!", Toast.LENGTH_LONG).show();
                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                startActivity(new Intent(this, LoggedInActivity.class));
                Toast.makeText(this, "Token Generation successful", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Token Generation unsuccessful, Please try again", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    /*
        Creates an NDEF Record which contains typed data, such as MIME-type media, a URI, or a custom application payload.
     */
    private NdefRecord createTextRecord(String content){
        try {
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");
            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) (languageSize & 0x1F));
            payload.write(language,0,languageSize);
            payload.write(text,0,textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload.toByteArray());
        }
        catch (UnsupportedEncodingException ex) {
            Log.e("createTextRecord", ex.getMessage());
       }
        return null;
    }

    /*
       Creates an NDEF Message which is a container for one or more NDEF Records
     */
    private NdefMessage createNdefMessage(String content) {
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] { ndefRecord});
        return ndefMessage;
    }

}
