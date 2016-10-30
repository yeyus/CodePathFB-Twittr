package com.codepath.apps.twitterapp.fragments;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import com.codepath.apps.twitterapp.models.Tweet;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

public class ComposeTweetDialogFragment extends DialogFragment {

    private Tweet inReplyTo;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tvInReplyTo) TextView tvInReplyTo;
    @BindView(R.id.etBody) EditText etBody;
    MenuItem txtCharCount;

    private final PublishSubject<String> postSubject = PublishSubject.create();
    SharedPreferences pref;
    private boolean mTweetSent = false;

    public ComposeTweetDialogFragment() {}

    public PublishSubject<String> getPostSubject() {
        return postSubject;
    }

    public static ComposeTweetDialogFragment newInstance() {
        ComposeTweetDialogFragment f = new ComposeTweetDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("in_reply_to", null);
        f.setArguments(args);
        return f;
    }

    public static ComposeTweetDialogFragment newInstance(@Nullable Tweet tweet) {
        ComposeTweetDialogFragment f = new ComposeTweetDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("in_reply_to", Parcels.wrap(tweet));
        f.setArguments(args);
        return f;
    }

    public static ComposeTweetDialogFragment newInstance(String body) {
        ComposeTweetDialogFragment f = new ComposeTweetDialogFragment();
        Bundle args = new Bundle();
        args.putString("body", body);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());

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

        inReplyTo = Parcels.unwrap(getArguments().getParcelable("in_reply_to"));
        String body = getArguments().getString("body");

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.compose_dialog);
        if (inReplyTo != null) {
            toolbar.setTitle(R.string.reply_tweet_title);
            tvInReplyTo.setText(String.format(
                    getResources().getString(R.string.in_reply_to),
                    inReplyTo.getUser().getName()));
            etBody.setText("@" + inReplyTo.getUser().getName() + " ");
            etBody.setSelection(etBody.getText().length());
        } else {
            toolbar.setTitle(R.string.compose_tweet_title);
        }

        if (body != null) {
            etBody.setText(body);
        }

        txtCharCount = toolbar.getMenu().findItem(R.id.txtCharsLeft);
        etBody.setText(pref.getString("lastTweet", ""));
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
        if (!mTweetSent) {
            SharedPreferences.Editor edit = pref.edit();
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_save)
                    .setMessage(R.string.dialog_save_as_draft)
                    .setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                        edit.putString("lastTweet", etBody.getText().toString());
                        edit.commit();
                    })
                    .setNegativeButton(R.string.dialog_dismiss, (dialogInterface, i) -> {
                        edit.remove("lastTweet");
                        edit.commit();
                    }).show();
        }

        postSubject.onCompleted();
        super.onDetach();
    }

    private void sendTweet() {
        mTweetSent = true;
        postSubject.onNext(etBody.getText().toString());
        postSubject.onCompleted();
    }
}
