package com.angelzbg.communityforum.models;

public class Community {

    private String avatar;
    private String creator;
    private Long date;
    private String description;
    private String name;
    private String pinned_post;
    private int posts_count;
    private int users_count;

    public Community(){}

    public Community(String avatar, String creator, Long date, String description, String name, String pinned_post, int posts_count, int users_count) {
        this.avatar = avatar;
        this.creator = creator;
        this.date = date;
        this.description = description;
        this.name = name;
        this.pinned_post = pinned_post;
        this.posts_count = posts_count;
        this.users_count = users_count;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinned_post() {
        return pinned_post;
    }

    public void setPinned_post(String pinned_post) {
        this.pinned_post = pinned_post;
    }

    public int getPosts_count() {
        return posts_count;
    }

    public void setPosts_count(int posts_count) {
        this.posts_count = posts_count;
    }

    public int getUsers_count() {
        return users_count;
    }

    public void setUsers_count(int users_count) {
        this.users_count = users_count;
    }
}