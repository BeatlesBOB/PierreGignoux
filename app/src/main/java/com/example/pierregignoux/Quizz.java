package com.example.pierregignoux;


import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class Quizz {

    private String id;
    private String score;
    private String auteur;
    private Timestamp date;





    public Quizz(String id, String score, String auteur,Timestamp date)
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

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        String DateData = formatter.format(date.toDate());

        return DateData;

    }
}
