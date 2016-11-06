package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.api.TwitterClient;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.fragments.TweetFragment;
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

public class TweetActivity extends AppCompatActivity {

    private TwitterClient client;

    private TweetFragment fragmentTweet;

    private Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        client = TwitterApplication.getRestClient();
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        boolean showMedia = getIntent().getBooleanExtra("show_media", true);

        fragmentTweet = TweetFragment.newInstance(tweet, showMedia, true);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentTweet, fragmentTweet);
        ft.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        fragmentTweet.getReplyClickSubject()
                .subscribe(tweet -> openComposeDialog(
                        ComposeTweetDialogFragment.newInstance(tweet)));

        fragmentTweet.getFavoriteClickSubject()
                .map(tweet ->
                    tweet.getFavorited() ? client.postFavorite(tweet) : client.destroyFavorite(tweet)
                ).subscribe(tweet -> {});

        fragmentTweet.getRetweetClickSubject()
                .map(tweet ->
                    tweet.getRetweeted() ? client.postRetweet(tweet) : client.destroyRetweet(tweet)
                ).subscribe(tweet -> {});
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fragment.getPostSubject()
                .flatMap(str -> client.postTweet(str))
                .subscribe(
                        tweet -> finish()
                );
        fragment.show(fm, "fragment_compose_tweet");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
