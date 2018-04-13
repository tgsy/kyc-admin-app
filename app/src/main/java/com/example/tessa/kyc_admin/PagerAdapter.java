package com.example.tessa.kyc_admin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    Bundle bundle;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        bundle = new Bundle();
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                VerifyFragment verifyTab = new VerifyFragment();
                verifyTab.setArguments(bundle);
                return verifyTab;
            case 1:
                TokenFragment tokenTab = new TokenFragment();
                tokenTab.setArguments(bundle);
                return tokenTab;
            case 2:
                AllUsersFragment allUsersTab = new AllUsersFragment();
                allUsersTab.setArguments(bundle);
                return allUsersTab;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }


    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

}
