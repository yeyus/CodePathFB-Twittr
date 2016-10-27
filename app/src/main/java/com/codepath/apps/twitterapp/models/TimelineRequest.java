package com.codepath.apps.twitterapp.models;

public class TimelineRequest {

    private final int count;
    private final long sinceId;
    private final long maxId;

    public int getCount() {
        return count;
    }

    public long getSinceId() {
        return sinceId;
    }

    public long getMaxId() {
        return maxId;
    }

    public static class Builder {
        private int count = 25;
        private long sinceId = 1;
        private long maxId = -1;

        public Builder() {}

        public Builder(TimelineRequest request) {
            this.count = request.count;
            this.maxId = request.maxId;
            this.sinceId = request.sinceId;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder sinceId(long sinceId) {
            this.sinceId = sinceId;
            return this;
        }

        public Builder maxId(long maxId) {
            this.maxId = maxId;
            return this;
        }

        public TimelineRequest build() {
            return new TimelineRequest(this);
        }
    }

    public TimelineRequest(TimelineRequest.Builder builder) {
        this.count = builder.count;
        this.sinceId = builder.sinceId;
        this.maxId = builder.maxId;
    }
}
