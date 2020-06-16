package com.nathanael.pierregignoux;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

public class MonthActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        String uid = user.getUid();
        db.collection("users")
                .whereEqualTo(FieldPath.documentId(), uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        double intConsoUser2 = 0;
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {


                                String consoUser2 = document.getString("conso_mois");
                                intConsoUser2 = Double.parseDouble(consoUser2);
                                TextView tot_co2 = findViewById(R.id.totCO2Month);
                                if (intConsoUser2 >=0){
                                    if (intConsoUser2/1000000 >= 1){
                                        intConsoUser2 = intConsoUser2/1000000;
                                        tot_co2.setText(Math.round(intConsoUser2)+" t/CO2");
                                    }else if(intConsoUser2/1000 >= 1){
                                        intConsoUser2 = intConsoUser2/1000;
                                        tot_co2.setText(Math.round(intConsoUser2)+" kg/CO2");
                                    }else {
                                        tot_co2.setText(Math.round(intConsoUser2)+" g/CO2");
                                    }
                                }


                                if (intConsoUser2 < 0){
                                    if (intConsoUser2/1000000 <= -1){
                                        intConsoUser2 = intConsoUser2/1000000;
                                        tot_co2.setText(intConsoUser2+" t/CO2");
                                    }else if(intConsoUser2/1000 <= -1){
                                        intConsoUser2 = intConsoUser2/1000;
                                        tot_co2.setText(intConsoUser2+" kg/CO2");
                                    }else {
                                        tot_co2.setText(intConsoUser2+" g/CO2");
                                    }
                                }

                                String kilometreUser = document.getString("kilometre_mois");
                                TextView totkilouser = findViewById(R.id.totKilometreMonth);
                                double intkilometreUser = Double.parseDouble(kilometreUser);
                                totkilouser.setText(Math.round(intkilometreUser)+" Km");


                                String eco_co2 = document.getString("eco_mois");
                                TextView toteco_co2 = findViewById(R.id.totecoCO2Month);
                                double intEco_co2 = Double.parseDouble(eco_co2);
                                if (intEco_co2 >=0){
                                    if (intEco_co2/1000000 >= 1){
                                        intEco_co2 = intEco_co2/1000000;
                                        toteco_co2.setText(Math.round(intEco_co2)+" t/CO2");
                                    }else if(intEco_co2/1000 >= 1){
                                        intEco_co2 = intEco_co2/1000;
                                        toteco_co2.setText(Math.round(intEco_co2)+" kg/CO2");
                                    }else {
                                        toteco_co2.setText(Math.round(intEco_co2)+" g/CO2");
                                    }
                                }


                                if (intEco_co2 < 0){
                                    if (intEco_co2/1000000 <= -1){
                                        intEco_co2 = intEco_co2/1000000;
                                        toteco_co2.setText(intEco_co2+" t/CO2");
                                    }else if(intEco_co2/1000 <= -1){
                                        intEco_co2 = intEco_co2/1000;
                                        toteco_co2.setText(intEco_co2+" kg/CO2");
                                    }else {
                                        toteco_co2.setText(intEco_co2+" g/CO2");
                                    }

                                }
                            }
                        } else {
                            Log.d("TEST", "Error getting documents: ", task.getException());
                        }
                    }
                });

        FloatingActionButton btninfo = findViewById(R.id.btninfomonth);
        btninfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cStart = Calendar.getInstance();   // this takes current date
                cStart.set(Calendar.DAY_OF_MONTH, 1);
                Date dateStart = cStart.getTime();
                long dateStartLong = dateStart.getTime();

                Calendar cEnd = Calendar.getInstance();   // this takes current date
                cEnd.set(Calendar.DATE, cEnd.getActualMaximum(Calendar.DATE));
                Date dateEnd = cEnd.getTime();
                long dateEndLong = dateEnd.getTime();

                Intent intent = new Intent(MonthActivity.this, InfoActivity.class);
                intent.putExtra("From","Month");
                intent.putExtra("DateStart",dateStartLong);
                intent.putExtra("DateEnd",dateEndLong);
                startActivity(intent);
            }
        });

    }
}
