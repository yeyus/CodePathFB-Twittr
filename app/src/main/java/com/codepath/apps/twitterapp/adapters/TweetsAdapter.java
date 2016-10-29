package com.codepath.apps.twitterapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.databinding.ItemTweetBinding;
import com.codepath.apps.twitterapp.models.Tweet;

import java.util.List;

import rx.subjects.PublishSubject;

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
        holder.binding.tvBody.setOnClickListener(view -> tweetClickSubject.onNext(tweet));
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }
}
