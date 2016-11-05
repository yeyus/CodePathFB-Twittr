package com.codepath.apps.twitterapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.fragments.SearchTimelineFragment;

import org.parceler.Parcels;

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();

    private String query;
    private SearchTimelineFragment searchFragment;
    private ViewGroup viewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        
        query = getIntent().getStringExtra("query");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(query);

        searchFragment = SearchTimelineFragment.newInstance(query);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, searchFragment);
        ft.commit();

        setupListeners();
    }

    private void setupListeners() {
        searchFragment.getOnTweetClickObservable()
                .subscribe(tweet -> {
                    Intent i = new Intent(SearchActivity.this, TweetActivity.class);
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    startActivity(i);
                });

        searchFragment.getOnReplyObservable()
                .subscribe(str -> openComposeDialog(ComposeTweetDialogFragment.newInstance(str)));
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fragment.getPostSubject()
                .flatMap(str -> TwitterApplication.getRestClient().postTweet(str))
                .subscribe(
                        tweet -> Log.i(TAG, "tweet posted"),
                        throwable -> {
                            Snackbar.make(viewGroup, R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
