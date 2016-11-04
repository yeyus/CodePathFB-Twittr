package com.codepath.apps.twitterapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.adapters.TweetsAdapter;
import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;
import com.codepath.apps.twitterapp.thirdparty.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class TimelineFragment extends Fragment {

    public static final String TAG = TimelineFragment.class.getSimpleName();

    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.rvTweets) RecyclerView rvTweets;

    private final PublishSubject<Tweet> replyClickSubject = PublishSubject.create();
    private final PublishSubject<Tweet> tweetClickSubject = PublishSubject.create();

    protected TwitterClient client;

    private List<Tweet> mTimelineTweets;
    private TweetsAdapter tweetsAdapter;
    private TimelineRequest lastRequest;
    private View rootView;

    public TimelineFragment() {
        client = TwitterApplication.getRestClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();

        // First fragment request
        lastRequest = new TimelineRequest.Builder()
                .count(25)
                .sinceId(1)
                .maxId(-1)
                .build();

        if (TwitterApplication.isNetworkAvailable()) {
            requestTimeline(lastRequest);
        } else {
            // Go to persistence
            for (Tweet t : Tweet.getLastTweets(50)) {
                addTweet(t);
            }
        }
    }

    private void setupRecyclerView() {
        mTimelineTweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(getContext(), mTimelineTweets);
        rvTweets.setAdapter(tweetsAdapter);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvTweets.setLayoutManager(layout);
        rvTweets.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        // Infinite scroll
        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layout) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i(TAG, "Asking for on scroll refresh");
                requestTimeline(new TimelineRequest.Builder(lastRequest)
                        .maxId(mTimelineTweets.get(mTimelineTweets.size()-1).getUid() - 1)
                        .sinceId(1)
                        .build());
            }
        });
    }

    private void setupListeners() {
        tweetsAdapter.getReplyClickSubject().subscribe(tweet -> replyClickSubject.onNext(tweet));

        tweetsAdapter.getTweetClickSubject().subscribe(tweet -> tweetClickSubject.onNext(tweet));

        // Pull to refresh
        swipeContainer.setOnRefreshListener(() -> {
            requestTimeline(new TimelineRequest.Builder(lastRequest)
                    .sinceId(mTimelineTweets.isEmpty() ?
                            1 : mTimelineTweets.get(0).getUid())
                    .maxId(-1)
                    .build());
        });
    }

    public boolean requestTimeline(TimelineRequest request) {
        lastRequest = request;

        if (!TwitterApplication.isNetworkAvailable()) {
            Snackbar.make(rootView, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, view -> requestTimeline(lastRequest))
                    .show();
            return false;
        }

        fetch(request)
                .subscribe(
                        tweet -> {
                            Log.i(TAG, String.format("got tweet with UID %d", tweet.getUid()));
                            tweet.persist();
                            addTweet(tweet);
                        },
                        throwable -> Log.e(TAG, "unable to process tweet", throwable),
                        () -> {
                            swipeContainer.setRefreshing(false);
                            Log.i(TAG, "all tweets were processed");
                        }
                );

        return true;
    }

    public abstract Observable<Tweet> fetch(TimelineRequest request);

    public Observable<Tweet> getOnReplyObservable() {
        return replyClickSubject;
    }

    public Observable<Tweet> getOnTweetClickObservable() {
        return tweetClickSubject;
    }

    public void addTweet(Tweet t) {
        if (mTimelineTweets.isEmpty()) {
            appendTweet(t);
        } else if (t.getUid() > mTimelineTweets.get(0).getUid()) {
            prependTweet(t);
        } else if (t.getUid() <= mTimelineTweets.get(mTimelineTweets.size() - 1).getUid()) {
            appendTweet(t);
        }
    }

    private void appendTweet(Tweet t) {
        mTimelineTweets.add(t);
        tweetsAdapter.notifyItemInserted(mTimelineTweets.size());
    }

    private void prependTweet(Tweet t) {
        // Prepend to list and scroll
        mTimelineTweets.add(0, t);
        tweetsAdapter.notifyItemInserted(0);
        rvTweets.scrollToPosition(0);
    }
}
