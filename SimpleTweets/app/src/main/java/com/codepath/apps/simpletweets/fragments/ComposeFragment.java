package com.codepath.apps.simpletweets.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.TwitterApplication;
import com.codepath.apps.simpletweets.TwitterClient;
import com.codepath.apps.simpletweets.activities.TimelineActivity;
import com.codepath.apps.simpletweets.models.Tweet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ComposeFragment extends DialogFragment {

    @BindView(R.id.btCancel)Button btCanel;
    @BindView(R.id.btTweet)Button btTweet;
    @BindView(R.id.etCompose) TextView etCompose;
    @BindView(R.id.ivComposeProfileImage) ImageView ivComposeProfileImage;
    @BindView(R.id.etCharCount) TextView tvCharCount;

    private TwitterClient twitterClient;

    // Define the listener of the interface type
    // listener will the activity instance containing fragment
    private ComposedTweetListener composedTweetListener;

    // Define the events that the fragment will use to communicate
    public interface ComposedTweetListener {
        // This can be any number of events to be sent to the activity
        public void onComposedTweet(Tweet composeTweet );
    }

    public static ComposeFragment newInstance(String userProfileUrl) {
        ComposeFragment composeFragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("user_profile_url", userProfileUrl);
        composeFragment.setArguments(args);
        return composeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compose, container, false);
        ButterKnife.bind(this, rootView);
        twitterClient = TwitterApplication.getRestClient();
        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

/*
        if (TimelineActivity.owner != null && !TextUtils.isEmpty(TimelineActivity.owner.getOwnerProfileImageUrl())) {
            Glide.with(getContext()).load(TimelineActivity.owner.getOwnerProfileImageUrl())
                    .bitmapTransform(new RoundedCornersTransformation(getContext(), 10, 0))
                    .override(150, 150)
                    .placeholder(R.drawable.ic_nocover)
                    .fitCenter()
                    .into(ivComposeProfileImage);
        }
*/
        String userProfileUrl = getArguments().getString("user_profile_url");
        if (!TextUtils.isEmpty(userProfileUrl)) {
            Glide.with(getContext()).load(userProfileUrl)
                    .bitmapTransform(new RoundedCornersTransformation(getContext(), 10, 0))
                    .override(150, 150)
                    .placeholder(R.drawable.ic_nocover)
                    .fitCenter()
                    .into(ivComposeProfileImage);
        }

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setTextColor(Color.GRAY);
                int tweetLength = 0;
                if (s.length() >= 120 && s.length() <= 140) {
                    tvCharCount.setTextColor(Color.RED);
                    tweetLength = 140 - s.length();
                    btTweet.setEnabled(true);
                } else if (s.length() > 140) {
                    tvCharCount.setTextColor(Color.RED);
                    tweetLength = 140 - s.length();
                    btTweet.setEnabled(false);
                } else {
                    tvCharCount.setTextColor(Color.GRAY);
                    tweetLength = s.length();
                    btTweet.setEnabled(true);
                }
                tvCharCount.setText("" + tweetLength);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @OnClick(R.id.btCancel)
    public void onCancel(){
        dismiss();
    }

    @OnClick(R.id.btTweet)
    public void onTweet(){
        final String tweet = etCompose.getText().toString();
        twitterClient.composeTweet(tweet, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("ERROR: ", "onTweet failed status code: " + statusCode + " : " +throwable.getMessage());
                //onTweet();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Tweet ret_tweet = new Tweet();
                if(responseString == null) {
                    Log.d("DEBUG", "returned response is null");
                    dismiss();
                    return;
                }
                Gson gson = new GsonBuilder().create();
                JsonObject jsonObject = gson.fromJson(responseString, JsonObject.class);
                if (jsonObject != null) {
                    ret_tweet = Tweet.fromJSONObject(jsonObject);
                }
                //((TimelineActivity) getActivity()).addNewComposedTweet(ret_tweet);
                //Trying the listerner way, more dynamic, extendible, never use the actual activity name.
                composedTweetListener.onComposedTweet(ret_tweet);
                dismiss();
            }
        });
    }


    // Store the listener (activity) that will have events fired once the fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ComposedTweetListener) {
            composedTweetListener = (ComposedTweetListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement ComposeFragment.OnTweetComposedListener");
        }
    }
}