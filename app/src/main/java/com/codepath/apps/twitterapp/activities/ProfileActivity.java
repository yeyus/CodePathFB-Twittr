package com.codepath.apps.twitterapp.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.databinding.ActivityProfileBinding;
import com.codepath.apps.twitterapp.fragments.ProfileTimelineFragment;
import com.codepath.apps.twitterapp.models.User;

import org.parceler.Parcels;

public class ProfileActivity extends AppCompatActivity {

    private User user;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        binding.setUser(user);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, ProfileTimelineFragment.newInstance(user));
        ft.commit();
    }
}
