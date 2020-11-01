package com.nexustech.comicfire.domains;

public class UserPosts {
    String PostKey;

    public UserPosts() {
    }

    public UserPosts(String postKey) {
        PostKey = postKey;
    }

    public String getPostKey() {
        return PostKey;
    }

    public void setPostKey(String postKey) {
        PostKey = postKey;
    }
}
