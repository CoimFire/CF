package com.nexustech.comicfire.domains;

public class Users {
    String DisplayName,ProfileImage,CharacterName,UserId, Points;

    public Users() {
    }

    public Users(String displayName, String profileImage, String characterName, String userId, String points) {
        DisplayName = displayName;
        ProfileImage = profileImage;
        CharacterName = characterName;
        UserId = userId;
        Points = points;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getCharacterName() {
        return CharacterName;
    }

    public void setCharacterName(String characterName) {
        CharacterName = characterName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPoints() {
        return Points;
    }

    public void setPoints(String points) {
        Points = points;
    }
}
