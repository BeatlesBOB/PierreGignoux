package com.example.pierregignoux;


public class Quizz {

    private String id;
    private String score;
    private String auteur;
    private String date;





    public Quizz(String id, String score, String auteur,String date)
    {
        this.id = id;
        this.score = score;
        this.auteur = auteur;
        this.date = date;

    }

    public String getId() {
        return id;
    }

    public String getScore() {
        return score;
    }

    public String getAuteur() {
        return auteur;
    }
    public String getDate() {
        return date;
    }
}
