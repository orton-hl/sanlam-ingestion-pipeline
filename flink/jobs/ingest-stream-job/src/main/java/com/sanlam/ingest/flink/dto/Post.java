package com.sanlam.ingest.flink.dto;

import java.sql.Timestamp;

public class Post {

    private String traceId;
    private String content;
    private String agentMeta;
    private Timestamp date;

    public Post() {}

    public Post(String traceId, String content, String agentMeta, Timestamp date) {
        this.traceId = traceId;
        this.content = content;
        this.agentMeta = agentMeta;
        this.date = date;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAgentMeta() {
        return agentMeta;
    }

    public void setAgentMeta(String agentMeta) {
        this.agentMeta = agentMeta;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Post{" +
                "traceId='" + traceId + '\'' +
                ", content='" + content + '\'' +
                ", agentMeta='" + agentMeta + '\'' +
                ", date=" + date +
                '}';
    }
}

