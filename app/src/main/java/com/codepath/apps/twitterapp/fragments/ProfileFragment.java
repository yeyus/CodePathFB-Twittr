package com.codepath.apps.twitterapp.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.activities.FollowActivity;
import com.codepath.apps.twitterapp.activities.TweetActivity;
import com.codepath.apps.twitterapp.databinding.FragmentProfileBinding;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private FragmentProfileBinding binding;
    private User user;
    private ProfileTimelineFragment timelineFragment;

    public static ProfileFragment newInstance(User user) {

        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = Parcels.unwrap(getArguments().getParcelable("user"));
        binding.setUser(user);
        binding.executePendingBindings();

        timelineFragment = ProfileTimelineFragment.newInstance(user);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, timelineFragment);
        ft.commit();

        setupListeners();
    }

    private void setupListeners() {
        binding.llFollows.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), FollowActivity.class);
            i.putExtra("user", Parcels.wrap(user));
            startActivity(i);
        });

        timelineFragment.getOnTweetClickObservable()
                .subscribe(tweet -> {
                    Intent i = new Intent(getContext(), TweetActivity.class);
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    startActivity(i);
                });

        timelineFragment.getOnReplyObservable()
                .subscribe(str -> openComposeDialog(ComposeTweetDialogFragment.newInstance(str)));
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getChildFragmentManager();
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
