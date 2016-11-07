package com.codepath.apps.simpletweets.adapters;

import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.simpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.simpletweets.fragments.MentionsTimelineFragment;
import com.codepath.apps.simpletweets.utils.Constants;

/**
 * Created by mkhade on 11/4/2016.
 */

public class TweetsPagerAdapter extends FragmentPagerAdapter{

    private String tabTitles[] = {"Home", "Mentions"};

    //adapter gets manager with which it inserts or removes fragments
    public TweetsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //order and creation of fragments within the pager (only one at a time)
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new HomeTimelineFragment();
        } else if (position == 1) {
            return new MentionsTimelineFragment();
        } else {
            return null;
        }
    }

    //how many gpages
    @Override
    public int getCount() {
        return Constants.PAGE_COUNT;
    }

    //return tab title
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
