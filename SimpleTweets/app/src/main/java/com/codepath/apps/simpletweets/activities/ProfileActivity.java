package com.codepath.apps.simpletweets.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.TwitterApplication;
import com.codepath.apps.simpletweets.TwitterClient;
import com.codepath.apps.simpletweets.fragments.ProfileHeaderFragment;
import com.codepath.apps.simpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.simpletweets.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.IOException;

import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import butterknife.BindView;
import cz.msebera.android.httpclient.util.TextUtils;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG="ProfileActivity";
    public TwitterClient client;
    public User newUser;
    public String screenName;

    @BindView(R.id.toolbarProfile) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialSetup(savedInstanceState);
    }

    public void initialSetup(Bundle savedInstanceState){
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        client = TwitterApplication.getRestClient();
        //Get the screen name from activity that launches this
        screenName = getIntent().getStringExtra("screen_name");

        if (checkConnectivityAndLaunch(savedInstanceState) == true){
            if (getIntent().hasExtra("screen_name")) {
                populateUserInfo(savedInstanceState);
            } else {
                populateOwnerInfo(savedInstanceState);
            }
        }
    }

    public boolean checkConnectivityAndLaunch(Bundle savedInstanceState){
        if (!isNetworkAvailable() || !isOnline()) {
            Toast.makeText(getApplicationContext(), "Network Problem: You do not have a working internet connection at this moment", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    void populateUserInfo(final Bundle savedInstanceState) {
        client.getUserInfo(screenName, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.w(TAG, "Failure " + statusCode + " " + throwable.getMessage());
            }

            //get json, deserialise it, create models, load the models into the adapter
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if(responseString == null) {
                    Log.e(TAG, "returned response is null");
                    return;
                }
                try {
                    Gson gson = new GsonBuilder().create();
                    JsonObject jsonUserObject = gson.fromJson(responseString, JsonObject.class);
                    if (jsonUserObject != null) {
                        newUser = User.fromJson(jsonUserObject);
                        if (newUser != null && !TextUtils.isEmpty(newUser.getScreenName())) {
                            getSupportActionBar().setTitle("@" + newUser.getScreenName());
                        }
                        loadPage(savedInstanceState, screenName);
                    }
                } catch (JsonParseException e) {
                    Log.e(TAG, "Parse: " + e.getMessage());
                }
            }
        });
    }

    void viewClickedUserProfile(View v) {}

    void populateOwnerInfo(final Bundle savedInstanceState) {
        client.getOwnerInfo(new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, + statusCode + " : " + throwable.getMessage());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if(responseString == null) {
                    Log.e("ERROR", "returned response is null");
                    return;
                }
                try {
                    Gson gson = new GsonBuilder().create();
                    JsonObject jsonUserObject = gson.fromJson(responseString, JsonObject.class);

                    if (jsonUserObject != null) {
                        newUser = User.fromJson(jsonUserObject);
                        if (newUser != null && !TextUtils.isEmpty(newUser.getScreenName())) {
                            getSupportActionBar().setTitle("@" + newUser.getScreenName());
                        }
                        loadPage(savedInstanceState, screenName);
                    }
                } catch (JsonParseException e) {
                    Log.e(TAG, "Parse: " + e.getMessage());
                }
            }
        });
    }

    void loadPage(Bundle savedInstanceState, String screenName) {
        if (savedInstanceState == null) {
            UserTimelineFragment userTimelineFragment = UserTimelineFragment.newInstance(screenName);
            ProfileHeaderFragment profileHeaderFragment =
                    ProfileHeaderFragment.newInstance(
                            newUser.getProfileImageUrl(),
                            newUser.getName(), newUser.getDescription(),
                            newUser.getFollowersCount(), newUser.getFriendsCount());
            //To dynamically display fragment usertimeline in the activity
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flUserTimelineContainer, userTimelineFragment);
            ft.replace(R.id.rlUserHeader, profileHeaderFragment);
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
}