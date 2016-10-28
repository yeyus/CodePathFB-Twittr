package com.codepath.apps.twitterapp.models;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.codepath.apps.twitterapp.MyDatabase;
import com.codepath.apps.twitterapp.R;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

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
        Picasso.with(view.getContext())
                .load(imageUrl)
                .transform(new RoundedCornersTransformation(3, 3))
                // TODO find placeholder
                .placeholder(R.drawable.ic_launcher)
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;
    }
}
