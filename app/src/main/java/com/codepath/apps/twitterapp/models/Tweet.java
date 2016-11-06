package com.codepath.apps.twitterapp.models;

import android.text.format.DateUtils;

import com.codepath.apps.twitterapp.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
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

import static android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE;

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
    Boolean retweeted;

    @Column
    Integer retweetCount = 0;

    @Column
    Boolean favorited;

    @Column
    Integer favouritesCount = 0;

    @Column
    Boolean isRetweet;

    @Column
    String retweetedBy;


    List<MediaEntity> media;

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

    public Boolean getRetweeted() {
        return retweeted;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public void setRetweeted(Boolean retweeted) {
        this.retweeted = retweeted;
        this.retweetCount = retweeted ? this.retweetCount + 1 : this.retweetCount - 1;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
        this.favouritesCount = favorited ? this.favouritesCount + 1 : this.favouritesCount - 1;
    }

    public String getRelativeCreatedAt() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(createdAt).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, FORMAT_ABBREV_RELATIVE).toString();
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

    public String getAnyMedia() {
        String url = null;
        if (media != null && !media.isEmpty()) {
            url = media.get(0).getMediaUrl();
        }

        return url;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "media")
    public List<MediaEntity> getMedia() {
        if (media == null || media.isEmpty()) {
            media = SQLite.select()
                    .from(MediaEntity.class)
                    .where(MediaEntity_Table.tweetUid.eq(uid))
                    .queryList();
        }
        return media;
    }

    public Boolean isRetweet() {
        return isRetweet;
    }

    public String getRetweetedBy() {
        return retweetedBy;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tw = new Tweet();

        try {
            tw.uid = jsonObject.getLong("id");
            if(!jsonObject.isNull("retweeted_status")) {
                tw.retweetedBy = User.fromJSON(jsonObject.getJSONObject("user")).getName();
                jsonObject = jsonObject.getJSONObject("retweeted_status");
                tw.isRetweet = true;
            } else {
                tw.isRetweet = false;
                tw.retweetedBy = "No one";
            }

            tw.body = jsonObject.getString("text");
            tw.createdAt = jsonObject.getString("created_at");
            tw.user = User.fromJSON(jsonObject.getJSONObject("user"));
            tw.retweeted = jsonObject.getBoolean("retweeted");
            if(!jsonObject.isNull("retweet_count")) {
                tw.retweetCount = jsonObject.getInt("retweet_count");
            }
            tw.favorited = jsonObject.getBoolean("favorited");
            if(!jsonObject.isNull("favourites_count")) {
                tw.favouritesCount = jsonObject.getInt("favourites_count");
            }
            if(!jsonObject.isNull("entities")) {
                JSONObject entitiesObj = jsonObject.getJSONObject("entities");
                if(!entitiesObj.isNull("media")) {
                    List<MediaEntity> media = MediaEntity.fromJSONArray(entitiesObj.getJSONArray("media"));
                    for (MediaEntity m : media) {
                        m.tweetUid = tw.getUid();
                    }
                    tw.media = media;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tw;
    }

    public void persist() {
        this.getUser().save();
        if (!this.getMedia().isEmpty()) {
            for(MediaEntity m: this.getMedia()) {
                m.save();
            }
        }
        this.save();
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