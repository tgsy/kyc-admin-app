package com.example.tessa.kyc_admin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.InputStream;

public class VerifyActivity extends BaseActivity implements Listener {

    public static final String TAG = VerifyActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    private String imageUrl;

    private String UserEmail;
    private String UserUid;
    private String UserName;
    private String UserDob;
    private String UserID;
    private String UserPostalCode;
    private String UserImage;

    private TextView uid;
    private TextView name;
    private TextView id;
    private TextView dob;
    private TextView postal_code;

    private ImageView image;

    private NFCWriteFragment mNfcWriteFragment;
    private NFCReadFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    private NfcAdapter mNfcAdapter;

    private JSONObject token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        UserUid = getIntent().getStringExtra("Uid");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("images").child(UserUid+".jpg");

        uid = findViewById(R.id.verifyActivity_uid);
        name = findViewById(R.id.verifyActivity_name);
        id = findViewById(R.id.verifyActivity_id);
        dob = findViewById(R.id.verifyActivity_dob);
        postal_code = findViewById(R.id.verifyActivity_postalcode);

        image = findViewById(R.id.verifyActivity_image);

        getDataFromFirebase(UserUid);
    }

    private void getDataFromFirebase(String uid){
        showProgressDialog();
        mDatabase.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //showProgressDialog();
                        User user = dataSnapshot.getValue(User.class);
                        UserEmail = user.getEmail();
                        UserUid = user.getUid();
                        UserName = user.getFull_name();
                        UserID = user.getId();
                        UserPostalCode = user.getPostal_code();
                        UserDob = user.getDate_of_birth();
                        UserImage = user.getImage();
                        Log.i("DED ", "User email: "+UserEmail);
                        Log.i("DED ", "User name: "+UserName);
                        Log.i("DED ", "User uid: "+UserUid);
                        Log.i("DED ", "User id: "+UserID);
                        Log.i("DED ", "User postal: "+UserPostalCode);
                        Log.i("DED ", "User dob: "+UserDob);
                        Log.i("DED ", "User image: "+UserImage);
                        updateUI();
                        //hideProgressDialog();
                        new imageDownloading().execute(UserImage);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
        hideProgressDialog();
    }

    private void updateUI() {
        uid.setText(UserUid);
        name.setText(UserName);
        id.setText(UserID);
        dob.setText(UserDob);
        postal_code.setText(UserPostalCode);
    }

    public void onClick(View view) {
        if (view.getId()==R.id.verifyActivity_verify) {
            new RegisterKYCTask().execute();
            Intent intent = new Intent(VerifyActivity.this, LoggedInActivity.class);
            startActivity(intent);

            Toast.makeText(VerifyActivity.this, "Verification Successful", Toast.LENGTH_LONG).show();

        }
    }

    public void verifyUser(String Uid) {
        mDatabase.child(Uid).child("status").setValue(2);
    }

    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed = true;

    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed = false;
        isWrite = false;

    }

    @Override
    protected void onResume() {
        Log.i("DED","onResume: ");
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {

                if (isWrite) {

                    String messageToWrite = UserUid;
                    mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    mNfcWriteFragment.onNfcDetected(ndef,messageToWrite);
                    Log.i("DED","message written: "+messageToWrite);
                    verifyUser(UserUid);

                } else {
                    mNfcReadFragment = (NFCReadFragment)getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                }
            }
        }
    }

    private class imageDownloading extends AsyncTask<String, Void, Bitmap> { //static??

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {
            imageUrl = URL[0];

            Bitmap bitmap = null;

            try {
                InputStream input = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return bitmap;


        }

        @Override
        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
            hideProgressDialog();
        }
    }

    class RegisterKYCTask extends AsyncTask<String,Void,String> { //static?
        @Override
        protected String doInBackground(String... params) {
            //get public key from kyc backend for encryption
            String str_public_key = Http_Get("https://kyc-project.herokuapp.com/getkey");

            try {
                //convert string public key to public key object and create JSONObject for the content you want to post and encrypt them using the public key
                byte[] pubKeyByte = BlocktraceCrypto.pemToBytes(str_public_key);

                JSONObject user_info_object = new JSONObject();
                user_info_object.put("name", UserName);
                user_info_object.put("postal_code", UserPostalCode);
                user_info_object.put("id_number", UserID);
                user_info_object.put("dob", UserDob);

                JSONObject encrypted_info = encrypt_json(user_info_object, pubKeyByte);

                //receive token info from kyc backend
                String token_received = Http_Post("https://kyc-project.herokuapp.com/register_kyc", encrypted_info); //local redundant??

                return token_received;

            } catch (Exception ex) {
                return "Exception: " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(VerifyActivity.this, result, Toast.LENGTH_LONG).show();
            try {
                //store the token received in the phone and convert the token into JSONObject
                token = new JSONObject(result);
                Log.i("Norman",token.get("merkle_raw").toString());
                Log.i("Norman","original token");
                Log.i("Norman",token.toString());

                mDatabase.child(UserUid).child("status").setValue(2);
                mDatabase.child(UserUid).child("image").setValue("null");
                mDatabase.child(UserUid).child("postal_code").setValue("null");
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

}
