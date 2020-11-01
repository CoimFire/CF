package com.nexustech.comicfire.domains;

public class Posts {
    String UserId,Date,Time,PostText,PostId,PostImage,ProfileImage,DisplayName;

    public Posts() {
    }

    public Posts(String userId, String date, String time, String postText, String postId, String postImage, String profileImage, String displayName) {
        UserId = userId;
        Date = date;
        Time = time;
        PostText = postText;
        PostId = postId;
        PostImage = postImage;
        ProfileImage = profileImage;
        DisplayName = displayName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getPostText() {
        return PostText;
    }

    public void setPostText(String postText) {
        PostText = postText;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getPostImage() {
        return PostImage;
    }

    public void setPostImage(String postImage) {
        PostImage = postImage;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }
}
