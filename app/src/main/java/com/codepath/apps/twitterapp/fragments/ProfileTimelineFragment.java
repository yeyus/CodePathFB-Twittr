package com.codepath.apps.twitterapp.fragments;

import android.os.Bundle;

import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

import rx.Observable;

/**
 * Created by jesusft on 11/3/16.
 */

public class ProfileTimelineFragment extends TimelineFragment {

    public static ProfileTimelineFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));

        ProfileTimelineFragment fragment = new ProfileTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Observable<Tweet> fetch(TimelineRequest request) {
        User user = Parcels.unwrap(getArguments().getParcelable("user"));
        return client.getUserTimeline(user.getScreenName(), request);
    }
}
