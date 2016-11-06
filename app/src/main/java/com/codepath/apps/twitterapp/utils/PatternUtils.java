package com.codepath.apps.twitterapp.utils;

import android.widget.TextView;

import com.codepath.apps.twitterapp.thirdparty.PatternEditableBuilder;

import java.util.regex.Pattern;

import rx.Observable;

/**
 * Created by jesusft on 11/5/16.
 */

public enum PatternUtils {
    INSTANCE;

    public Observable<String> getTweetPattern(TextView textView, int color) {
        return Observable.create(subscriber -> {
            new PatternEditableBuilder().
                    addPattern(Pattern.compile("\\@(\\w+)"), color,
                            text -> {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onNext(text);
                                    subscriber.onCompleted();
                                }
                            })
                    .addPattern(Pattern.compile("\\#(\\w+)"), color,
                            text -> {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onNext(text);
                                    subscriber.onCompleted();
                                }
                            })
                    .into(textView);
        });
    }

}
