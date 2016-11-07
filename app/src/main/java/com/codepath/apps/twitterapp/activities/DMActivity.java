package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.adapters.DirectMessageAdapter;
import com.codepath.apps.twitterapp.api.TwitterClient;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.models.DirectMessage;
import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.thirdparty.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterapp.thirdparty.SimpleDividerItemDecoration;
import com.codepath.apps.twitterapp.utils.NetworkState;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class DMActivity extends AppCompatActivity {

    public static final String TAG = DMActivity.class.getSimpleName();

    private TwitterClient client;
    private TimelineRequest lastRequest;

    private View rootView;
    private List<DirectMessage> mMessages;
    private DirectMessageAdapter adapterMessages;

    @BindView(R.id.fabCompose) FloatingActionButton fabCompose;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appBar) AppBarLayout appBar;
    @BindView(R.id.rvMessages) RecyclerView rvMessages;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    private MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = getLayoutInflater().inflate(R.layout.activity_dm, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();

        setupRecyclerView();

        // Network status handler
        client.getRequestSubject().repeat().subscribe(
                networkState -> {
                    if (miActionProgressItem != null) {
                        if (networkState == NetworkState.LOADING) {
                            miActionProgressItem.setVisible(true);
                        } else {
                            miActionProgressItem.setVisible(false);
                        }
                    }
                });

        lastRequest = new TimelineRequest.Builder()
                .count(25)
                .sinceId(1)
                .maxId(-1)
                .build();

        if (TwitterApplication.isNetworkAvailable()) {
            requestMessages(lastRequest);
        } else {
            // Go to persistence
            for (DirectMessage t : DirectMessage.getLastDirectMessages(50)) {
                addDM(t);
            }
        }
    }

    private void setupRecyclerView() {
        mMessages = new ArrayList<>();
        adapterMessages = new DirectMessageAdapter(this, mMessages);
        rvMessages.setAdapter(adapterMessages);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvMessages.setLayoutManager(layout);
        rvMessages.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        rvMessages.addOnScrollListener(new EndlessRecyclerViewScrollListener(layout) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i(TAG, "Asking for on scroll refresh");
                requestMessages(new TimelineRequest.Builder(lastRequest)
                        .maxId(mMessages.get(mMessages.size()-1).getUid() - 1)
                        .sinceId(1)
                        .build());
            }
        });
    }

    private boolean requestMessages(TimelineRequest request) {
        lastRequest = request;

        if (!TwitterApplication.isNetworkAvailable()) {
            Snackbar.make(rootView, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, view -> requestMessages(lastRequest))
                    .show();
            return false;
        }

        client.getDirectMessages(request)
                .subscribe(
                        dm -> {
                            Log.i(TAG, String.format("got dm with UID %d", dm.getUid()));
                            dm.persist();
                            addDM(dm);
                        },
                        throwable -> Log.e(TAG, "unable to process dm", throwable),
                        () -> {
                            swipeContainer.setRefreshing(false);
                            Log.i(TAG, "all tweets were processed");
                        }
                );

        return true;
    }

    public void addDM(DirectMessage t) {
        if (mMessages.isEmpty()) {
            appendDM(t);
        } else if (t.getUid() > mMessages.get(0).getUid()) {
            prependDM(t);
        } else if (t.getUid() <= mMessages.get(mMessages.size() - 1).getUid()) {
            appendDM(t);
        }
    }

    private void appendDM(DirectMessage t) {
        mMessages.add(t);
        adapterMessages.notifyItemInserted(mMessages.size());
    }

    private void prependDM(DirectMessage t) {
        // Prepend to list and scroll
        mMessages.add(0, t);
        adapterMessages.notifyItemInserted(0);
        rvMessages.scrollToPosition(0);
    }

    @OnClick(R.id.fabCompose)
    public void composeFromClick() {
        openComposeDialog(ComposeTweetDialogFragment.newInstance());
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fragment.getPostSubject()
                .flatMap(str -> client.postTweet(str))
                .subscribe(
                        dm -> {},
                        throwable -> {
                            Snackbar.make(rootView, R.string.dm_posting_error, Snackbar.LENGTH_LONG)
                                    .show();
                            Log.e(TAG, "dm posting error", throwable);
                        },
                        () -> {
                            fragment.dismiss();
                            Log.i(TAG, "dm posting dismissed");
                        }
                );
        fragment.show(fm, "fragment_compose_tweet");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messages, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        return super.onCreateOptionsMenu(menu);
    }
}
