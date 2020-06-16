package com.nathanael.pierregignoux.models.direction;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Historique {

    private String id;
    private String userId;
    private Timestamp date;
    private String consommation;
    private String economie;
    private String kilometre;







    public Historique(String id, String userId, Timestamp date, String consommation, String economie, String kilometre)
    {
        this.id = id;
        this.date = date;
        this.consommation=consommation;
        this.userId= userId;
        this.economie= economie;
        this.kilometre = kilometre;

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

    public String getEconomie() {
        return economie;
    }

    public String getKilometre() {
        return kilometre;
    }

    public Date getDateDate() {
        return date.toDate();
    }

}
