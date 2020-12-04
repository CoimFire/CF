package com.nexustech.comicfire.domains;

import com.nexustech.comicfire.utils.Utils;

public class ReplyComments {

    String CommentId,RepliedUserId,CommentText,ParentCommentId,ParentCommentUserId,ParentUserId;
    public ReplyComments() {
    }

    public ReplyComments(String commentId, String repliedUserId, String commentText, String parentCommentId, String parentCommentUserId, String parentUserId) {
        CommentId = commentId;
        RepliedUserId = repliedUserId;
        CommentText = commentText;
        ParentCommentId = parentCommentId;
        ParentCommentUserId = parentCommentUserId;
        ParentUserId = parentUserId;
    }

    public String getCommentId() {
        return CommentId;
    }

    public void setCommentId(String commentId) {
        CommentId = commentId;
    }

    public String getRepliedUserId() {
        return RepliedUserId;
    }

    public void setRepliedUserId(String repliedUserId) {
        RepliedUserId = repliedUserId;
    }

    public String getCommentText() {
        return CommentText;
    }

    public void setCommentText(String commentText) {
        CommentText = commentText;
    }

    public String getParentCommentId() {
        return ParentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        ParentCommentId = parentCommentId;
    }

    public String getParentCommentUserId() {
        return ParentCommentUserId;
    }

    public void setParentCommentUserId(String parentCommentUserId) {
        ParentCommentUserId = parentCommentUserId;
    }

    public String getParentUserId() {
        return ParentUserId;
    }

    public void setParentUserId(String parentUserId) {
        ParentUserId = parentUserId;
    }
}
