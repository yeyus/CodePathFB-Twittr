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

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = TweetsAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTweetBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ItemTweetBinding.bind(itemView);
        }
    }

    private List<Tweet> mTweets;
    private Context mContext;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        mTweets = tweets;
        mContext = context;
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
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }
}
