package com.codepath.apps.twitterapp.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.twitterapp.R;
import com.codepath.apps.twitterapp.TwitterClient;
import com.codepath.apps.twitterapp.models.Tweet;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

public class ComposeTweetDialogFragment extends DialogFragment {

    private TwitterClient client;
    private Tweet inReplyTo;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tvInReplyTo) TextView tvInReplyTo;
    @BindView(R.id.etBody) EditText etBody;
    MenuItem txtCharCount;

    private final PublishSubject<String> postSubject = PublishSubject.create();

    public ComposeTweetDialogFragment() {}

    public PublishSubject<String> getPostSubject() {
        return postSubject;
    }

    public static ComposeTweetDialogFragment newInstance(@Nullable Tweet tweet) {
        ComposeTweetDialogFragment f = new ComposeTweetDialogFragment();
        Bundle args = new Bundle();
        // TODO set instance
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_compose_tweet, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.compose_dialog);
        toolbar.setTitle(R.string.compose_tweet_title);
        txtCharCount = toolbar.getMenu().findItem(R.id.txtCharsLeft);

        tvInReplyTo.setVisibility(inReplyTo == null ? View.GONE : View.VISIBLE);

        setupListeners();
    }

    private void setupListeners() {
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_send:
                    sendTweet();
            }
            return true;
        });

        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                int maxLength = getResources().getInteger(R.integer.tweet_max_length);
                if(txtCharCount != null) {
                    txtCharCount.setTitle(Integer.toString(maxLength - editable.length()));
                }
            }
        });
    }

    @Override
    public void onDetach() {
        postSubject.onCompleted();
        super.onDetach();
    }

    private void sendTweet() {
        postSubject.onNext(etBody.getText().toString());
        postSubject.onCompleted();
    }
}
