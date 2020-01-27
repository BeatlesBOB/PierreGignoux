package com.example.pierregignoux.models.direction;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class Trajet {

    private String id;
    private String auteurId;
    private Timestamp date;
    private String consommation;
    private String vehicule;
    private String kilometre;
    private String image;




    public Trajet(String id, String auteurId, Timestamp date, String consommation, String vehicule, String kilometre, String image)
    {
        this.id = id;
        this.auteurId = auteurId;
        this.date = date;
        this.consommation=consommation;
        this.vehicule= vehicule;
        this.kilometre= kilometre;
        this.image = image;


    }



    public String getId() {
        return id;
    }

    public String getAuteurId() {
        return auteurId;
    }

    public String getDate() {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        String DateData = formatter.format(date.toDate());

        return DateData;

    }

    public String getVehicule() {
        return vehicule;
    }

    public String getKilometre() {
        return kilometre;
    }

    public String getImage() {
        return image;
    }


    public int getConsommation() {

        double data= Double.parseDouble(consommation);
        int value = (int)data;
        return value;
    }

}
