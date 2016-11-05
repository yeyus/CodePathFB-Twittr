package com.codepath.apps.twitterapp.fragments;

import android.os.Bundle;

import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;

import rx.Observable;

public class SearchTimelineFragment extends TimelineFragment {

    public static SearchTimelineFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString("query", query);

        SearchTimelineFragment fragment = new SearchTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Observable<Tweet> fetch(TimelineRequest request) {
        return client.getSearchTimeline(getArguments().getString("query"), request);
    }
}
