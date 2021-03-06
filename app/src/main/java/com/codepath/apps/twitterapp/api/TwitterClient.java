package com.codepath.apps.twitterapp.api;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.twitterapp.models.DirectMessage;
import com.codepath.apps.twitterapp.models.TimelineRequest;
import com.codepath.apps.twitterapp.models.Tweet;
import com.codepath.apps.twitterapp.models.User;
import com.codepath.apps.twitterapp.utils.NetworkState;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter_blue.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {

	public static final String TAG = TwitterClient.class.getSimpleName();

	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "R37ou6CF9ybUZijkCvxj2oxdl";       // Change this
	public static final String REST_CONSUMER_SECRET = "UDKmTrxGXyKE2x2B3qfSserRr0UZNnZ8bd7mkHTfK3zW5rrVCy"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://tweetsappcallback"; // Change this (here and in manifest)

    private final PublishSubject<NetworkState> requestSubject = PublishSubject.create();

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

    public PublishSubject<NetworkState> getRequestSubject() {
        return requestSubject;
    }

    // region Home Timeline
	public void getHomeTimeline(int count, long since_id, long max_id, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", String.valueOf(count));
		if (since_id > 0) {
			params.put("since_id", String.valueOf(since_id));
		}
		if(max_id > 0) {
			params.put("max_id", String.valueOf(max_id));
		}
		getClient().get(apiUrl, params, handler);
	}

    public Observable<Tweet> getHomeTimeline(TimelineRequest request) {
        return wrapCall(Observable.create(subscriber -> {
            getHomeTimeline(
                    request.getCount(),
                    request.getSinceId(),
                    request.getMaxId(),
                    getTimelineHandler(subscriber)
            );
        }));
    }
    // endregion

    // region Mentions Timeline
	public void getMentionsTimeline(int count, long since_id, long max_id, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/mentions_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", String.valueOf(count));
		if (since_id > 0) {
			params.put("since_id", String.valueOf(since_id));
		}
		if(max_id > 0) {
			params.put("max_id", String.valueOf(max_id));
		}
		getClient().get(apiUrl, params, handler);
	}

    public Observable<Tweet> getMentionsTimeline(TimelineRequest request) {
        return wrapCall(Observable.create(subscriber -> {
            getMentionsTimeline(
                    request.getCount(),
                    request.getSinceId(),
                    request.getMaxId(),
                    getTimelineHandler(subscriber)
            );
        }));
    }
    // endregion

    // region User Timeline
	public void getUserTimeline(String screenName, int count, long since_id, long max_id, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", String.valueOf(count));
		if (since_id > 0) {
			params.put("since_id", String.valueOf(since_id));
		}
		if(max_id > 0) {
			params.put("max_id", String.valueOf(max_id));
		}
		if(screenName != null) {
			params.put("screen_name", screenName);
		}
		getClient().get(apiUrl, params, handler);
	}

    public Observable<Tweet> getUserTimeline(String screenName, TimelineRequest request) {
        return wrapCall(Observable.create(subscriber -> {
            getUserTimeline(
                    screenName,
                    request.getCount(),
                    request.getSinceId(),
                    request.getMaxId(),
                    getTimelineHandler(subscriber)
            );
        }));
    }
    // endregion

    // region Post Tweet
	public void postTweet(String body, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", body);
		getClient().post(apiUrl, params, handler);
	}

    public Observable<Tweet> postTweet(String body) {
        return Observable.create(subscriber -> {
            postTweet(body, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(Tweet.fromJSON(response));
                        subscriber.onCompleted();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(throwable);
                    }
                }
            });
        });
    }
    // endregion

    // region Get Account
	public void getAccount(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("account/verify_credentials.json");
		getClient().get(apiUrl, null, handler);
	}

    public Observable<User> getAccount() {
        return Observable.create(subscriber -> {
            getAccount(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(User.fromJSON(response));
                        subscriber.onCompleted();
                    }
                }


                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(throwable);
                    }
                }
            });
        });
    }
    // endregion

    // region Search Timeline

    public void getSearchTimeline(String query, int count, long since_id, long max_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("search/tweets.json");
        RequestParams params = new RequestParams();
        params.put("count", String.valueOf(count));
        if (since_id > 0) {
            params.put("since_id", String.valueOf(since_id));
        }
        if(max_id > 0) {
            params.put("max_id", String.valueOf(max_id));
        }
        if(query != null) {
            params.put("q", query);
        }
        getClient().get(apiUrl, params, handler);
    }

    public Observable<Tweet> getSearchTimeline(String query, TimelineRequest request) {
        return wrapCall(Observable.create(subscriber -> {
            getSearchTimeline(
                    query,
                    request.getCount(),
                    request.getSinceId(),
                    request.getMaxId(),
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (!subscriber.isUnsubscribed()) {
                                try {
                                    JSONArray statuses = response.getJSONArray("statuses");
                                    ArrayList<Tweet> tweets = Tweet.fromJSONArray(statuses);
                                    Log.i(TAG, String.format("Received %d tweets", tweets.size()));
                                    for (Tweet t: tweets) {
                                        subscriber.onNext(t);
                                    }
                                    Log.i(TAG, String.format("Closing connection and observable"));
                                } catch (JSONException e) {
                                    subscriber.onError(e);
                                } finally {
                                    subscriber.onCompleted();
                                }
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(throwable);
                            }
                        }
                    }
            );
        }));
    }

    // endregion

    // region Favorite
    public void postFavorite(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/create.json");
        RequestParams params = new RequestParams();
        params.put("id", tweet.getUid());
        getClient().post(apiUrl, params, handler);
    }

    public Observable<Tweet> postFavorite(Tweet tweet) {
        return Observable.create(subscriber -> {
           postFavorite(tweet, getTweetHandler(subscriber));
        });
    }

    public void destroyFavorite(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/destroy.json");
        RequestParams params = new RequestParams();
        params.put("id", tweet.getUid());
        getClient().post(apiUrl, params, handler);
    }

    public Observable<Tweet> destroyFavorite(Tweet tweet) {
        return Observable.create(subscriber -> {
            destroyFavorite(tweet, getTweetHandler(subscriber));
        });
    }
    // endregion

    // region ReTweet
    public void postRetweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(String.format("statuses/retweet/%d.json", tweet.getUid()));
        getClient().post(apiUrl, null, handler);
    }

    public Observable<Tweet> postRetweet(Tweet tweet) {
        return Observable.create(subscriber -> {
            postRetweet(tweet, getTweetHandler(subscriber));
        });
    }

    public void destroyRetweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(String.format("statuses/unretweet/%d.json", tweet.getUid()));
        getClient().post(apiUrl, null, handler);
    }

    public Observable<Tweet> destroyRetweet(Tweet tweet) {
        return Observable.create(subscriber -> {
            destroyRetweet(tweet, getTweetHandler(subscriber));
        });
    }
    // endregion

    // region Direct Messages
    public void getDirectMessages(int count, long since_id, long max_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("direct_messages.json");
        RequestParams params = new RequestParams();
        params.put("count", String.valueOf(count));
        if (since_id > 0) {
            params.put("since_id", String.valueOf(since_id));
        }
        if(max_id > 0) {
            params.put("max_id", String.valueOf(max_id));
        }
        getClient().get(apiUrl, params, handler);
    }

    public Observable<DirectMessage> getDirectMessages(TimelineRequest request) {
        requestSubject.onNext(NetworkState.LOADING);
        Observable<DirectMessage> obs = Observable.create(subscriber -> {
            getDirectMessages(
                    request.getCount(),
                    request.getSinceId(),
                    request.getMaxId(),
                    getDirectMessagesHandler(subscriber));
        });
        obs.doOnCompleted(() -> requestSubject.onNext(NetworkState.STALE));
        return obs;
    }
    // endregion

    // region Followers
    public void getFollowers(User user, long cursor, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("followers/list.json");
        RequestParams params = new RequestParams();
        params.put("count", String.valueOf(count));
        params.put("cursor", String.valueOf(cursor));
        params.put("user_id", String.valueOf(user.getUid()));
        getClient().get(apiUrl, params, handler);
    }

    public Observable<User> getFollowers(User user, long cursor, int count) {
        return Observable.create(subscriber -> getFollowers(user, cursor, count, getUsersHandler(subscriber)));
    }

    public void getFollowing(User user, long cursor, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friends/list.json");
        RequestParams params = new RequestParams();
        params.put("count", String.valueOf(count));
        params.put("cursor", String.valueOf(cursor));
        params.put("user_id", String.valueOf(user.getUid()));
        getClient().get(apiUrl, params, handler);
    }

    public Observable<User> getFollowing(User user, long cursor, int count) {
        return Observable.create(subscriber -> getFollowing(user, cursor, count, getUsersHandler(subscriber)));
    }
    // endregion

    // region JSON Handlers
    private JsonHttpResponseHandler getTimelineHandler(Subscriber<? super Tweet> subscriber) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (!subscriber.isUnsubscribed()) {
                    ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                    Log.i(TAG, String.format("Received %d tweets", tweets.size()));
                    for (Tweet t: tweets) {
                        subscriber.onNext(t);
                    }
                    Log.i(TAG, String.format("Closing connection and observable"));
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(throwable);
                }
            }
        };
    }

    private JsonHttpResponseHandler getTweetHandler(Subscriber<? super Tweet> subscriber) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(Tweet.fromJSON(response));
                    Log.i(TAG, String.format("Closing connection and observable"));
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(throwable);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(throwable);
                }
            }
        };
    }

    private JsonHttpResponseHandler getDirectMessagesHandler(Subscriber<? super DirectMessage> subscriber) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (!subscriber.isUnsubscribed()) {
                    List<DirectMessage> dms = DirectMessage.fromJSONArray(response);
                    Log.i(TAG, String.format("Received %d direct messages", dms.size()));
                    for (DirectMessage t: dms) {
                        subscriber.onNext(t);
                    }
                    Log.i(TAG, String.format("Closing connection and observable"));
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(throwable);
                }
            }
        };
    }

    private JsonHttpResponseHandler getUsersHandler(Subscriber<? super User> subscriber) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (!subscriber.isUnsubscribed()) {
                    List<User> users = null;
                    try {
                        users = User.fromJSONArray(response.getJSONArray("users"));
                        Log.i(TAG, String.format("Received %d users", users.size()));
                        for (User u: users) {
                            subscriber.onNext(u);
                        }
                        Log.i(TAG, String.format("Closing connection and observable"));
                        subscriber.onCompleted();
                    } catch (JSONException e) {
                        subscriber.onError(e);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(throwable);
                }
            }
        };
    }

    private Observable<Tweet> wrapCall(Observable<Tweet> original) {
        requestSubject.onNext(NetworkState.LOADING);
        return original.doOnCompleted(() -> requestSubject.onNext(NetworkState.STALE));
    }
    // endregion

}