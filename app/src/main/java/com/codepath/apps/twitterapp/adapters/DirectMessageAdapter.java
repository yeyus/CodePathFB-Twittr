package com.codepath.apps.twitterapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.databinding.ItemDmBinding;
import com.codepath.apps.twitterapp.models.DirectMessage;
import com.codepath.apps.twitterapp.utils.PatternUtils;

import java.util.List;

import rx.subjects.PublishSubject;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class DirectMessageAdapter extends RecyclerView.Adapter<DirectMessageAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemDmBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ItemDmBinding.bind(itemView);
        }
    }

    public static final String TAG = DirectMessageAdapter.class.getSimpleName();
    private final PublishSubject<String> spannableClickSubject = PublishSubject.create();

    private List<DirectMessage> mMessages;
    private Context mContext;

    public DirectMessageAdapter(Context context, List<DirectMessage> dms) {
        mMessages = dms;
        mContext = context;
    }

    public PublishSubject<String> getSpannableClickSubject() {
        return spannableClickSubject;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View dmView = inflater.inflate(R.layout.item_dm, parent, false);
        ViewHolder vh = new ViewHolder(dmView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DirectMessage dm = mMessages.get(position);

        holder.binding.setDm(dm);
        holder.binding.executePendingBindings();

        PatternUtils.INSTANCE.getTweetPattern(holder.binding.tvBody,
                getContext().getResources().getColor(R.color.twitter_blue))
                .repeat()
                .subscribe(str -> spannableClickSubject.onNext(str));
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
