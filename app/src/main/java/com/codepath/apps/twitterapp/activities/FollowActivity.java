package com.codepath.apps.twitterapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.fragments.FollowFragment;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.codepath.apps.twitterapp.R.id.vpPager;

public class FollowActivity extends AppCompatActivity {

    public static final String TAG = FollowActivity.class.getSimpleName();

    private User user;

    private View rootView;
    private FollowViewsAdapter adapterViewPager;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.appBar) AppBarLayout appBar;
    @BindView(vpPager) ViewPager vpPages;
    @BindView(R.id.tlTabs) TabLayout tlTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        rootView = getLayoutInflater().inflate(R.layout.activity_follow, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);


        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        getSupportActionBar().setTitle(user.getName());
        
        setupViewPager();
    }

    private void setupViewPager() {
        adapterViewPager = new FollowActivity.FollowViewsAdapter(getSupportFragmentManager(), this, user);
        vpPages.setAdapter(adapterViewPager);

        tlTabs.setupWithViewPager(vpPages, true);
    }

    public static class FollowViewsAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        Context context;
        User user;

        public FollowViewsAdapter(FragmentManager fragmentManager,
                               Context context, User user) {
            super(fragmentManager);
            this.context = context;
            this.fragments = new ArrayList<>();
            this.user = user;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return 2;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Fragment f = null;
            switch (position) {
                case 0:
                    f = FollowFragment.newInstance(user, false);
                    break;
                case 1:
                    f = FollowFragment.newInstance(user, true);
                    break;
                default:
                    f = null;
            }
            return f;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            switch (position) {
                case 0:
                    title =context.getResources().getString(R.string.followers);
                    break;
                case 1:
                    title = context.getResources().getString(R.string.following);
                    break;
                default:
                    title = "Unknown";
            }

            return title;
        }

    }
}
