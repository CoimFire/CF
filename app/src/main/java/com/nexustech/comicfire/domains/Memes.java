package com.nexustech.comicfire.domains;

public class Memes {

    private String MemeName,memeCoverImageUrl,MemeImage,CreatedDate,State;

    public Memes() {
    }

    public Memes(String memeName, String memeCoverImageUrl, String memeImage, String createdDate, String state) {
        MemeName = memeName;
        this.memeCoverImageUrl = memeCoverImageUrl;
        MemeImage = memeImage;
        CreatedDate = createdDate;
        State=state;
    }

    public String getMemeName() {
        return MemeName;
    }

    public void setMemeName(String memeName) {
        this.MemeName = memeName;
    }

    public String getMemeCoverImageUrl() {
        return memeCoverImageUrl;
    }

    public void setMemeCoverImageUrl(String memeCoverImageUrl) {
        this.memeCoverImageUrl = memeCoverImageUrl;
    }

    public String getMemeImage() {
        return MemeImage;
    }

    public void setMemeImage(String memeImage) {
        MemeImage = memeImage;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }
}

