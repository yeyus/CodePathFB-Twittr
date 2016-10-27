package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.codepath.apps.twitterapp.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.adapters.TweetsAdapter;
import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import rx.Observable;
import rx.subjects.PublishSubject;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = TweetsAdapter.class.getSimpleName();

    private TwitterClient client;
    private final PublishSubject<TimelineRequest> timelineRequestSubject = PublishSubject.create();

    @BindView(R.id.rvTweets) RecyclerView rvTweets;

    private List<Tweet> mTimelineTweets;
    private TweetsAdapter tweetsAdapter;
    private TimelineRequest lastRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        client = TwitterApplication.getRestClient();

        setupRecyclerView();

        // What to do when a request is received
        timelineRequestSubject
                .distinct()
                .flatMap(request -> loadTweets(request))
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

    private Observable<Tweet> loadTweets(TimelineRequest request) {
        return Observable.create(subscriber -> {
            client.getHomeTimeline(
                    request.getCount(),
                    request.getSinceId(),
                    request.getMaxId(),
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            if (!subscriber.isUnsubscribed()) {
                                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                                for (Tweet t: tweets) {
                                    subscriber.onNext(t);
                                }
                                subscriber.onCompleted();
                            }
                        }


                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(throwable);
                            }
                        }
                    }
            );
        });
    }
}
