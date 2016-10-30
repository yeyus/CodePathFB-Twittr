package com.codepath.apps.twitterapp.models;

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

@Table(database = MyDatabase.class)
@Parcel(analyze = {MediaEntity.class})
public class MediaEntity extends BaseModel {

    @PrimaryKey
    @Column
    Long id;

    @Column
    Long tweetUid;

    @Column
    String displayUrl;

    @Column
    String expandedUrl;

    @Column
    String mediaUrl;

    @Column
    String type;

    public MediaEntity() {}

    public Long getId() {
        return id;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getType() {
        return type;
    }

    public static MediaEntity fromJSON(JSONObject jsonObject) {
        MediaEntity me = new MediaEntity();

        try {
            me.id = jsonObject.getLong("id");
            me.displayUrl = jsonObject.getString("display_url");
            me.expandedUrl = jsonObject.getString("expanded_url");
            me.mediaUrl = jsonObject.getString("media_url");
            me.type = jsonObject.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return me;
    }

    public static ArrayList<MediaEntity> fromJSONArray(JSONArray jsonArray) {
        ArrayList<MediaEntity> mes = new ArrayList<MediaEntity>();

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                mes.add(MediaEntity.fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return mes;
    }
}
