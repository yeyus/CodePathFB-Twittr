package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.fragments.TweetFragment;
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

public class TweetActivity extends AppCompatActivity {

    private TweetFragment fragmentTweet;

    private Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));

        fragmentTweet = TweetFragment.newInstance(tweet);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentTweet, fragmentTweet);
        ft.commit();
    }


}
