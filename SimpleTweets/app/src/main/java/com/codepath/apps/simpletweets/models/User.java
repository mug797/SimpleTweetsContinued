package com.codepath.apps.simpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.codepath.apps.simpletweets.MyDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.config.FlowLog.Level.D;

/**
 * Created by mkhade on 10/26/2016.
 */

@Table(database = MyDatabase.class, name="Users")
public class User extends BaseModel { //implements Parcelable{

    @Column
    private String name;

    @PrimaryKey
    @Column
    private long uid;

    @Column
    private String screenName;

    @Column
    private String profileImageUrl;

    @Column
    private String uid_string;

    @Column
    private String followersCount;

    @Column
    private String description;

    @Column
    private String friendsCount;

    public static User fromJson(JsonObject jsonObject) {

        User user = new User();
        try {
            user.name = jsonObject.get("name").getAsString();
            user.uid = jsonObject.get("id").getAsLong();
            user.screenName = jsonObject.get("screen_name").getAsString();
            if (jsonObject.has("profile_image_url")) { user.profileImageUrl = jsonObject.get("profile_image_url").getAsString(); }
            if (jsonObject.has("description")) { user.setDescription(jsonObject.get("description").getAsString()); }
            if (jsonObject.has("followers_count")) { user.setFollowersCount(jsonObject.get("followers_count").getAsString()); }
            if (jsonObject.has("friends_count")) { user.setFriendsCount(jsonObject.get("friends_count").getAsString()); }
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return user;
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setUid_string(String uid_string) {
        this.uid_string = uid_string;
    }

    public String getUid_string() { return uid_string; }

    public String getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(String followersCount) {
        this.followersCount = followersCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(String friendsCount) {
        this.friendsCount = friendsCount;
    }

/*    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getUid_string());
        dest.writeString(this.getName());
        dest.writeString(this.getScreenName());
        dest.writeString(this.getProfileImageUrl());
        dest.writeString(this.getDescription());
        dest.writeString(this.getFollowersCount());
        dest.writeString(this.getFriendsCount());
    }

    protected User (Parcel in) {
        this.setUid_string(in.readString());
        this.setName(in.readString()));
        this.setScreenName(in.readString());
        this.setProfileImageUrl(in.readString());
        this.setDescription(in.readString());
        this.setFollowersCount(in.readString());
        this.setFriendsCount(in.readString());
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };*/
}
