package com.angelzbg.communityforum.models;

public class Message {
    private String from;
    private String message;
    private Long date;

    public Message() {};

    public Message(String from, String message, Long date) {
        this.from = from;
        this.message = message;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
} // Message{}