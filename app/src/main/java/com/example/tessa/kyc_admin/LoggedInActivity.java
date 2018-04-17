package com.example.tessa.kyc_admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import android.util.Base64;

public class LoggedInActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        PagerAdapter mAdapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoggedInActivity.this);
                final EditText input = new EditText(LoggedInActivity.this);
                builder.setTitle("Re-activate Lost Token");
                builder.setIcon(R.drawable.ic_autorenew_black_24dp);
                builder.setMessage(R.string.enter_id_prompt);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(16, 0, 16, 0);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setPositiveButton(R.string.reactivate, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                switch (id) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        Log.i("input", input.getText().toString());
                                        if (TextUtils.isEmpty(input.getText().toString())) {
                                            Log.i("input", input.getText().toString());
                                            input.setError("Required");
                                        } else {
                                            Log.i("input", input.getText().toString().toUpperCase());
                                            new reactivateTokenTask().execute(BlocktraceCrypto.hash256(input.getText().toString().toUpperCase()));
                                        }
                                        break;
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logged_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exiting blocktrace")
                .setMessage("Are you sure you want to log out and exit blocktrace?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }

                })
                .setNegativeButton("NO", null)
                .show();
    }
    private class reactivateTokenTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... string) {
            HttpURLConnection urlConnection = null;

            try {
                Log.i("TAG", string[0]);
                URL url = new URL("https://kyc-project.herokuapp.com/token_found");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("block_id", string[0]);

                //get public key from kyc backend for encryption
                String str_public_key = Http_Get("https://kyc-project.herokuapp.com/getkey");
                //convert string public key to public key object and create JSONObject for the content you want to post and encrypt them using the public key
                byte[] pubKeyByte = BlocktraceCrypto.pemToBytes(str_public_key);

                JSONObject encryptedJSON =  encrypt_json(jsonObject, pubKeyByte);

                urlConnection = (HttpURLConnection) url.openConnection();
                //set the request method to Post
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/json");
                String encoded = Base64.encodeToString(("admin"+":"+"secret").getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);  //Java 8
                urlConnection.setRequestProperty("Authorization", "Basic "+encoded);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //output the stream to the server
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.
                        getOutputStream());
                wr.write(encryptedJSON.toString());
                wr.flush();
                int responseCode = urlConnection.getResponseCode();
                return responseCode;
            } catch (Exception ex){
                ex.printStackTrace();
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer==200) {
                Toast.makeText(getApplicationContext(), "Token Reactivation Successful", Toast.LENGTH_SHORT).show();
            }

            else
                Toast.makeText(getApplicationContext(), "Token Reactivation Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
