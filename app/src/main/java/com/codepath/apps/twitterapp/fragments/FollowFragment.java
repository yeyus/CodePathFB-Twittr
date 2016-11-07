package com.codepath.apps.twitterapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.adapters.UsersAdapter;
import com.codepath.apps.twitterapp.api.TwitterClient;
import com.codepath.apps.twitterapp.models.User;
import com.codepath.apps.twitterapp.thirdparty.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterapp.thirdparty.SimpleDividerItemDecoration;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

public class FollowFragment extends Fragment {

    public static final String TAG = FollowFragment.class.getSimpleName();

    @BindView(R.id.rvFollow) RecyclerView rvFollow;

    protected TwitterClient client;

    private List<User> mUsers;
    private UsersAdapter usersAdapter;
    private View rootView;

    private User user;
    private boolean following;

    public static FollowFragment newInstance(User user, boolean showFollowing) {

        Bundle args = new Bundle();
        args.putBoolean("following", true);
        args.putParcelable("user", Parcels.wrap(user));
        FollowFragment fragment = new FollowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FollowFragment() {
        client = TwitterApplication.getRestClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_follow, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        following = getArguments().getBoolean("following", false);
        user = Parcels.unwrap(getArguments().getParcelable("user"));

        requestFollows(user, -1);
    }

    private void setupRecyclerView() {
        mUsers = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), mUsers);
        rvFollow.setAdapter(usersAdapter);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvFollow.setLayoutManager(layout);
        rvFollow.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        // Infinite scroll
        rvFollow.addOnScrollListener(new EndlessRecyclerViewScrollListener(layout) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i(TAG, "Asking for on scroll refresh");
                requestFollows(user, mUsers.get(mUsers.size() - 1).getUid());
            }
        });
    }

    private void requestFollows(User pUser, long cursor) {
        fetch(pUser, cursor)
                .subscribe(
                        user -> {
                            user.save();
                            appendUser(user);
                        }
                );
    }

    private Observable<User> fetch(User user, long cursor) {
        if (following) {
            return client.getFollowing(user, cursor, 50);
        } else {
            return client.getFollowers(user, cursor, 50);
        }
    }

    public void appendUser(User u) {
        mUsers.add(u);
        usersAdapter.notifyItemInserted(mUsers.size());
    }
}
