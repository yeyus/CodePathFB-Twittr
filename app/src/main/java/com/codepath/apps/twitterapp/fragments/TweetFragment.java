package com.codepath.apps.twitterapp.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.databinding.ItemTweetBinding;
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

import rx.subjects.PublishSubject;

public class TweetFragment extends Fragment {

    private Tweet tweet;
    private boolean showMedia;
    private boolean showActions;
    private ItemTweetBinding binding;

    public static final String TAG = TweetFragment.class.getSimpleName();
    private final PublishSubject<Tweet> replyClickSubject = PublishSubject.create();

    public TweetFragment() {}

    public PublishSubject<Tweet> getReplyClickSubject() {
        return replyClickSubject;
    }

    public static TweetFragment newInstance(Tweet tweet, boolean showMedia, boolean showActions) {
        TweetFragment f = new TweetFragment();
        Bundle args = new Bundle();
        args.putParcelable("tweet", Parcels.wrap(tweet));
        args.putBoolean("show_media", showMedia);
        args.putBoolean("show_actions", showActions);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.item_tweet, container, false);
        final View v = binding.getRoot();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
        showMedia = getArguments().getBoolean("show_media");
        showActions = getArguments().getBoolean("show_actions");
        binding.setTweet(tweet);
        binding.executePendingBindings();

        binding.btnReply.setOnClickListener(v -> {
            replyClickSubject.onNext(tweet);
        });

        if (!showMedia) {
            binding.ivMedia.setVisibility(View.GONE);
        }

        if (!showActions) {
            binding.llActions.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        replyClickSubject.onCompleted();
        super.onDetach();
    }
}
