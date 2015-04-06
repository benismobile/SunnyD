package org.fabeo.benbutchart.webmap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Created by benbutchart on 02/04/2015.
 */
public class PagerAdapter extends FragmentPagerAdapter{

    final int PAGE_COUNT = 1;
    private String tabTitles[] = new String[] { "Tab1" };

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int i) {
        Log.d("PagerAdapater:", "getItem() called for item:" + i);
        return new LineFragment();

    }



    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


}
