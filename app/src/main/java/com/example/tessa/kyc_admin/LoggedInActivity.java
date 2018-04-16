package com.example.tessa.kyc_admin;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import android.util.Base64;

public class LoggedInActivity extends BaseActivity {

    private PagerAdapter mAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mAdapter = new PagerAdapter
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
                                String userId = input.getText().toString();
                                reactivateToken(BlocktraceCrypto.hash256(userId));
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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

    private int reactivateToken(String blockId){
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("https://kyc-project.herokuapp.com/token_found");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("block_id", blockId);

            //get public key from kyc backend for encryption
            String str_public_key = Http_Get("https://kyc-project.herokuapp.com/getkey");
            //convert string public key to public key object and create JSONObject for the content you want to post and encrypt them using the public key
            byte[] pubKeyByte = BlocktraceCrypto.pemToBytes(str_public_key);

            JSONObject encryptedJSON =  encrypt_json(jsonObject, pubKeyByte);

            urlConnection = (HttpURLConnection) url.openConnection();
            //set the request method to Post
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type","application/json");
            String encoded = Base64.encodeToString(("admin"+":"+"secret").getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);  //Java 8
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
            Toast.makeText(getApplicationContext(), "Token Reactivation Successful", Toast.LENGTH_SHORT).show();
            return responseCode;
        } catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Token Reactivation Failed", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return 0;
        }
    }
}
