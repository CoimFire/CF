package com.nexustech.comicfire.domains;

public class Comments {
    String CommentId,PostId,Comment,CommenterId,ParentUserId;
    String HasReply;

    public Comments() {
    }

    public Comments(String commentId, String postId, String comment, String commenterId, String hasReply, String parentUserId) {
        CommentId = commentId;
        PostId = postId;
        Comment = comment;
        CommenterId = commenterId;
        HasReply = hasReply;
        ParentUserId = parentUserId;
    }

    public String getCommentId() {
        return CommentId;
    }

    public void setCommentId(String commentId) {
        CommentId = commentId;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getCommenterId() {
        return CommenterId;
    }

    public void setCommenterId(String commenterId) {
        CommenterId = commenterId;
    }

    public String isHasReply() {
        return HasReply;
    }

    public void setHasReply(String hasReply) {
        HasReply = hasReply;
    }

    public String getParentUserId() {
        return ParentUserId;
    }

    public void setParentUserId(String parentUserId) {
        ParentUserId = parentUserId;
    }
}
