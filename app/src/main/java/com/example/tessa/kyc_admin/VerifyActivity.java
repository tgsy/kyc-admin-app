package com.example.tessa.kyc_admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.InputStream;

public class VerifyActivity extends BaseActivity {

    private DatabaseReference mDatabase;

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
    private TextView email;

    private PhotoView image;

    private static AsyncTask<String, Void, Bitmap> imageDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        UserUid = getIntent().getStringExtra("Uid");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        uid = findViewById(R.id.verifyActivity_uid);
        name = findViewById(R.id.verifyActivity_name);
        id = findViewById(R.id.verifyActivity_id);
        dob = findViewById(R.id.verifyActivity_dob);
        postal_code = findViewById(R.id.verifyActivity_postalcode);
        email = findViewById(R.id.verifyActivity_email);
        image = findViewById(R.id.verifyActivity_image);
        getDataFromFirebase(UserUid);
    }

    private void getDataFromFirebase(String uid){
        showProgressDialog();
        mDatabase.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        UserEmail = user.getEmail();
                        UserUid = user.getUid();
                        UserName = user.getFull_name();
                        UserID = user.getId();
                        UserPostalCode = user.getPostal_code();
                        UserDob = user.getDate_of_birth();
                        UserImage = user.getImage();
                        updateUI();
                        imageDownload = new imageDownload().execute(UserImage);
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
        email.setText(UserEmail);
    }

    public void onClick(View view) {
        int v = view.getId();
        if (v==R.id.verifyActivity_verify) {
            showProgressDialog();
            new RegisterKYCTask().execute();
        }

        else if (v==R.id.verifyActivity_error) {
            mDatabase.child(UserUid).child("status").setValue(3);
            imageDownload.cancel(true);
            startActivity(new Intent(this, LoggedInActivity.class));
            finish();
        }
    }

    private class imageDownload extends AsyncTask<String, Void, Bitmap> { //static??

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageUrl = URL[0];

            Bitmap bitmap = null;

            try {
                InputStream input = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception ex){
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
                user_info_object.put("id_number", UserID.toUpperCase());
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
            hideProgressDialog();
            if (result.contains("False")) {
                Toast.makeText(VerifyActivity.this, "Verification failed.", Toast.LENGTH_LONG).show();
            } else {
                try {
                    //store the token received in the phone and convert the token into JSONObject
                    //JSONObject token = new JSONObject(result);
                    mDatabase.child(UserUid).child("status").setValue(2);
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(UserImage);
                    imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDatabase.child(UserUid).child("image").setValue("null");
                            Toast.makeText(getApplicationContext(), "onSuccess: deleted file", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "onFailure: deleted file", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mDatabase.child(UserUid).child("id").setValue("null");
                    mDatabase.child(UserUid).child("postal_code").setValue("null");
                    mDatabase.child(UserUid).child("date_of_birth").setValue("null");
                    Toast.makeText(VerifyActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyActivity.this, WriteTokenActivity.class);
                    intent.putExtra("KEY", result);
                    startActivity(intent);
                    finish();

                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(getParentActivityIntent());
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

