package com.codepath.apps.twitterapp.fragments;

import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;

import rx.Observable;

public class HomeTimelineFragment extends TimelineFragment {

    @Override
    public Observable<Tweet> fetch(TimelineRequest request) {
        return client.getHomeTimeline(request);
    }

}
