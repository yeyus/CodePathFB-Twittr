package com.codepath.apps.twitterapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterapp.fragments.TimelineFragment;
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = TimelineActivity.class.getSimpleName();

    private TwitterClient client;

    private View rootView;

    @BindView(R.id.fabCompose) FloatingActionButton fabCompose;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appBar) AppBarLayout appBar;
    private TimelineFragment mTimelineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        rootView = getLayoutInflater().inflate(R.layout.activity_timeline, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();

        mTimelineFragment = new HomeTimelineFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentTimeline, mTimelineFragment);
        ft.commit();

        setupListeners();

        // Handling receiving share intents
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

                // Make sure to check whether returned data will be null.
                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);
                composeFromIntent(titleOfPage, urlOfPage);
            }
        }
    }


    private void setupListeners() {
        mTimelineFragment.getOnReplyObservable()
                .subscribe(
                        tweet -> openComposeDialog(ComposeTweetDialogFragment.newInstance(tweet))
                );

        mTimelineFragment.getOnTweetClickObservable().subscribe(
                tweet -> {
                    Intent i = new Intent(TimelineActivity.this, TweetActivity.class);
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    startActivity(i);
                }
        );
    }

    private void addTweet(Tweet t) {
        mTimelineFragment.addTweet(t);
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fragment.getPostSubject()
                .flatMap(str -> client.postTweet(str))
                .subscribe(
                        tweet -> addTweet(tweet),
                        throwable -> {
                            Snackbar.make(rootView, R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
                                    .show();
                            Log.e(TAG, "tweet posting error", throwable);
                        },
                        () -> {
                            fragment.dismiss();
                            Log.i(TAG, "tweet posting dismissed");
                        }
                );
        fragment.show(fm, "fragment_compose_tweet");
    }

    @OnClick(R.id.fabCompose)
    public void composeFromClick() {
        openComposeDialog(ComposeTweetDialogFragment.newInstance());
    }

    public void composeFromIntent(String title, String text) {
        StringBuffer body = new StringBuffer();
        body.append(title);
        body.append(" - ");
        body.append(text);
        openComposeDialog(
                ComposeTweetDialogFragment.newInstance(
                    body.substring(0, Math.min(140, body.length()))
                )
        );
    }
}
