package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.codepath.apps.twitterapp.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.adapters.TweetsAdapter;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subjects.PublishSubject;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = TweetsAdapter.class.getSimpleName();

    private TwitterClient client;
    private final PublishSubject<TimelineRequest> timelineRequestSubject = PublishSubject.create();

    private View rootView;

    @BindView(R.id.rvTweets) RecyclerView rvTweets;
    @BindView(R.id.fabCompose) FloatingActionButton fabCompose;

    private List<Tweet> mTimelineTweets;
    private TweetsAdapter tweetsAdapter;
    private TimelineRequest lastRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.activity_timeline, null);
        setContentView(rootView);
        ButterKnife.bind(this);

        client = TwitterApplication.getRestClient();

        setupRecyclerView();

        // What to do when a request is received
        timelineRequestSubject
                .distinct()
                .flatMap(request -> client.getHomeTimeline(request))
                .subscribe(
                        tweet -> {
                            mTimelineTweets.add(tweet);
                            tweetsAdapter.notifyItemInserted(mTimelineTweets.size());
                        },
                        throwable -> Log.e(TAG, "unable to process tweet", throwable),
                        () -> Log.i(TAG, "all tweets were processed")
                );

        lastRequest = new TimelineRequest.Builder()
                .count(25)
                .sinceId(1)
                .maxId(-1)
                .build();
        timelineRequestSubject.onNext(lastRequest);
    }

    private void setupRecyclerView() {
        mTimelineTweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, mTimelineTweets);
        rvTweets.setAdapter(tweetsAdapter);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvTweets.setLayoutManager(layout);
        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(layout) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                timelineRequestSubject.onNext(
                        new TimelineRequest.Builder(lastRequest)
                                .maxId(mTimelineTweets.get(mTimelineTweets.size()-1).getUid())
                                .build()

                );
            }
        });
    }


    @OnClick(R.id.fabCompose)
    public void composeClick() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetDialogFragment editNameDialogFragment = ComposeTweetDialogFragment.newInstance(null);
        editNameDialogFragment.getPostSubject()
                .flatMap(str -> client.postTweet(str))
                .subscribe(
                        tweet -> {
                            // Prepend to list and scroll
                            mTimelineTweets.add(0, tweet);
                            tweetsAdapter.notifyItemInserted(0);
                            rvTweets.scrollToPosition(0);
                        },
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
}
