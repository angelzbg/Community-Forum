package com.angelzbg.communityforum.models;

public class Post {

    private String community;
    private String author;
    private long date;
    private String title;
    private String text;
    private String image;
    private int comments_count;
    private int votes;

    public Post(){}

    public Post(String community, String author, long date, String title, String text, String image, int comments_count, int votes) {
        this.community = community;
        this.author = author;
        this.date = date;
        this.title = title;
        this.text = text;
        this.image = image;
        this.comments_count = comments_count;
        this.votes = votes;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getComments_count() {
        return comments_count;
    }

    public void setComments_count(int comments_count) {
        this.comments_count = comments_count;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
} // Post{}