package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
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

        fragmentTweet.getReplyClickSubject()
                .subscribe(tweet -> openComposeDialog(
                        ComposeTweetDialogFragment.newInstance(tweet)));
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


}
