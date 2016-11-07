package com.codepath.apps.twitterapp.models;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitterapp.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

@Table(database = MyDatabase.class)
@Parcel(analyze={User.class})
public class User extends BaseModel {

    @Column
    String name;

    @PrimaryKey
    @Column
    Long uid;

    @Column
    String screenName;

    @Column
    String profileImageUrl;

    @Column
    String profileBannerUrl;

    @Column
    Integer followers;

    @Column
    Integer following;

    @Column
    String description;

    public User() { super(); }

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
        return profileImageUrl.replace("_normal", "_bigger");
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public String getDescription() {
        return description;
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(view.getContext(), 5, 5))
                // TODO find placeholder
                .into(view);
    }

    public static User fromJSON(JSONObject jsonObject) {
        User u = new User();

        try {
            u.name = jsonObject.getString("name");
            u.uid = jsonObject.getLong("id");
            u.screenName = jsonObject.getString("screen_name");
            u.profileImageUrl = jsonObject.getString("profile_image_url");
            u.followers = jsonObject.getInt("followers_count");
            u.following = jsonObject.getInt("friends_count");
            u.description = jsonObject.getString("description");
            if (!jsonObject.isNull("profile_banner_url")) {
                u.profileBannerUrl = jsonObject.getString("profile_banner_url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;
    }

    public static ArrayList<User> fromJSONArray(JSONArray jsonArray) {
        ArrayList<User> users = new ArrayList<User>();

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                users.add(User.fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return users;
    }
}
