package com.example.tessa.kyc_admin;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;

    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                VerifyFragment verifyTab = new VerifyFragment();
                return verifyTab;
            case 1:
                TokenFragment tokenTab = new TokenFragment();
                return tokenTab;
            case 2:
                AllUsersFragment allUsersTab = new AllUsersFragment();
                return allUsersTab;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }


}
