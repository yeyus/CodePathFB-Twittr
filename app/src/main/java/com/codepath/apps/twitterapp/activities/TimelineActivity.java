package com.codepath.apps.twitterapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.api.TwitterClient;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterapp.fragments.MentionsTimelineFragment;
import com.codepath.apps.twitterapp.fragments.ProfileFragment;
import com.codepath.apps.twitterapp.models.Tweet;
import com.codepath.apps.twitterapp.models.User;
import com.codepath.apps.twitterapp.utils.NetworkState;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

import static com.codepath.apps.twitterapp.R.id.vpPager;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = TimelineActivity.class.getSimpleName();

    private TwitterClient client;
    private User user;

    private View rootView;
    private TimelineAdapter adapterViewPager;
    private HomeTimelineFragment homeTimelineFragment;
    private MentionsTimelineFragment mentionsTimelineFragment;
    private ProfileFragment profileFragment;

    @BindView(R.id.fabCompose) FloatingActionButton fabCompose;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appBar) AppBarLayout appBar;
    @BindView(vpPager) ViewPager vpPages;
    @BindView(R.id.tlTabs) TabLayout tlTabs;
    private MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        rootView = getLayoutInflater().inflate(R.layout.activity_timeline, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient();

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

        client.getAccount().subscribe(user -> {
            profileFragment = ProfileFragment.newInstance(user);
            adapterViewPager.addTab(profileFragment);
            adapterViewPager.notifyDataSetChanged();
        });

        setupViewPager();
        setupListeners();

        // Handling receiving share intents
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

                // Make sure to check whether returned data will be null.
                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);
                composeFromIntent(titleOfPage, urlOfPage);
            }
        }
    }

    private void setupViewPager() {
        homeTimelineFragment = new HomeTimelineFragment();
        mentionsTimelineFragment = new MentionsTimelineFragment();
        profileFragment = ProfileFragment.newInstance(user);
        adapterViewPager = new TimelineAdapter(getSupportFragmentManager(), this);
        adapterViewPager.addTab(homeTimelineFragment);
        adapterViewPager.addTab(mentionsTimelineFragment);
        adapterViewPager.notifyDataSetChanged();
        vpPages.setAdapter(adapterViewPager);

        tlTabs.setupWithViewPager(vpPages, true);
    }


    private void setupListeners() {
        Observable.merge(
                homeTimelineFragment.getOnReplyObservable(),
                mentionsTimelineFragment.getOnReplyObservable()
            ).subscribe(
                tweet -> openComposeDialog(ComposeTweetDialogFragment.newInstance(tweet))
            );

        Observable.merge(
                homeTimelineFragment.getOnTweetClickObservable(),
                mentionsTimelineFragment.getOnTweetClickObservable()
            ).subscribe(
                tweet -> {
                    Intent i = new Intent(TimelineActivity.this, TweetActivity.class);
                    i.putExtra("tweet", Parcels.wrap(tweet));
                    startActivity(i);
                }
            );

        Observable.merge(
                homeTimelineFragment.getRetweetClickSubject(),
                mentionsTimelineFragment.getRetweetClickSubject()
            ).flatMap(tweet ->
                tweet.getRetweeted() ? client.postRetweet(tweet) : client.destroyRetweet(tweet)
            ).retry().repeat().subscribe(
                tweet -> addTweet(tweet),
                throwable ->
                    Snackbar.make(rootView, R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
                            .show()
            );

        Observable.merge(
                homeTimelineFragment.getFavoriteClickSubject(),
                mentionsTimelineFragment.getFavoriteClickSubject()
            ).flatMap(tweet ->
                    tweet.getFavorited() ? client.postFavorite(tweet) : client.destroyFavorite(tweet)
            ).retry().repeat().subscribe(
                tweet -> {},
                throwable -> Snackbar.make(rootView, R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
                        .show()
            );

        Observable.merge(
                homeTimelineFragment.getProfileClickObservable(),
                mentionsTimelineFragment.getProfileClickObservable()
            ).subscribe(
                user -> {
                    Intent i = new Intent(TimelineActivity.this, ProfileActivity.class);
                    i.putExtra("user", Parcels.wrap(user));
                    startActivity(i);
                }
            );
    }

    private void addTweet(Tweet t) {
        homeTimelineFragment.addTweet(t);
    }

    private void openComposeDialog(ComposeTweetDialogFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fragment.getPostSubject()
                .flatMap(str -> client.postTweet(str))
                .subscribe(
                        tweet -> addTweet(tweet),
                        throwable -> {
                            Snackbar.make(rootView, R.string.tweet_posting_error, Snackbar.LENGTH_LONG)
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

    @OnClick(R.id.fabCompose)
    public void composeFromClick() {
        openComposeDialog(ComposeTweetDialogFragment.newInstance());
    }

    public void composeFromIntent(String title, String text) {
        StringBuffer body = new StringBuffer();
        body.append(title);
        body.append(" - ");
        body.append(text);
        openComposeDialog(
                ComposeTweetDialogFragment.newInstance(
                    body.substring(0, Math.min(140, body.length()))
                )
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timeline, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                Intent i = new Intent(TimelineActivity.this, SearchActivity.class);
                i.putExtra("query", queryString);
                startActivity(i);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public static class TimelineAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        Context context;

        public TimelineAdapter(FragmentManager fragmentManager,
                               Context context) {
            super(fragmentManager);
            this.context = context;
            this.fragments = new ArrayList<>();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return fragments.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            Fragment f = fragments.get(position);
            if (f instanceof HomeTimelineFragment) {
                return context.getResources().getString(R.string.tab_timeline);
            } else if (f instanceof MentionsTimelineFragment) {
                return context.getResources().getString(R.string.tab_mentions);
            } else if (f instanceof ProfileFragment) {
                return context.getResources().getString(R.string.tab_me);
            }

            return "Unknown";
        }

        public void addTab(Fragment tab) {
            fragments.add(tab);
        }

    }
}
