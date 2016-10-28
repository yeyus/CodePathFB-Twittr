package com.codepath.apps.twitterapp.models;

import android.text.format.DateUtils;

import com.codepath.apps.twitterapp.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Table(database = MyDatabase.class)
@Parcel(analyze={Tweet.class})
public class Tweet extends BaseModel {

    @PrimaryKey
    @Column
    Long uid;

    @Column
    String body;

    @ForeignKey(tableClass = User.class)
    @Column
    User user;

    @Column
    String createdAt;

    @Column
    Integer retweetCount = 0;

    @Column
    Integer favouritesCount = 0;

    public Tweet() { super(); }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public String getRelativeCreatedAt() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(createdAt).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public String getFormattedCreatedAt() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String strDate = "";
        try {
            Date date = sf.parse(createdAt);
            strDate = DateFormat.getDateTimeInstance().format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return strDate;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tw = new Tweet();

        try {
            tw.body = jsonObject.getString("text");
            tw.uid = jsonObject.getLong("id");
            tw.createdAt = jsonObject.getString("created_at");
            tw.user = User.fromJSON(jsonObject.getJSONObject("user"));
            if(!jsonObject.isNull("retweet_count")) {
                tw.retweetCount = jsonObject.getInt("retweet_count");
            }
            if(!jsonObject.isNull("favourites_count")) {
                tw.favouritesCount = jsonObject.getInt("favourites_count");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tw;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>();

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                tweets.add(Tweet.fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return tweets;
    }

    public static List<Tweet> getLastTweets(int number) {
        return SQLite.select()
                .from(Tweet.class)
                .orderBy(Tweet_Table.uid, false)
                .limit(number)
                .queryList();
    }
}