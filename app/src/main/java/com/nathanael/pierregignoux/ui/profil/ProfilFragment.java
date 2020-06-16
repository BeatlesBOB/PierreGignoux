package com.nathanael.pierregignoux.ui.profil;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.nathanael.pierregignoux.HistoriqueActivity;
import com.nathanael.pierregignoux.InfoActivity;
import com.nathanael.pierregignoux.Main2Activity;
import com.nathanael.pierregignoux.MapsActivity;
import com.nathanael.pierregignoux.ModifProfil;
import com.nathanael.pierregignoux.MonthActivity;
import com.nathanael.pierregignoux.R;
import com.nathanael.pierregignoux.models.direction.Trajet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nathanael.pierregignoux.service.ReminderBroadcast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class ProfilFragment extends Fragment {

    private ProfilViewModel profilViewModel;

    TextView name;

    private int REQUEST_IMAGE_CAPTURE = 1001;
    ImageView proimg;
    ArrayList<String> trajtype;
    List<Trajet> items = new ArrayList<>();

    ArrayList<Double> trajdistvelo;

    int counthabitude = 0;


    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profilViewModel = ViewModelProviders.of(this).get(ProfilViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profil, container, false);

        createNotificationChannel();

        trajtype = new ArrayList<>();
        trajdistvelo = new ArrayList<Double>();

        db = FirebaseFirestore.getInstance();
        name= root.findViewById(R.id.profilnom);
        proimg = root.findViewById(R.id.imageprofil);


        final double[] velodist = {0};
        final double[] marchedist = {0};
        final double[] raildist = {0};
        final double[] busdist = {0};
        final double[] codist = {0};
        final double[] ecoCO2 = {0};


        final double[] deuxRconso = {0};
        final double[] voitconso = {0};
        final double[] avconso = {0};

        final double[] moyenneQuizz = {0};
        final double[] intkilometreUser = {0};
        final double[] bonneH = {0};


        if (user!= null) {
            String uid = user.getUid();

            db.collection("trajets")
                    .whereEqualTo("auteurTrajet",uid)
                    .limit(20)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    trajtype.add(document.getString("vehiculeTrajet"));
                                }

                                for (String s : trajtype){
                                    if (s.contains("Métro") || s.contains("TRAM") || s.contains("RER")|| s.contains("Train") || s.contains("Covoiturage") || s.contains("Auto-Stop") || s.contains("Vélo") || s.contains("Télétravail")|| s.contains("Marche")){
                                        bonneH[0]++;
                                    }
                                }
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                                String stringbonneH = sharedPreferences.getString("bonneH","5");
                                String stringbonneHSave = sharedPreferences.getString("bonneHSave", String.valueOf(bonneH[0]));

                                double doublebonneH = Double.parseDouble(stringbonneH);
                                double doublebonneHSave = Double.parseDouble(stringbonneHSave);

                                ImageButton btnbonneH = root.findViewById(R.id.BH);

                                if (bonneH[0]-doublebonneHSave >= doublebonneH){
                                    btnbonneH.clearColorFilter();
                                }


                            } else {

                            }
                        }
                    });

            db.collection("trajets")
                    .whereEqualTo("auteurTrajet",uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Map<String, Double> sum = null;
                            Map<String, Integer> sum2 = null;

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("distanceTrajet"),document.getString("imageTrajet"),document.getString("ecoTrajet"));

                                    items.add(nTrajet);

                                    sum = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getVehicule, Collectors.summingDouble(Trajet::getIntKilometre)));

                                    sum2 = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getVehicule, Collectors.summingInt(Trajet::getConsommation)));


                                }
                                if (sum != null) {
                                    sum.forEach((k, v) -> {
                                        Log.d("info", "Item : " + k + " Count : " + v);

                                        if (k.contains("Vélo")){
                                            velodist[0] = v;
                                        }

                                        if (k.contains("Marche")){
                                            marchedist[0] = v;
                                        }
                                        if (k.contains("Métro") || k.contains("TRAM") || k.contains("RER")|| k.contains("Train")){
                                            raildist[0] = v;
                                        }
                                         if (k.contains("Bus")){
                                            busdist[0] = v;
                                        }
                                        if (k.contains("Covoiturage")){
                                            codist[0] = v;
                                        }

                                    });
                                    ImageButton maillotJ = root.findViewById(R.id.MJ);

                                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                                    String stringvelodist = sharedPreferences.getString("velodist","100");
                                    String stringvelodistSave = sharedPreferences.getString("velodistSave", String.valueOf(velodist[0]));

                                    double doublevelodist = Double.parseDouble(stringvelodist);
                                    double doublevelodistSave = Double.parseDouble(stringvelodistSave);

                                    if (velodist[0]-doublevelodistSave >= doublevelodist){
                                        maillotJ.clearColorFilter();
                                    }
                                    ImageButton marcheB = root.findViewById(R.id.MB);

                                    String stringmarchedist = sharedPreferences.getString("marchedist","100");
                                    String stringmarchedistSave = sharedPreferences.getString("marchedistSave", String.valueOf(marchedist[0]));

                                    double doublemarchedist = Double.parseDouble(stringmarchedist);
                                    double doublemarchedistSave = Double.parseDouble(stringmarchedistSave);

                                    if (marchedist[0]-doublemarchedistSave >= doublemarchedist){
                                        marcheB.clearColorFilter();
                                    }


                                    ImageButton interR = root.findViewById(R.id.IR);
                                    String stringraildist = sharedPreferences.getString("raildist","100");
                                    String stringraildistSave = sharedPreferences.getString("raildistSave", String.valueOf(raildist[0]));

                                    double doubleraildist = Double.parseDouble(stringraildist);
                                    double doubleraildistSave = Double.parseDouble(stringraildistSave);

                                    if (raildist[0]-doubleraildistSave >= doubleraildist){
                                        interR.setBackgroundColor(getContext().getColor(R.color.colorAccent));
                                    }

                                    String stringbusdist = sharedPreferences.getString("busdist","100");
                                    String stringbusdistSave = sharedPreferences.getString("busdistSave", String.valueOf(busdist[0]));

                                    double doublebusdist = Double.parseDouble(stringbusdist);
                                    double doublebusdistSave = Double.parseDouble(stringbusdistSave);

                                    ImageButton rosaP = root.findViewById(R.id.RP);
                                    if (busdist[0]-doublebusdistSave >= doublebusdist){
                                        rosaP.clearColorFilter();
                                    }

                                    String stringcovoit = sharedPreferences.getString("covoit","100");
                                    String stringcovoitSave = sharedPreferences.getString("covoitSave", String.valueOf(codist[0]));

                                    double doublecovoit = Double.parseDouble(stringcovoit);
                                    double doublecovoitSave = Double.parseDouble(stringcovoitSave);

                                    ImageButton covoit = root.findViewById(R.id.CV);
                                    if (codist[0]-doublecovoitSave >= doublecovoit){
                                        covoit.clearColorFilter();
                                    }


                                }
                                if (sum2 != null){
                                    sum2.forEach((k, v) -> {
                                        Log.d("info", "Item : " + k + " Count : " + v);

                                        if (k.contains("Deux roues")){
                                            deuxRconso[0] = v;
                                        }
                                        if (k.contains("Voiture")){
                                            voitconso[0] = v;
                                        }
                                        if (k.contains("Avion")){
                                            avconso[0] = v;
                                        }
                                    });
                                    ImageButton motoc = root.findViewById(R.id.MC);
                                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                                    String stringdeuxRconso = sharedPreferences.getString("deuxRconso","100");
                                    String stringdeuxRconsoSave = sharedPreferences.getString("deuxRconsoSave", String.valueOf(deuxRconso[0]));

                                    double doubledeuxRconso = Double.parseDouble(stringdeuxRconso)*1000;
                                    double doubledeuxRconsoSave = Double.parseDouble(stringdeuxRconsoSave)*1000;

                                    if (deuxRconso[0]-doubledeuxRconsoSave <= doubledeuxRconso){
                                        motoc.clearColorFilter();
                                    }
                                    ImageButton voitc = root.findViewById(R.id.VC);
                                    String stringdvoitconso = sharedPreferences.getString("voitconso","100");
                                    String stringdvoitconsoSave = sharedPreferences.getString("voitconsoSave", String.valueOf(voitconso[0]));

                                    double doubledvoitconso = Double.parseDouble(stringdvoitconso)*1000;
                                    double doubledvoitconsoSave = Double.parseDouble(stringdvoitconsoSave)*1000;

                                    if (voitconso[0]-doubledvoitconsoSave <= doubledvoitconso){
                                        voitc.clearColorFilter();
                                    }

                                    ImageButton avc = root.findViewById(R.id.AC);
                                    String stringdavconso = sharedPreferences.getString("avconso","100");
                                    String stringdavconsoSave = sharedPreferences.getString("avconsoSave", String.valueOf(avconso[0]));

                                    double doubledavconso = Double.parseDouble(stringdavconso)*1000;
                                    double doubledavconsoSave = Double.parseDouble(stringdavconsoSave)*1000;

                                    if (avconso[0]-doubledavconsoSave <= doubledavconso){
                                        avc.clearColorFilter();
                                    }
                                }

                            } else {

                            }
                        }
                    });




            TextView modprofil = root.findViewById(R.id.modifprofil);
            modprofil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getContext(), ModifProfil.class);
                    startActivity(intent);
                }
            });

            Button btnHistorique = root.findViewById(R.id.historiquebutton);
            btnHistorique.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), HistoriqueActivity.class);
                    startActivity(intent);
                }
            });

            FloatingActionButton btninfo = root.findViewById(R.id.btninfo);
            btninfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), InfoActivity.class);
                    intent.putExtra("From","Profil");
                    startActivity(intent);
                }
            });

            Button btnMonth = root.findViewById(R.id.monthbutton);
            btnMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MonthActivity.class);
                    startActivity(intent);
                }
            });


            Button btnresetuserdata = root.findViewById(R.id.resetuserdata);
            btnresetuserdata.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Map<String, Object> User = new HashMap<>();
                    User.put("obj_co2","0");
                    User.put("eco_co2","0");
                    User.put("conso_co2","0");
                    User.put("kilometre","0");
                    User.put("conso_mois","0");


                    db.collection("users").document(uid)
                            .set(User, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Reload current fragment
                            getFragmentManager().beginTransaction().detach(ProfilFragment.this).attach(ProfilFragment.this).commit();
                        }
                    });
                }
            });

            Button btnresetotherdata = root.findViewById(R.id.resetotherdata);
            btnresetotherdata.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    db.collection("trajets")
                            .whereEqualTo("auteurTrajet",uid)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            document.getReference().delete();
                                            Log.d("suc","sucesssupp");
                                        }
                                    } else {

                                    }
                                }
                            });

                    db.collection("quizz")
                            .whereEqualTo("auteurQuizz",uid)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            document.getReference().delete();
                                            Log.d("suc","sucesssupp");
                                        }
                                    } else {

                                    }
                                }
                            });


                }
            });


            proimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takepictureIntent();
                }
            });

            if (user.getPhotoUrl() != null)
            {
                Glide.with(getActivity())
                        .load(user.getPhotoUrl())
                        .into(proimg);
            }
            String nomprenom = user.getDisplayName();
            name.setText(nomprenom);

            db.collection("quizz")
                    .whereEqualTo("auteurQuizz", uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int counttotalscore = 0;
                            int countquizz = 0;
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String Stringscore = document.getString("scoreQuizz");
                                    double intScore = Double.parseDouble(Stringscore);
                                    counttotalscore += intScore;
                                    countquizz++;
                                }
                                if(countquizz != 0){
                                    moyenneQuizz[0] = counttotalscore/countquizz;
                                }
                                ImageButton qm = root.findViewById(R.id.QM);
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                                String stringmoyenneQuizz = sharedPreferences.getString("moyenneQuizz","50");
                                String stringmoyenneQuizzSave = sharedPreferences.getString("moyenneQuizzSave", String.valueOf(moyenneQuizz[0]));

                                double doubledmoyenneQuizz = Double.parseDouble(stringmoyenneQuizz);
                                double doubledmoyenneQuizzSave = Double.parseDouble(stringmoyenneQuizzSave);

                                if (moyenneQuizz[0]-doubledmoyenneQuizzSave >= doubledmoyenneQuizz){
                                    qm.clearColorFilter();
                                }
                            }
                        }
                    });


            db.collection("users")
                    .whereEqualTo(FieldPath.documentId(), uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            double intConsoUser2 = 0;
                            if (task.isSuccessful()) {

                               for (QueryDocumentSnapshot document : task.getResult()) {

                                    String consoUser = document.getString("conso_co2");
                                    TextView totco2user = root.findViewById(R.id.totCO2);
                                    double intConsoUser = Double.parseDouble(consoUser);
                                    String consoUser2 = document.getString("conso_mois");
                                    intConsoUser2 = Double.parseDouble(consoUser2);

                                   if (intConsoUser/1000000 >= 1){
                                        intConsoUser = intConsoUser/1000000;
                                        totco2user.setText(Math.round(intConsoUser)+" t/CO2");
                                    }else if(intConsoUser/1000 >= 1){
                                        intConsoUser = intConsoUser/1000;
                                        totco2user.setText(Math.round(intConsoUser)+" kg/CO2");
                                    }else {
                                        totco2user.setText(Math.round(intConsoUser)+" g/CO2");
                                    }

                                    String kilometreUser = document.getString("kilometre");
                                    TextView totkilouser = root.findViewById(R.id.totKilometre);
                                    intkilometreUser[0] = Double.parseDouble(kilometreUser);
                                    totkilouser.setText(Math.round(intkilometreUser[0])+" Km");

                                    ImageButton globeT = root.findViewById(R.id.GT);
                                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                                    String stringglobeT = sharedPreferences.getString("globeT","1000");
                                    String stringglobeTSave = sharedPreferences.getString("globeTSave", String.valueOf(intkilometreUser[0]));

                                   double DoubleglobeT = Double.parseDouble(stringglobeT);
                                   double DoubleglobeTSave = Double.parseDouble(stringglobeTSave);

                                   if (intkilometreUser[0]-DoubleglobeTSave >= DoubleglobeT){
                                        globeT.clearColorFilter();
                                    }



                                    String eco_co2 = document.getString("eco_co2");
                                    TextView toteco_co2 = root.findViewById(R.id.totecoCO2);
                                    ecoCO2[0] = Double.parseDouble(eco_co2);
                                    if (ecoCO2[0] >=0){
                                        if (ecoCO2[0]/1000000 >= 1){
                                            ecoCO2[0] = ecoCO2[0]/1000000;
                                            toteco_co2.setText(Math.round(ecoCO2[0])+" t/CO2");
                                        }else if(ecoCO2[0]/1000 >= 1){
                                            ecoCO2[0] = ecoCO2[0]/1000;
                                            toteco_co2.setText(Math.round(ecoCO2[0])+" kg/CO2");
                                        }else {
                                            toteco_co2.setText(Math.round(ecoCO2[0])+" g/CO2");
                                        }

                                    }
                                   ImageButton planteV = root.findViewById(R.id.PV);
                                   String stringplante = sharedPreferences.getString("planteVerte", "0");
                                   String stringplanteSave = sharedPreferences.getString("planteVSave", String.valueOf(ecoCO2[0]));
                                   Log.d("ntmdefi",stringplante);


                                   double doublePlanteVSave = Double.parseDouble(stringplanteSave);
                                   double doublePlante = Double.parseDouble(stringplante);

                                   if (ecoCO2[0]-doublePlanteVSave < doublePlante){
                                       planteV.clearColorFilter();
                                   }

                                   if (ecoCO2[0] < 0){
                                       if (ecoCO2[0]/1000000 <= -1){
                                           ecoCO2[0] = ecoCO2[0]/1000000;
                                           toteco_co2.setText(ecoCO2[0]+" t/CO2");
                                       }else if(ecoCO2[0]/1000 <= -1){
                                           ecoCO2[0] = ecoCO2[0]/1000;
                                           toteco_co2.setText(ecoCO2[0]+" kg/CO2");
                                       }else {
                                           toteco_co2.setText(ecoCO2[0]+" g/CO2");
                                       }

                                   }
                                }

                                double finalIntConsoUser = intConsoUser2;
                                db.collection("historique")
                                        .whereEqualTo("user",uid)
                                        .orderBy("date", Query.Direction.DESCENDING)
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        String txtConsoMois = document.getString("consommation");
                                                        double intConsoMois= Double.parseDouble(txtConsoMois);

                                                        if (intConsoMois>0){
                                                            double comparatifmois = (finalIntConsoUser*100/intConsoMois)-100;
                                                            TextView prevMonth = root.findViewById(R.id.totMois);
                                                            prevMonth.setText(comparatifmois+"%");
                                                        }else {
                                                            TextView prevMonth = root.findViewById(R.id.totMois);
                                                            prevMonth.setText(0+"%");
                                                        }



                                                    }
                                                } else {
                                                    Log.d("TEST", "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });


                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });



        }else {
            Snackbar snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.no_account), Snackbar.LENGTH_LONG);snackBar.show();
        }
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        ImageButton planteV = root.findViewById(R.id.PV);

        planteV.setColorFilter(filter);
        planteV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                Date now = new Date();


                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringplanteVSave = sharedPreferences.getString("planteVSave", String.valueOf(ecoCO2[0]));
                String stringplanteV = sharedPreferences.getString("planteVerte", "0");

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         isWeek[0] = true;
                         isMonth[0] = false;
                         isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });


                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST1");
                        intent.putExtra("texte","TEST1");

                        cancelAlarmIfExists(getContext(),0,intent);

                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),0,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (!newdef.isEmpty()){
                            editor.putString("planteVerte",newdef);
                        }
                        editor.putString("planteVSave", String.valueOf(ecoCO2[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                        Log.d("minteur", String.valueOf(calendar.getTime()));

                    }
                });




                double doublePlanteVSave = Double.parseDouble(stringplanteVSave);
                double currentPlanteV = ecoCO2[0]-doublePlanteVSave;

                titleBadges.setText(getString(R.string.plantV));
                descBadges.setText(getString(R.string.plantVDesc));
                statueBadges.setText(currentPlanteV+" < "+stringplanteV);
                imgBadges.setImageResource(R.drawable.ic_plantev);
                bottomSheetDialog.show();
            }
        });
        ImageButton qm = root.findViewById(R.id.QM);
        qm.setColorFilter(filter);
        qm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);



                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();


                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });


                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST2");
                        intent.putExtra("texte","TEST2");

                        cancelAlarmIfExists(getContext(),1,intent);

                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),1,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("moyenneQuizz",newdef);
                        }
                        editor.putString("moyenneQuizzSave", String.valueOf(moyenneQuizz[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                titleBadges.setText(getString(R.string.QM));
                String stringmoyenneQuizzSave = sharedPreferences.getString("moyenneQuizzSave", String.valueOf(moyenneQuizz[0]));
                String stringmoyenneQuizz = sharedPreferences.getString("moyenneQuizz", "0");

                double doublemoyenneQuizzSave = Double.parseDouble(stringmoyenneQuizzSave);
                double currentmoyenneQuizz = moyenneQuizz[0]-doublemoyenneQuizzSave;

                statueBadges.setText(getString(R.string.QMSTATUE)+" "+currentmoyenneQuizz+"/"+stringmoyenneQuizz);
                descBadges.setText(getString(R.string.QMDESC));
                imgBadges.setImageResource(R.drawable.ic_lepers);
                bottomSheetDialog.show();
            }
        });


        ImageButton voitc = root.findViewById(R.id.VC);
        voitc.setColorFilter(filter);

        voitc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST3");
                        intent.putExtra("texte","TEST3");

                        cancelAlarmIfExists(getContext(),2,intent);

                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),2,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("voitconso",newdef);
                        }
                        editor.putString("voitconsoSave", String.valueOf(voitconso[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringvoitconso = sharedPreferences.getString("voitconso","100");
                String stringvoitconsoSave = sharedPreferences.getString("voitconsoSave", String.valueOf(voitconso[0]));
                double doublevoitconsoSave = Double.parseDouble(stringvoitconsoSave);
                double currentvoitconso = voitconso[0]-doublevoitconsoSave;

                titleBadges.setText(getString(R.string.VOITC));
                statueBadges.setText(Math.round(currentvoitconso)+" < "+stringvoitconso+" kg/CO2");
                descBadges.setText(getString(R.string.VOITCDESC));
                imgBadges.setImageResource(R.drawable.ic_voit);

                bottomSheetDialog.show();
            }
        });
        ImageButton btnimgbonneH = root.findViewById(R.id.BH);
        btnimgbonneH.setColorFilter(filter);

        btnimgbonneH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST4");
                        intent.putExtra("texte","TEST4");

                        cancelAlarmIfExists(getContext(),3,intent);

                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),3,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("bonneH",newdef);
                        }
                        editor.putString("bonneHSAVE", String.valueOf(bonneH[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });
                String stringbonneH = sharedPreferences.getString("bonneH","5");
                String stringbonneHSave = sharedPreferences.getString("bonneHSave", String.valueOf(bonneH[0]));
                double doublebonneHSave = Double.parseDouble(stringbonneHSave);
                double currentbonneH = bonneH[0]-doublebonneHSave;

                titleBadges.setText(getString(R.string.bonneH));
                statueBadges.setText(currentbonneH+"/"+bonneH[0]);
                descBadges.setText(getString(R.string.bonneHDESC));
                imgBadges.setImageResource(R.drawable.ic_bonneh);

                bottomSheetDialog.show();
            }
        });

        ImageButton avc = root.findViewById(R.id.AC);
        avc.setColorFilter(filter);

        avc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();


                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST5");
                        intent.putExtra("texte","TEST5");

                        cancelAlarmIfExists(getContext(),4,intent);

                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),4,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("avconso",newdef);
                        }
                        editor.putString("avconsoSave", String.valueOf(avconso[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringdavconso = sharedPreferences.getString("avconso","100");
                String stringdavconsoSave = sharedPreferences.getString("avconsoSave", String.valueOf(avconso[0]));

                double doubledavconsoSave = Double.parseDouble(stringdavconsoSave);
                double currentavconso = avconso[0]-doubledavconsoSave;

                titleBadges.setText(getString(R.string.avc));
                statueBadges.setText(Math.round(currentavconso)+" <"+stringdavconso+" kg/CO2");
                descBadges.setText(getString(R.string.avcDESC));
                imgBadges.setImageResource(R.drawable.ic_pilote);

                bottomSheetDialog.show();
            }
        });
        ImageButton globeT = root.findViewById(R.id.GT);
        globeT.setColorFilter(filter);
        globeT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST6");
                        intent.putExtra("texte","TEST6");

                        cancelAlarmIfExists(getContext(),5,intent);

                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),5,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("globeT",newdef);
                        }
                        editor.putString("globeTSave", String.valueOf(intkilometreUser[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });



                titleBadges.setText(getString(R.string.globeT));
                String stringglobeT = sharedPreferences.getString("globeT","1000");
                String stringglobeTSave = sharedPreferences.getString("globeTSave", String.valueOf(intkilometreUser[0]));

                double DoubleglobeTSave = Double.parseDouble(stringglobeTSave);
                double currentglobT = intkilometreUser[0]-DoubleglobeTSave;

                statueBadges.setText(Math.round(currentglobT)+" > "+stringglobeT+ " km");
                descBadges.setText(getString(R.string.globeTDESC));
                imgBadges.setImageResource(R.drawable.ic_globet);
                bottomSheetDialog.show();
            }
        });

        ImageButton motoc = root.findViewById(R.id.MC);
        motoc.setColorFilter(filter);


        motoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST7");
                        intent.putExtra("texte","TEST7");

                        cancelAlarmIfExists(getContext(),6,intent);

                        Date now = new Date();
                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),6,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("deuxRconso",newdef);
                        }
                        editor.putString("deuxRconsoSave", String.valueOf(ecoCO2[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringdeuxRconso = sharedPreferences.getString("deuxRconso","100");
                String stringdeuxRconsoSave = sharedPreferences.getString("deuxRconsoSave", String.valueOf(deuxRconso[0]));

                double doubledeuxRconsoSave = Double.parseDouble(stringdeuxRconsoSave)*1000;
                double currentdeuxRconso = deuxRconso[0]-doubledeuxRconsoSave;

                titleBadges.setText(getString(R.string.motoc));
                statueBadges.setText(Math.round(currentdeuxRconso)+" <"+stringdeuxRconso+" kg/CO2");
                descBadges.setText(getString(R.string.motocDESC));
                imgBadges.setImageResource(R.drawable.ic_moto);

                bottomSheetDialog.show();
            }
        });

        ImageButton covoit = root.findViewById(R.id.CV);
        covoit.setColorFilter(filter);

        covoit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST8");
                        intent.putExtra("texte","TEST8");

                        cancelAlarmIfExists(getContext(),7,intent);

                        Date now = new Date();
                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),7,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("covoit",newdef);
                        }
                        editor.putString("covoitSave", String.valueOf(codist[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringcovoit = sharedPreferences.getString("covoit","100");
                String stringcovoitSave = sharedPreferences.getString("covoitSave", String.valueOf(codist[0]));

                double doublecovoitSave = Double.parseDouble(stringcovoitSave);

                double currentcovoit = codist[0]-doublecovoitSave;

                titleBadges.setText(getString(R.string.covoit));
                statueBadges.setText(Math.round(currentcovoit)+"/"+stringcovoit+" km");
                descBadges.setText(getString(R.string.covoitDESC));
                imgBadges.setImageResource(R.drawable.ic_covoit_1);
                bottomSheetDialog.show();

            }
        });
        ImageButton rosaP = root.findViewById(R.id.RP);
        rosaP.setColorFilter(filter);

        rosaP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();


                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST9");
                        intent.putExtra("texte","TEST9");

                        cancelAlarmIfExists(getContext(),8,intent);

                        Date now = new Date();
                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),8,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("busdist",newdef);
                        }
                        editor.putString("busdistSave", String.valueOf(busdist[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringbusdist = sharedPreferences.getString("busdist","100");
                String stringbusdistSave = sharedPreferences.getString("busdistSave", String.valueOf(busdist[0]));

                double doublebusdistSave = Double.parseDouble(stringbusdistSave);
                double currentbustdist = busdist[0]-doublebusdistSave;

                titleBadges.setText(getString(R.string.rosap));
                statueBadges.setText(Math.round(currentbustdist)+"/"+stringbusdist+" km");
                descBadges.setText(getString(R.string.rosapDESC));
                imgBadges.setImageResource(R.drawable.ic_imperial);
                bottomSheetDialog.show();
            }
        });

        ImageButton interR = root.findViewById(R.id.IR);
        interR.setColorFilter(filter);

        interR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST10");
                        intent.putExtra("texte","TEST10");

                        cancelAlarmIfExists(getContext(),9,intent);

                        Date now = new Date();
                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),9,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("raildist",newdef);
                        }
                        editor.putString("raildistSave", String.valueOf(raildist[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringraildist = sharedPreferences.getString("raildist","100");
                String stringraildistSave = sharedPreferences.getString("raildistSave", String.valueOf(raildist[0]));

                double doubleraildistSave = Double.parseDouble(stringraildistSave);

                double currentraildist = raildist[0]-doubleraildistSave;

                titleBadges.setText(getString(R.string.interR));
                statueBadges.setText(Math.round(currentraildist)+"/"+stringraildist+" km");
                descBadges.setText(getString(R.string.interRDESC));
                imgBadges.setImageResource(R.drawable.ic_harry);

                bottomSheetDialog.show();
            }
        });


        ImageButton marcheB = root.findViewById(R.id.MB);
        marcheB.setColorFilter(filter);

        marcheB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST11");
                        intent.putExtra("texte","TEST11");

                        cancelAlarmIfExists(getContext(),10,intent);

                        Date now = new Date();
                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),10,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("marchedist",newdef);
                        }
                        editor.putString("marchedistSave", String.valueOf(marchedist[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringmarchedist = sharedPreferences.getString("marchedist","100");
                String stringmarchedistSave = sharedPreferences.getString("marchedistSave", String.valueOf(marchedist[0]));

                double doublemarchedistSave = Double.parseDouble(stringmarchedistSave);

                double currentmarchedist = marchedist[0]-doublemarchedistSave;

                titleBadges.setText(getString(R.string.marcheB));
                statueBadges.setText(Math.round(currentmarchedist)+"/"+stringmarchedist+" km");
                descBadges.setText(getString(R.string.marcheBDESC));
                imgBadges.setImageResource(R.drawable.ic_gump_1);
                bottomSheetDialog.show();
            }
        });




        ImageButton maillotJ = root.findViewById(R.id.MJ);
        maillotJ.setColorFilter(filter);

        maillotJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                RadioButton btnsemaine = bottomSheetDialog.findViewById(R.id.btnsemaine);
                btnsemaine.toggle();

                btnsemaine.toggle();

                final Boolean[] isWeek = {true};
                final Boolean[] isMonth = {false};
                final Boolean[] isYear = {false};


                btnsemaine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = true;
                        isMonth[0] = false;
                        isYear[0] = false;
                    }
                });


                RadioButton btnmois = bottomSheetDialog.findViewById(R.id.btnmois);
                btnmois.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = true;
                        isYear[0] = false;

                    }
                });

                RadioButton btnannee = bottomSheetDialog.findViewById(R.id.btnannee);
                btnannee.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isWeek[0] = false;
                        isMonth[0] = false;
                        isYear[0] = true;

                    }
                });
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"Reminder Set",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                        intent.putExtra("title","TEST12");
                        intent.putExtra("texte","TEST12");

                        cancelAlarmIfExists(getContext(),11,intent);

                        Date now = new Date();
                        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),11,intent,0);
                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);

                        if (isWeek[0]){
                            calendar.add(Calendar.WEEK_OF_MONTH, 1);
                        }else if(isMonth[0]){
                            calendar.add(Calendar.MONTH, 1);
                        }else if(isYear[0]){
                            calendar.add(Calendar.YEAR, 1);

                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

                        EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);

                        String newdef = ndef.getText().toString();
                        if (!newdef.isEmpty()){
                            editor.putString("velodist",newdef);
                        }
                        editor.putString("velodistSave", String.valueOf(velodist[0]));
                        editor.apply();

                        Intent intent2 = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent2);
                    }
                });

                String stringvelodist = sharedPreferences.getString("velodist","100");
                String stringvelodistSave = sharedPreferences.getString("velodistSave", String.valueOf(velodist[0]));

                double doublevelodistSave = Double.parseDouble(stringvelodistSave);

                double currentvelodist = velodist[0]-doublevelodistSave;

                titleBadges.setText(getString(R.string.maillotJ));
                statueBadges.setText(Math.round(currentvelodist)+"/"+stringvelodist+" km");
                descBadges.setText(getString(R.string.maillotJDESC));
                imgBadges.setImageResource(R.drawable.ic_maillotj);
                bottomSheetDialog.show();

            }
        });



        return root;
    }


    private void takepictureIntent(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if( intent.resolveActivity(getActivity().getPackageManager())!= null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE){
            switch (resultCode){
                case RESULT_OK:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    proimg.setImageBitmap(bitmap);
                    handleUpload(bitmap);
            }
        }
    }

    private void handleUpload(Bitmap bitmap){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profilImages")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        getDownloadUrl(reference);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getDownloadUrl(StorageReference reference){

        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        setUserProfilUrl(uri);
                    }
                });
    }

    private void setUserProfilUrl(Uri uri){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getContext(),getString(R.string.img_sucess), Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getContext(),getString(R.string.img_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "DefiReminderChannel";
            String description = "Channel for Defis Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("NotificationDefis",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void cancelAlarmIfExists(Context mContext, int requestCode, Intent intent){
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent,0);
            AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}