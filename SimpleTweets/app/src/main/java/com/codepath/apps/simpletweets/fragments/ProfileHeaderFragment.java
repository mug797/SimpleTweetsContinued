package com.codepath.apps.simpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.TwitterApplication;
import com.codepath.apps.simpletweets.TwitterClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by mkhade on 11/5/2016.
 */

public class ProfileHeaderFragment extends Fragment {

    @BindView(R.id.ivUserProfileImage) ImageView ivUserProfileImage;
    @BindView(R.id.tvUserFullName) TextView tvUserFullName;
    @BindView(R.id.tvUserTagLine) TextView tvUserTageLine;
    @BindView(R.id.tvProfileFollowers) TextView tvUserProfileFollowers;
    @BindView(R.id.tvProfileFollowing) TextView tvUserProfileFollowing;

    public static ProfileHeaderFragment newInstance(String profileUrl, String name,
                                               String tagLine,
                                               String followers,
                                               String following) {
        ProfileHeaderFragment profileHeaderFragment = new ProfileHeaderFragment();
        Bundle args = new Bundle();
        args.putString("profile_url", profileUrl);
        args.putString("full_name", name);
        args.putString("tag_line", tagLine);
        args.putString("followers", followers);
        args.putString("following", following);
        profileHeaderFragment.setArguments(args);
        return profileHeaderFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_profile_header, parent, false);
        ButterKnife.bind(this, v);

        tvUserFullName.setText(getArguments().getString("full_name"));
        tvUserTageLine.setText(getArguments().getString("tag_line"));
        tvUserProfileFollowers.setText(getArguments().getString("followers") + " Followers");
        tvUserProfileFollowing.setText(getArguments().getString("following") + " Following");

        Glide.with(getContext()).load(getArguments().getString("profile_url"))
                .placeholder(R.drawable.ic_nocover)
                .bitmapTransform(new RoundedCornersTransformation(getContext(), 10, 0))
                .override(150, 150).into(ivUserProfileImage);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}