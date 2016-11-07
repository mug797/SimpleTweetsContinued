package com.codepath.apps.simpletweets.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.simpletweets.MyDatabase;
import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.TwitterApplication;
import com.codepath.apps.simpletweets.TwitterClient;
import com.codepath.apps.simpletweets.adapters.TweetsPagerAdapter;
import com.codepath.apps.simpletweets.fragments.ComposeFragment;
import com.codepath.apps.simpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.simpletweets.fragments.MentionsTimelineFragment;
import com.codepath.apps.simpletweets.fragments.TweetsListFragment;
import com.codepath.apps.simpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.simpletweets.models.Owner;
import com.codepath.apps.simpletweets.models.Tweet;
import com.codepath.apps.simpletweets.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.loopj.android.http.TextHttpResponseHandler;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.BindView;
import cz.msebera.android.httpclient.Header;

import static com.codepath.apps.simpletweets.R.id.miActionProgress;

public class TimelineActivity extends ActionBarActivity implements ComposeFragment.ComposedTweetListener{

    public static final String TAG="TimelineActivity";
    public static User owner;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.slidingTabs) PagerSlidingTabStrip tabStrip;

    MenuItem miActionProgressItem;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialSetup();
    }

    public void setUpPager(){
        //getup viewpager, bindview
        // set the viewpager adapter for the pager,
        viewPager.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));
        // find the pager sliding tabsstrips, attach the pager tabsstrips to the viewpager
        tabStrip.setViewPager(viewPager);
    }

    public void initialSetup(){
        setContentView(R.layout.activity_timeline);
        client = TwitterApplication.getRestClient();
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setLogo(R.drawable.twitter_logo);
        setUpPager();
        if(checkConnectivityAndLaunch()==true) {
            getOwnerDetails();
        }
    }

    public boolean checkConnectivityAndLaunch(){
        if (!isNetworkAvailable() || !isOnline()) {
            Toast.makeText(getApplicationContext(), "Network Problem: You do not have a working internet connection at this moment", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

/* //Access fragment
    public void getFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            tweetsListFragment = (TweetsListFragment) getSupportFragmentManager().findFragmentById(R.id.);
    }
*/

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //TODO find out if this could be made static?
    public void viewClickedUserProfile(View view) {
        Intent i = new Intent(this, ProfileActivity.class);
        Log.i(TAG, "Clicked: " + (String)view.getTag());
        i.putExtra("screen_name", (String)view.getTag());
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.miComposeTweet:
                triggerComposeTweet();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void triggerComposeTweet() {
        if (owner != null && !TextUtils.isEmpty(owner.getProfileImageUrl())) {
            getComposeFragment(owner.getProfileImageUrl());
        }
    }

    void getComposeFragment(String userProfileUrl){
        ComposeFragment composeFragment = ComposeFragment.newInstance(userProfileUrl);
        composeFragment.show(getSupportFragmentManager(), "composetweet");
    }

    void getOwnerDetails(){
       // client.getUserTimeline(new TextHttpResponseHandler() {
        client.getOwnerInfo(new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("ERROR", "triggerComposTweet : "  + statusCode + " : " + throwable.getMessage());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if(responseString == null) {
                    Log.e("ERROR", "returned response is null");
                    //should compose tweet frag not be calleD?
                    return;
                }
                try {
                    Gson gson = new GsonBuilder().create();
                    JsonObject jsonObject = gson.fromJson(responseString, JsonObject.class);
                    if (jsonObject != null) {
                        owner = User.fromJson(jsonObject);
                    }
                } catch (JsonParseException e){
                    Log.e(TAG, "Parse: " + e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DEBUGG", "delete older enteries and update the db.");
        //deleteFromDB();
        //addToDB();
    }

/*    public void getFromDB() {
        List<User> usersFromDB = SQLite.select().from(User.class).queryList();
        List<Tweet> tweetsFromDB = SQLite.select().from(Tweet.class).queryList();

        Map<Long, User> userMap = new HashMap<>();

        if (usersFromDB != null) {
            for (User user : usersFromDB) {
                userMap.put(user.getUid(), user);
            }
        }

        if (tweetsFromDB != null) {
            for (Tweet tweet : tweetsFromDB) {
                if (userMap.containsKey(tweet.getUser_id())) {
                    tweet.setUser(userMap.get(tweet.getUser_id()));
                }
                tweets.add(tweet);
            }
            aTweets.notifyDataSetChanged();
        }
    }

    public voProid deleteFromDB() {
        Delete.tables(User.class, Tweet.class);
    }

    public void addToDB(){
        List<User> usersFromDB = SQLite.select().from(User.class).queryList();
        Set<String> userIds = new HashSet<>();

        for (User user: usersFromDB) {
            userIds.add(user.getUid_string());
        }

        for (Tweet tweet : tweets) {
            if (!userIds.contains(tweet.getUser().getUid_string())) {
                tweet.getUser().save();
                userIds.add(tweet.getUser().getUid_string());
            }
            tweet.save();
        }
    }

    public void addToDB_notWorking(){
        List<User> usersFromDB = SQLite.select().from(User.class).queryList();

        FlowManager.getDatabase(MyDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
                        new ProcessModelTransaction.ProcessModel<User>() {
                            @Override
                            public void processModel(User u) {
                                List<User> usersFromDB = SQLite.select().from(User.class).queryList();
                                Set<String> userIds = new HashSet<>();

                                for (User user: usersFromDB) {
                                    userIds.add(user.getUid_string());
                                }

                                for (Tweet tweet : tweets) {
                                    if (!userIds.contains(tweet.getUser().getUid_string())) {
                                        tweet.getUser().save();
                                        userIds.add(tweet.getUser().getUid_string());
                                    }
                                    tweet.save();
                                }
                            }
                        }).addAll().build())  // add elements (can also handle multiple)
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable error) {

                    }
                })
                .success(new Transaction.Success() {
                    @Override
                    public void onSuccess(Transaction transaction) {

                    }
                }).build().execute();
    }*/

    public void onProfileView(MenuItem mi){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    @Override
    public void onComposedTweet(Tweet composeTweet) {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        HomeTimelineFragment homeTimelineFragment = null;
        MentionsTimelineFragment mentionsTimelineFragment = null;

        for (Fragment f : fragmentList) {
            if (f instanceof HomeTimelineFragment) {
                homeTimelineFragment = (HomeTimelineFragment)f;
                addToHomeTimeline(homeTimelineFragment,composeTweet);
                continue;
            }
            if (f instanceof MentionsTimelineFragment) {
                mentionsTimelineFragment = (MentionsTimelineFragment)f;
                addToMentionsTimeline(mentionsTimelineFragment, composeTweet);
                continue;
            }
        }
    }

    public void addToHomeTimeline(HomeTimelineFragment homeTimelineFragment, Tweet composeTweet){
        if (homeTimelineFragment != null) {
            homeTimelineFragment.tweets.addFirst(composeTweet);
            homeTimelineFragment.aTweets.notifyItemRangeInserted(0,
                    1);
            homeTimelineFragment.layoutManager.scrollToPosition(0);
        }
    }
    public void addToMentionsTimeline(MentionsTimelineFragment mentionsTimelineFragment, Tweet composeTweet){
        if (mentionsTimelineFragment != null) {
            mentionsTimelineFragment.tweets.addFirst(composeTweet);
            mentionsTimelineFragment.aTweets.notifyItemRangeInserted(0,
                    1);
            mentionsTimelineFragment.layoutManager.scrollToPosition(0);
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }
}