package com.codepath.apps.twitterapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.activities.ImageActivity;
import com.codepath.apps.twitterapp.databinding.ItemTweetBinding;
import com.codepath.apps.twitterapp.models.Tweet;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

import java.util.List;

import rx.subjects.PublishSubject;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTweetBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ItemTweetBinding.bind(itemView);
        }
    }

    public static final String TAG = TweetsAdapter.class.getSimpleName();
    private final PublishSubject<Tweet> replyClickSubject = PublishSubject.create();
    private final PublishSubject<Tweet> tweetClickSubject = PublishSubject.create();
    private final PublishSubject<User> profileClickSubject = PublishSubject.create();
    private final PublishSubject<Tweet> favoriteClickSubject = PublishSubject.create();
    private final PublishSubject<Tweet> retweetClickSubject = PublishSubject.create();

    private List<Tweet> mTweets;
    private Context mContext;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        mTweets = tweets;
        mContext = context;
    }

    public PublishSubject<Tweet> getReplyClickSubject() {
        return replyClickSubject;
    }

    public PublishSubject<Tweet> getTweetClickSubject() {
        return tweetClickSubject;
    }

    public PublishSubject<User> getProfileClickSubject() {
        return profileClickSubject;
    }

    public PublishSubject<Tweet> getFavoriteClickSubject() {
        return favoriteClickSubject;
    }

    public PublishSubject<Tweet> getRetweetClickSubject() {
        return retweetClickSubject;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
        ViewHolder vh = new ViewHolder(tweetView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Tweet tweet = mTweets.get(position);

        holder.binding.setTweet(tweet);
        holder.binding.executePendingBindings();
        holder.binding.btnReply.setOnClickListener(view -> {
            replyClickSubject.onNext(tweet);
        });
        holder.binding.btnRetweet.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            tweet.setRetweeted(v.isSelected());
            holder.binding.tvRetweetsCount.setText(String.valueOf(tweet.getRetweetCount()));
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
            ((ImageButton)v).setColorFilter(getContext().getResources().getColor(
                    v.isSelected() ? R.color.twitter_active_retweet : R.color.twitter_grey));
            retweetClickSubject.onNext(tweet);
        });
        holder.binding.btnFavorite.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            tweet.setFavorited(v.isSelected());
            holder.binding.tvFavCount.setText(String.valueOf(tweet.getFavouritesCount()));
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
            ((ImageButton)v).setColorFilter(getContext().getResources().getColor(
                    v.isSelected() ? R.color.twitter_active_fav : R.color.twitter_grey));
            favoriteClickSubject.onNext(tweet);
        });
        holder.binding.tvBody.setOnClickListener(view -> tweetClickSubject.onNext(tweet));
        holder.binding.ivProfileImage.setOnClickListener(view -> profileClickSubject.onNext(tweet.getUser()));
        holder.binding.ivMedia.setOnClickListener(view -> {
            Intent i = new Intent(mContext, ImageActivity.class);
            i.putExtra("tweet", Parcels.wrap(tweet));
            i.putExtra("show_media", false);
            mContext.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }
}
