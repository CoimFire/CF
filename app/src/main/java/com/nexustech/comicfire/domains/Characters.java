package com.nexustech.comicfire.domains;

public class Characters {
    String CharacterName,CharImage,RequiredPoints,CharacterProfile,Abilities,Franchise,Role,Race,Priority;

    public Characters() {
    }

    public Characters(String characterName, String charImage, String requiredPoints, String characterProfile,
                      String abilities, String franchise, String role, String race, String priority) {
        CharacterName = characterName;
        CharImage = charImage;
        RequiredPoints = requiredPoints;
        CharacterProfile = characterProfile;
        Abilities = abilities;
        Franchise = franchise;
        Role = role;
        Race = race;
        Priority=priority;
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

    public String getAbilities() {
        return Abilities;
    }

    public void setAbilities(String abilities) {
        Abilities = abilities;
    }

    public String getFranchise() {
        return Franchise;
    }

    public void setFranchise(String franchise) {
        Franchise = franchise;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getRace() {
        return Race;
    }

    public void setRace(String race) {
        Race = race;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }
}
