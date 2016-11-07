package com.codepath.apps.simpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.codepath.apps.simpletweets.adapters.RecyclerViewUserTimelineArrayAdapter;
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
 * Created by mkhade on 11/5/2016.
 */

public class UserTimelineFragment extends TweetsListFragment {

    public static final String TAG="UserTimelineFragment";
    public LinkedList<Tweet> tweets;
    public RecyclerViewUserTimelineArrayAdapter aTweets;
    @BindView(R.id.rvTweets) RecyclerView rvTweets;
    private LinearLayoutManager layoutManager;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeRefreshLayout;

    public static UserTimelineFragment newInstance(String screen_name) {
        UserTimelineFragment userDemo = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screen_name);
        userDemo.setArguments(args);
        return userDemo;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        ButterKnife.bind(this, view);
        rvTweets.setAdapter(aTweets);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rvTweets.setLayoutManager(layoutManager);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline(false, true);
            }
        });

        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Toast.makeText(getContext(), "OnScroll : loading", Toast.LENGTH_SHORT).show();
                populateTimeline(true, false);
            }
        });

        tweets.clear();
        aTweets.notifyDataSetChanged();
        populateTimeline(false, false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweets = new LinkedList<>();

        aTweets = new RecyclerViewUserTimelineArrayAdapter(getActivity(), tweets);
        // This instantiates DBFlow
        FlowManager.init(new FlowConfig.Builder(getActivity()).build());
        // add for verbose logging
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
    }

    @Override
    public void populateTimeline(final boolean isScrolled, final boolean isRefreshed) {
        long maxid = !tweets.isEmpty() ? Long.parseLong(tweets.getLast().getIdStr()) - 1 : 1;
        long sinceid = !tweets.isEmpty() ? Long.parseLong(tweets.getFirst().getIdStr()) : 1;
        String screen_name = getArguments().getString("screen_name");
        client.getUserTimeline(maxid, sinceid, isScrolled, isRefreshed, screen_name, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, + statusCode + " : " + throwable.getMessage());
            }

            //get json, deserialise it, create models, load the models into the adapter
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                List<Tweet> fetchedTweets = new LinkedList<Tweet>();
                if(response == null) {
                    Log.e("ERROR", "returned response is null");
                    return;
                }
                Gson gson = new GsonBuilder().create();
                JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject jsonTweetObject = jsonArray.get(i).getAsJsonObject();
                        if (jsonTweetObject != null) {
                            fetchedTweets.add(Tweet.fromJSONObject(jsonTweetObject));
                        }
                    }

                    //add to list
                    if (isRefreshed) {
                        for (int i = fetchedTweets.size() - 1; i >= 0; i--) {
                            tweets.addFirst(fetchedTweets.get(i));
                        }
                    } else {
                        tweets.addAll(fetchedTweets);
                    }
                }

                Log.d("DEBUG", tweets.toString());
                if (isScrolled) {
                    aTweets.notifyItemRangeInserted(aTweets.getItemCount(), fetchedTweets.size());
                } else if (isRefreshed) {
                    aTweets.notifyItemRangeInserted(0, fetchedTweets.size());
                    layoutManager.scrollToPosition(0);
                    swipeRefreshLayout.setRefreshing(false); //remove the symbol
                } else {
                    aTweets.notifyDataSetChanged();
                }
            }
        });
    }
}
