package com.codepath.apps.twitterapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.databinding.ItemUserBinding;
import com.codepath.apps.twitterapp.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemUserBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ItemUserBinding.bind(itemView);
        }
    }

    public static final String TAG = UsersAdapter.class.getSimpleName();

    private List<User> mUsers;
    private Context mContext;

    public UsersAdapter(Context mContext, List<User> mUsers) {
        this.mUsers = mUsers;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.item_user, parent, false);
        ViewHolder vh = new ViewHolder(userView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User user = mUsers.get(position);

        holder.binding.setUser(user);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
