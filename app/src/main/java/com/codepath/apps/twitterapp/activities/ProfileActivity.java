package com.codepath.apps.twitterapp.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.databinding.ActivityProfileBinding;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.fragments.ProfileTimelineFragment;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = ProfileActivity.class.getSimpleName();

    private User user;
    private ActivityProfileBinding binding;
    private ProfileTimelineFragment timelineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        binding.setUser(user);

        timelineFragment = ProfileTimelineFragment.newInstance(user);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, timelineFragment);
        ft.commit();

        setupListeners();
    }

    private void setupListeners() {
        timelineFragment.getOnTweetClickObservable()
                .subscribe(tweet -> {
                    Intent i = new Intent(ProfileActivity.this, TweetActivity.class);
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    startActivity(i);
                });

        timelineFragment.getOnReplyObservable()
                .subscribe(str -> openComposeDialog(ComposeTweetDialogFragment.newInstance(str)));
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fragment.getPostSubject()
                .flatMap(str -> TwitterApplication.getRestClient().postTweet(str))
                .subscribe(
                        tweet -> Log.i(TAG, "tweet posted"),
                        throwable -> {
                            Snackbar.make(binding.getRoot(), R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
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
}
