package com.codepath.apps.twitterapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterApplication;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.twitterapp.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterapp.fragments.MentionsTimelineFragment;
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

import static com.codepath.apps.twitterapp.R.id.vpPager;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = TimelineActivity.class.getSimpleName();

    private TwitterClient client;

    private View rootView;
    private FragmentPagerAdapter adapterViewPager;
    private HomeTimelineFragment homeTimelineFragment;
    private MentionsTimelineFragment mentionsTimelineFragment;

    @BindView(R.id.fabCompose) FloatingActionButton fabCompose;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appBar) AppBarLayout appBar;
    @BindView(vpPager) ViewPager vpPages;
    @BindView(R.id.pager_header) PagerTabStrip ptsHeader;

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
        adapterViewPager = new TimelineAdapter(getSupportFragmentManager(), this, homeTimelineFragment, mentionsTimelineFragment);
        vpPages.setAdapter(adapterViewPager);

        ptsHeader.setDrawFullUnderline(true);
        ptsHeader.setTabIndicatorColor(getResources().getColor(R.color.twitter_blue));
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

    public static class TimelineAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        Context context;
        HomeTimelineFragment homeTimelineFragment;
        MentionsTimelineFragment mentionsTimelineFragment;

        public TimelineAdapter(FragmentManager fragmentManager, Context context, HomeTimelineFragment homeTimeline, MentionsTimelineFragment mentionsTimeline) {
            super(fragmentManager);
            this.context = context;
            this.homeTimelineFragment = homeTimeline;
            this.mentionsTimelineFragment = mentionsTimeline;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return homeTimelineFragment;
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return mentionsTimelineFragment;
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getResources().getString(R.string.tab_timeline);
                case 1:
                    return context.getString(R.string.tab_mentions);
                default:
                    return null;
            }
        }

    }
}
