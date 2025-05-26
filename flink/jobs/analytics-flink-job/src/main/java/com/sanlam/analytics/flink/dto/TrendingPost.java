package com.sanlam.analytics.flink.dto;

public class TrendingPost {
    private String content;
    private long count;

    public TrendingPost() {}

    public TrendingPost(String content, long count) {
        this.content = content;
        this.count = count;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "TrendingPost{" +
                "content='" + content + '\'' +
                ", count=" + count +
                '}';
    }
}