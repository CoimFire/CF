package com.nexustech.comicfire.domains;

public class Quiz {
    String CreatedDate,QuizName,QuizImage,State,QuizId;

    public Quiz() {
    }


    public Quiz(String createdDate, String quizName, String quizImage, String state, String quizId) {
        CreatedDate = createdDate;
        QuizName = quizName;
        QuizImage = quizImage;
        State = state;
        QuizId = quizId;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getQuizName() {
        return QuizName;
    }

    public void setQuizName(String quizName) {
        QuizName = quizName;
    }

    public String getQuizImage() {
        return QuizImage;
    }

    public void setQuizImage(String quizImage) {
        QuizImage = quizImage;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getQuizId() {
        return QuizId;
    }

    public void setQuizId(String quizId) {
        QuizId = quizId;
    }
}
