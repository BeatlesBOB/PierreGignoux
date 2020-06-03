package com.nathanael.pierregignoux;

public class Vehicule {

    private String id;
    private String titre;
    private String image;
    private String consocalcule;
    private String methode;



    public Vehicule(String id, String titre, String image,String consocalcule, String methode)
    {

        this.id = id;
        this.titre = titre;
        this.image = image;
        this.consocalcule = consocalcule;
        this.methode = methode;


    }

    public String getId() {
        return id;
    }
    public String getTitre() {
        return titre;
    }
    public String getImage(){return image;}
    public  String getConsocalcule(){return  consocalcule;}
    public  String getMethode(){return  methode;}


    public String toString() {
        return titre;
    }

}
