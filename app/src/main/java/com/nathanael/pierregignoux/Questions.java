package com.nathanael.pierregignoux;

import android.content.Context;
import android.content.ContextWrapper;


public class Questions extends ContextWrapper {

//    Context mContext;
//    public Questions(Context context) // constructor
//    {
//        mContext = context;
//    }

    public Questions(Context base) {
        super(base);
    }
    public String mQuestions[] = getResources().getStringArray(R.array.question_array);

//    public String mQuestions[] = {
//
//            "A quelle fréquence manger vous de la viande ?",
//            "Combien de temps dur en moyenne votre douche ?",
//            "A quelle fréquence recyclez vous ?",
//            "Votre production de déchét par rapport a votre voisin ?",
//            "Combien de temps par jour mettez vous le chauffage chez vous ?",
//            "Faites vous toujours attention a éteindre les lumières ?"
//    };

//    public String mChoices[][] = getResources().getStringArray(R.array.choices_array);
    String choice1[] = getResources().getStringArray(R.array.choice01);
    String choice2[] = getResources().getStringArray(R.array.choice02);
    String choice3[] = getResources().getStringArray(R.array.choice03);
    String choice4[] = getResources().getStringArray(R.array.choice04);
    String choice5[] = getResources().getStringArray(R.array.choice05);
    String choice6[] = getResources().getStringArray(R.array.choice06);


    private String mChoices[][] = {
            choice1,choice2,choice3,choice4,choice5,choice6
//            {"Zéro fois par semaines", "Une fois par semaines", "Entre deux et trois fois par semaines", "Quatre fois et plus par semaines"},
//            {"Moins de 6 minutes", "Entre 6 et 10 minutes", "Entre 10 et 15 minutes", "Plus de 15 minutes"},
//            {"Jamais", "Presque jamais", "Parfois", "Toujours"},
//            {"Moins que mon voisin", "Equivalent a mon voisin", "Légèrement plus que mon voisin", "Considérablement plus que mon voisin"},
//            {"Il n'est jamaais allumé", "Entre une et deux heures", "Entre trois et quatre heures", "Quatre heures et plus"},
//            {"Jamais", "Presque jamais", "Parfois", "Toujours"}
    };

    public String mCorrectAnswers[] = getResources().getStringArray(R.array.correctAnswers_array);

//    private String mCorrectAnswers[] = {"Zéro fois par semaines", "Moins de 6 minutes", "Jamais", "Moins que mon voisin", "Il n'est jamaais allumé", "Jamais"};




    public String getQuestion(int a) {
        String question = mQuestions[a];
        return question;

    }

    public String getChoice1(int a) {
        String choice = mChoices[a][0];
        return choice;
    }

    public String getChoice2(int a) {
        String choice = mChoices[a][1];
        return choice;
    }
    public String getChoice3(int a){
        String choice = mChoices[a][2];
        return choice;
    }
    public String getChoice4(int a){
        String choice = mChoices[a][3];
        return choice;
    }

    public String getCorrectAnswer(int a){
        String answer = mCorrectAnswers[a];
        return answer;
    }
}



