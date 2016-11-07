package com.codepath.apps.simpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.TwitterApplication;
import com.codepath.apps.simpletweets.TwitterClient;
import com.codepath.apps.simpletweets.adapters.DividerItemDecoration;
import com.codepath.apps.simpletweets.adapters.RecyclerViewTweetsArrayAdapter;
import com.codepath.apps.simpletweets.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.simpletweets.models.Tweet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.loopj.android.http.TextHttpResponseHandler;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by mkhade on 11/2/2016.
 */

public abstract class TweetsListFragment extends Fragment {

    public static final String TAG="TweetsListFragment";
    public TwitterClient client;

    public abstract void populateTimeline(boolean isScrolled, boolean isRefreshed);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();
        //getFromDB(); //Debugging
    }
}