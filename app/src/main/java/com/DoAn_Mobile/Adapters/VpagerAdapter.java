package com.DoAn_Mobile.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.DoAn_Mobile.Fragments.FindFragment;
//import com.DoAn_Mobile.Fragments.HomeFragment;
import com.DoAn_Mobile.Fragments.HomeFragment;
import com.DoAn_Mobile.Fragments.ProfileFragment;
//import com.DoAn_Mobile.Fragments.FindFragment;
import com.DoAn_Mobile.Fragments.WatchFragment;
public class VpagerAdapter extends FragmentStateAdapter {

    public VpagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new HomeFragment();
            case 1:
                return new WatchFragment();
            case 2:
                return new FindFragment();
            case 3:
                return new ProfileFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
