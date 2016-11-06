package com.codepath.apps.twitterapp.utils;

import com.loopj.android.http.JsonHttpResponseHandler;

import rx.subjects.PublishSubject;

/**
 * Created by jesusft on 11/6/16.
 */

public class ProgressJsonHttpResponseHandler extends JsonHttpResponseHandler {

    private final PublishSubject<NetworkState> requestSubject = PublishSubject.create();

    public PublishSubject<NetworkState> getRequestSubject() {
        return requestSubject;
    }

    @Override
    public void onStart() {
        super.onStart();
        requestSubject.onNext(NetworkState.LOADING);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        requestSubject.onNext(NetworkState.STALE);
    }
}
