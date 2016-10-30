package com.codepath.apps.twitterapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.codepath.apps.twitterapp.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.adapters.TweetsAdapter;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = TimelineActivity.class.getSimpleName();

    private TwitterClient client;

    private View rootView;

    @BindView(R.id.rvTweets) RecyclerView rvTweets;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.fabCompose) FloatingActionButton fabCompose;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appBar) AppBarLayout appBar;

    private List<Tweet> mTimelineTweets;
    private TweetsAdapter tweetsAdapter;
    private TimelineRequest lastRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        rootView = getLayoutInflater().inflate(R.layout.activity_timeline, null);
        setContentView(rootView);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();

        setupRecyclerView();
        setupListeners();

        // First request
        lastRequest = new TimelineRequest.Builder()
                .count(25)
                .sinceId(1)
                .maxId(-1)
                .build();
        if (!requestTimeline(lastRequest)) {
            // if offline we populate from db
            for (Tweet t : Tweet.getLastTweets(50)) {
                addTweet(t);
            }
        }
    }

    private void setupRecyclerView() {
        mTimelineTweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, mTimelineTweets);
        rvTweets.setAdapter(tweetsAdapter);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvTweets.setLayoutManager(layout);

        // Infinite scroll
        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layout) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                requestTimeline(new TimelineRequest.Builder(lastRequest)
                        .maxId(mTimelineTweets.get(mTimelineTweets.size()-1).getUid())
                        .build());
            }
        });
    }

    private void setupListeners() {
        // Reply click
        tweetsAdapter.getReplyClickSubject().subscribe(
                tweet -> openComposeDialog(tweet)
        );

        // Open detail click
        tweetsAdapter.getTweetClickSubject().subscribe(
                tweet -> {
                    Intent i = new Intent(TimelineActivity.this, TweetActivity.class);
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    startActivity(i);
                }
        );

        // Pull to refresh
        swipeContainer.setOnRefreshListener(() -> {
            requestTimeline(new TimelineRequest.Builder(lastRequest)
                .sinceId(mTimelineTweets.isEmpty() ?
                        1 : mTimelineTweets.get(0).getUid())
                .maxId(-1)
                .build());
        });
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean requestTimeline(TimelineRequest request) {
        setSupportProgressBarIndeterminateVisibility(true);

        lastRequest = request;
        // Check for internet connection
        if (!isNetworkAvailable()) {
            Snackbar.make(rootView, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, view -> requestTimeline(lastRequest))
                    .show();
            return false;
        }

        client.getHomeTimeline(request)
                .subscribe(
                        tweet -> {
                            tweet.persist();
                            addTweet(tweet);
                        },
                        throwable -> Log.e(TAG, "unable to process tweet", throwable),
                        () -> {
                            swipeContainer.setRefreshing(false);
                            setSupportProgressBarIndeterminateVisibility(false);
                            Log.i(TAG, "all tweets were processed");
                        }
                );

        return true;
    }

    private void addTweet(Tweet t) {
        if (mTimelineTweets.isEmpty()) {
            appendTweet(t);
        } else if (t.getUid() > mTimelineTweets.get(0).getUid()) {
            prependTweet(t);
        } else if (t.getUid() < mTimelineTweets.get(mTimelineTweets.size() - 1).getUid()) {
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

    private void openComposeDialog(Tweet t) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetDialogFragment editNameDialogFragment = ComposeTweetDialogFragment.newInstance(t);
        editNameDialogFragment.getPostSubject()
                .flatMap(str -> client.postTweet(str))
                .subscribe(
                        tweet -> addTweet(tweet),
                        throwable -> {
                            Snackbar.make(rootView, R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
                                    .show();
                            Log.e(TAG, "tweet posting error", throwable);
                        },
                        () -> {
                            editNameDialogFragment.dismiss();
                            Log.i(TAG, "tweet posting dismissed");
                        }
                );
        editNameDialogFragment.show(fm, "fragment_compose_tweet");
    }

    @OnClick(R.id.fabCompose)
    public void composeClick() {
        openComposeDialog(null);
    }
}
