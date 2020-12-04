package com.nexustech.comicfire.domains;

public class Winner {
    String PostId,Rank;

    public Winner() {
    }

    public Winner(String postId, String rank) {
        PostId = postId;
        Rank = rank;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getRank() {
        return Rank;
    }

    public void setRank(String rank) {
        Rank = rank;
    }
}
