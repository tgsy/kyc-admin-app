package com.example.tessa.kyc_admin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class VerifyActivity extends BaseActivity implements Listener {

    public static final String TAG = VerifyActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    private String UserUid;
    private String UserName;
    private String UserDob;
    private String UserID;
    private String UserPostalCode;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        UserUid = getIntent().getStringExtra("Uid");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("images").child(UserUid+".jpg");

        uid = (TextView) findViewById(R.id.verify__uid);
        name = (TextView) findViewById(R.id.verify__name);
        id = (TextView) findViewById(R.id.verify__id);
        dob = (TextView) findViewById(R.id.verify__dob);
        postal_code = (TextView) findViewById(R.id.verify__postalcode);

        image = (ImageView) findViewById(R.id.verify__image);

        getDataFromFirebase(UserUid);
    }

    private void getDataFromFirebase(String uid){
        showProgressDialog();
        mDatabase.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        showProgressDialog();
                        User user = dataSnapshot.getValue(User.class);
                        UserUid = user.getUid();
                        UserName = user.getFull_name();
                        UserID = user.getId();
                        UserPostalCode = user.getPostal_code();
                        UserDob = user.getDate_of_birth();
                        Log.i("DED ", "User name: "+UserName);
                        Log.i("DED ", "User uid: "+UserUid);
                        Log.i("DED ", "User id: "+UserID);
                        Log.i("DED ", "User postal: "+UserPostalCode);
                        Log.i("DED ", "User dob: "+UserDob);
                        updateUI();
                        hideProgressDialog();
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

        Glide.with(this /* context */)
                .load(mStorageRef)
                .into(image);
    }

    public void onClick(View view) {
        if (view.getId()==R.id.verify__verify) {
            showWriteFragment();
//            Intent intent = new Intent(this, LoggedInActivity.class);
//            startActivity(intent);
        }
        if (view.getId()==R.id.verify__read) {
            showReadFragment();
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

    private void showWriteFragment() {

        isWrite = true;

        mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);

        if (mNfcWriteFragment == null) {

            mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        mNfcWriteFragment.show(getFragmentManager(),NFCWriteFragment.TAG);

    }

    private void showReadFragment() {

        mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getFragmentManager(),NFCReadFragment.TAG);

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

}
