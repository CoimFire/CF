package com.nexustech.comicfire.domains;

public class Characters {
    String CharacterName,CharImage,RequiredPoints,CharacterProfile;

    public Characters() {
    }

    public Characters(String characterName, String charImage, String requiredPoints, String characterProfile) {
        CharacterName = characterName;
        CharImage = charImage;
        RequiredPoints = requiredPoints;
        CharacterProfile = characterProfile;
    }

    public String getCharacterName() {
        return CharacterName;
    }

    public void setCharacterName(String characterName) {
        CharacterName = characterName;
    }

    public String getCharImage() {
        return CharImage;
    }

    public void setCharImage(String charImage) {
        CharImage = charImage;
    }

    public String getRequiredPoints() {
        return RequiredPoints;
    }

    public void setRequiredPoints(String requiredPoints) {
        RequiredPoints = requiredPoints;
    }

    public String getCharacterProfile() {
        return CharacterProfile;
    }

    public void setCharacterProfile(String characterProfile) {
        CharacterProfile = characterProfile;
    }
}
