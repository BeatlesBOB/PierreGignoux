package com.nathanael.pierregignoux;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nathanael.pierregignoux.models.direction.Trajet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nathanael.pierregignoux.utils.DatepickerFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoActivity extends AppCompatActivity implements  DatepickerFragment.DatePickerFragmentListener {

    ListAdapterInfo listAdapter;
    ArrayList<String> mImage = new ArrayList<>();
    ArrayList<String> mCO2 = new ArrayList<>();
    ArrayList<Double> mTotal = new ArrayList<>();
    ArrayList<Double> mKilo = new ArrayList<>();
    private Boolean totaldate;
    private Boolean moisdate;

    List<Trajet> items = new ArrayList<>();
    private FirebaseFirestore db;
    private double total;
    private String uid;

    int DATE_DIALOG = 0;
    FragmentManager fm = getSupportFragmentManager();
    private Boolean datepickerdebut;
    private Boolean datepickerfin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ListView listView = findViewById(R.id.listinfo);
        mTotal.add(0.0);
        listAdapter = new ListAdapterInfo(this,mImage,mCO2,mTotal,mKilo);
        listAdapter.notifyDataSetChanged();
        listView.setAdapter(listAdapter);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        total=0;


        Intent intent = getIntent();
        totaldate = intent.getBooleanExtra("TotalDate",true);
        moisdate = intent.getBooleanExtra("MoisDate",false);
        datepickerdebut = false;
        datepickerfin= false;
        db = FirebaseFirestore.getInstance();
        getItemdate(uid);


        Button btndatedebut = findViewById(R.id.btndatedebut);
        btndatedebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATE_DIALOG = 1;
                openDialog();
            }
        });


        Button btndatefin = findViewById(R.id.btndatefin);
        btndatefin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATE_DIALOG = 2;
                openDialog();
            }
        });


    }

    private void getItemdate(String uid){
        if (moisdate){
            Log.d("getItemdate","Mois");
            items.clear();
            Date datefin = new Date();
            Date datedebut = new Date();
            Intent intent = getIntent();
            datedebut.setTime(intent.getLongExtra("DateStart", -1));
            datefin.setTime(intent.getLongExtra("DateEnd", -1));

            db.collection("trajets")
                    .whereEqualTo("auteurTrajet", uid)
                    .whereLessThan("dateTrajet",datefin)
                    .whereGreaterThan("dateTrajet",datedebut)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                Map<String, Integer> sum = null;

                                Map<String, Double> sum2 = null;


                                for (QueryDocumentSnapshot document : task.getResult()) {


                                    Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("distanceTrajet"),document.getString("imageTrajet"), document.getString("consoTrajet"));

                                    items.add(nTrajet);

                                    sum = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getImage, Collectors.summingInt(Trajet::getConsommation)));

                                    sum2 = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getImage, Collectors.summingDouble(Trajet::getIntKilometre)));

                                }

                                if (sum != null && sum2 != null) {
                                    total = mTotal.get(0);
                                    sum.forEach((k, v) -> {
                                        Log.d("info", "Item : " + k + " Count : " + v);

                                        mImage.add(k);
                                        mCO2.add(String.valueOf(v));
                                        total += v;
                                        mTotal.set(0,total);
                                    });

                                    sum2.forEach((k, v) -> {
                                        Log.d("info", "Item : " + k + " Count : " + v);
                                        mKilo.add(v);
                                    });
                                    Log.d("calculepourcentage","mtotal.get(0)"+mTotal.get(0));

                                    listAdapter.notifyDataSetChanged();

                                }

                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }else if (totaldate){
            items.clear();
            db.collection("trajets")
                    .whereEqualTo("auteurTrajet", uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                Map<String, Integer> sum = null;
                                Map<String, Double> sum2 = null;

                                for (QueryDocumentSnapshot document : task.getResult()) {


                                    Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("distanceTrajet"),document.getString("imageTrajet"), document.getString("consoTrajet"));

                                    items.add(nTrajet);

                                    sum = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getImage, Collectors.summingInt(Trajet::getConsommation)));
                                    sum2 = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getImage, Collectors.summingDouble(Trajet::getIntKilometre)));

                                }

                                if (sum != null && sum2 != null) {
                                    total = mTotal.get(0);
                                    sum.forEach((k, v) -> {
                                        Log.d("info", "Item : " + k + " Count : " + v);

                                        mImage.add(k);
                                        mCO2.add(String.valueOf(v));
                                        total += v;
                                        mTotal.set(0,total);
                                    });

                                    sum2.forEach((k, v) -> {
                                        Log.d("info", "Item : " + k + " Count : " + v);

                                        mKilo.add(v);

                                    });

                                    listAdapter.notifyDataSetChanged();

                                }

                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }

    }


    private void getItemdatePicker(Date datedebut, Date datefin){
        Log.d("getItemdatePicker","getItemdatePicker");
        items.clear();
        listAdapter.notifyDataSetChanged();

        Log.d("getItemdatePicker",datedebut.toString());

        db.collection("trajets")
                .whereEqualTo("auteurTrajet", uid)
                .whereLessThan("dateTrajet",datefin)
                .whereGreaterThan("dateTrajet",datedebut)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Map<String, Integer> sum = null;

                            Map<String, Double> sum2 = null;


                            for (QueryDocumentSnapshot document : task.getResult()) {


                                Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("distanceTrajet"),document.getString("imageTrajet"), document.getString("consoTrajet"));

                                items.add(nTrajet);

                                sum = items.stream().collect(
                                        Collectors.groupingBy(Trajet::getImage, Collectors.summingInt(Trajet::getConsommation)));

                                sum2 = items.stream().collect(
                                        Collectors.groupingBy(Trajet::getImage, Collectors.summingDouble(Trajet::getIntKilometre)));

                            }

                            if (sum != null && sum2 != null) {
                                total = mTotal.get(0);
                                sum.forEach((k, v) -> {
                                    Log.d("info", "Item : " + k + " Count : " + v);

                                    mImage.add(k);
                                    mCO2.add(String.valueOf(v));
                                    total += v;
                                    mTotal.set(0,total);
                                });

                                sum2.forEach((k, v) -> {
                                    Log.d("info", "Item : " + k + " Count : " + v);
                                    mKilo.add(v);
                                });
                                Log.d("calculepourcentage","mtotal.get(0)"+mTotal.get(0));

                                listAdapter.notifyDataSetChanged();

                            }

                        } else {
                            Log.d("TEST", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void openDialog(){
        DatepickerFragment datepickDialog = new DatepickerFragment();
        datepickDialog.show(fm, "Start Date");
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        if(DATE_DIALOG ==1){
            datepickerdebut = true;
            Calendar calendar = new C


        }
        else if(DATE_DIALOG ==2){
            datepickerfin = true;

        }
    }



    class ListAdapterInfo extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> rImage = new ArrayList<>();
        ArrayList<String> rCO2 = new ArrayList<>();
        ArrayList<Double> rTotal = new ArrayList<>();
        ArrayList<Double> rKilo = new ArrayList<>();


        ListAdapterInfo(Context c,ArrayList<String> image, ArrayList<String> co2,ArrayList<Double> total, ArrayList<Double> kilo){
            super(c,R.layout.layout_info_row,image);
            this.context =c;
            this.rCO2 = co2;
            this.rImage = image;
            this.rTotal = total;
            this.rKilo = kilo;

        }



        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.layout_info_row, parent,false);
            TextView myCO2 = row.findViewById(R.id.infoCO2);
            TextView myPourcent = row.findViewById(R.id.infoPourcent);
            ImageView myImage = row.findViewById(R.id.imageinfo);

            double intrCO2 = Double.parseDouble(rCO2.get(position));
            int pourcent = (int) Math.round(intrCO2*100/rTotal.get(0));
            if(rTotal.get(0) == 0){
                myPourcent.setText(0+"%");
            }else {
                myPourcent.setText(pourcent+"%");
            }

            if (intrCO2/1000000 >= 1){
                intrCO2 = intrCO2/1000000;
                myCO2.setText(intrCO2+" t/CO2");
            }else if(intrCO2/1000 >= 1){
                intrCO2 = intrCO2/1000;
                myCO2.setText(intrCO2+" kg/CO2");
            }else {
                myCO2.setText(intrCO2+" g/CO2");
            }
            String image = rImage.get(position);
            Picasso.get().load(image).into(myImage);
            TextView infoKilotxt = row.findViewById(R.id.infoKilo);
            infoKilotxt.setText(Math.round(rKilo.get(position))+" Km");


            return row;
        }

    }
}

