package com.nathanael.pierregignoux.models.direction;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class Historique {

    private String id;
    private String userId;
    private Timestamp date;
    private String consommation;






    public Historique(String id, String userId, Timestamp date, String consommation)
    {
        this.id = id;
        this.date = date;
        this.consommation=consommation;
        this.userId= userId;

    }



    public String getId() {
        return id;
    }

    public String getUserIdId() {
        return userId;
    }

    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        String DateData = formatter.format(date.toDate());

        return DateData;
    }

    public int getConsommation() {
        double data= Double.parseDouble(consommation);
        int value = (int)data;
        return value;
    }

}
