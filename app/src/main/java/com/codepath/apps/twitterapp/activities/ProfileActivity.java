package com.codepath.apps.twitterapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.fragments.ProfileFragment;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = ProfileActivity.class.getSimpleName();

    private User user;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        profileFragment = ProfileFragment.newInstance(user);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flProfile, profileFragment);
        ft.commit();
    }

}
