package com.example.tessa.kyc_admin;

import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class LoggedInActivity extends BaseActivity {


    //private SectionsPagerAdapter mSectionsPagerAdapter;
    private PagerAdapter mAdapter;

    private ViewPager mViewPager;

    private SearchView mSearchBar;

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
        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //mViewPager.setAdapter(mSectionsPagerAdapter);
        mSearchBar = (SearchView) findViewById(R.id.search_bar);
        // Get the intent, verify the action and get the query

        mSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            Bundle bundle = new Bundle();

            @Override
            public boolean onQueryTextSubmit(String s) {
                bundle.putString("Query", s);
                mAdapter.setBundle(bundle);
                mAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                bundle.putString("Query", s);
                mAdapter.setBundle(bundle);
                mAdapter.notifyDataSetChanged();
                Log.i("TES", "tostring:"+bundle.toString());
                return false;
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
}
