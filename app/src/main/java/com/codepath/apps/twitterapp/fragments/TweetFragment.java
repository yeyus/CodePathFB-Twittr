package com.codepath.apps.twitterapp.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

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
    private final PublishSubject<Tweet> favoriteClickSubject = PublishSubject.create();
    private final PublishSubject<Tweet> retweetClickSubject = PublishSubject.create();

    public TweetFragment() {}

    public PublishSubject<Tweet> getReplyClickSubject() {
        return replyClickSubject;
    }

    public PublishSubject<Tweet> getFavoriteClickSubject() {
        return favoriteClickSubject;
    }

    public PublishSubject<Tweet> getRetweetClickSubject() {
        return retweetClickSubject;
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
        binding.btnRetweet.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            tweet.setRetweeted(v.isSelected());
            binding.tvRetweetsCount.setText(String.valueOf(tweet.getRetweetCount()));
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
            ((ImageButton)v).setColorFilter(getResources().getColor(
                    v.isSelected() ? R.color.twitter_active_retweet : R.color.twitter_grey));
            retweetClickSubject.onNext(tweet);
        });
        binding.btnFavorite.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            tweet.setFavorited(v.isSelected());
            binding.tvFavCount.setText(String.valueOf(tweet.getFavouritesCount()));
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
            ((ImageButton)v).setColorFilter(getResources().getColor(
                    v.isSelected() ? R.color.twitter_active_fav : R.color.twitter_grey));
            favoriteClickSubject.onNext(tweet);
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
