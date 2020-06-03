package com.nathanael.pierregignoux.ui.profil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
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
import com.nathanael.pierregignoux.ModifProfil;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ProfilFragment extends Fragment {

    private ProfilViewModel profilViewModel;

    TextView name;

    private int REQUEST_IMAGE_CAPTURE = 1001;
    ImageView proimg;
    ArrayList<String> trajtype;
    List<Trajet> items = new ArrayList<>();

    ArrayList<Double> trajdistvelo;

    boolean habituedesrails;
    boolean partagevoiture;
    boolean bonnehabitude;
    boolean plante;
    boolean six;
    boolean sept;
    boolean huit;
    int counthabitude = 0;


    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profilViewModel = ViewModelProviders.of(this).get(ProfilViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profil, container, false);
        trajtype = new ArrayList<>();
        trajdistvelo = new ArrayList<Double>();

        db = FirebaseFirestore.getInstance();
        name= root.findViewById(R.id.profilnom);
        proimg = root.findViewById(R.id.imageprofil);
        habituedesrails = false;
        partagevoiture = false;
        plante = false;
        bonnehabitude = false;

        final double[] velodist = {0};
        final double[] marchedist = {0};
        final double[] raildist = {0};
        final double[] busdist = {0};
        final double[] codist = {0};


        final double[] deuxRconso = {0};
        final double[] voitconso = {0};
        final double[] avconso = {0};

        final double[] moyenneQuizz = {0};
        final double[] intkilometreUser = {0};


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
                                        counthabitude++;
                                    }
                                }
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                                String stringbonneH = sharedPreferences.getString("bonneH","5");
                                double doublebonneH = Double.parseDouble(stringbonneH);
                                if (counthabitude >= doublebonneH){
                                    bonnehabitude = true;
                                }
                                ImageButton bonneH = root.findViewById(R.id.BH);
                                if (bonnehabitude){
                                    bonneH.clearColorFilter();
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
                                    Log.d("stringvelodist",stringvelodist);
                                    double doublevelodist = Double.parseDouble(stringvelodist);
                                    if (velodist[0] >= doublevelodist){
                                        maillotJ.clearColorFilter();
                                    }
                                    ImageButton marcheB = root.findViewById(R.id.MB);

                                    String stringmarchedist = sharedPreferences.getString("marchedist","100");
                                    double doublemarchedist = Double.parseDouble(stringmarchedist);
                                    if (marchedist[0] >= doublemarchedist){
                                        marcheB.clearColorFilter();
                                    }


                                    ImageButton interR = root.findViewById(R.id.IR);
                                    String stringraildist = sharedPreferences.getString("raildist","100");
                                    double doubleraildist = Double.parseDouble(stringraildist);
                                    if (raildist[0] >= doubleraildist){
                                        interR.setBackgroundColor(getContext().getColor(R.color.colorAccent));
                                    }

                                    String stringbusdist = sharedPreferences.getString("busdist","100");
                                    double doublebusdist = Double.parseDouble(stringbusdist);
                                    ImageButton rosaP = root.findViewById(R.id.RP);
                                    if (busdist[0] >= doublebusdist){
                                        rosaP.clearColorFilter();
                                    }
                                    String stringcovoit = sharedPreferences.getString("covoit","100");
                                    double doublecovoit = Double.parseDouble(stringcovoit);
                                    ImageButton covoit = root.findViewById(R.id.CV);
                                    if (codist[0] >= doublecovoit){
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
                                    double doubledeuxRconso = Double.parseDouble(stringdeuxRconso)*100;
                                    if (deuxRconso[0] <= doubledeuxRconso){
                                        motoc.clearColorFilter();
                                    }
                                    ImageButton voitc = root.findViewById(R.id.VC);
                                    String stringdvoitconso = sharedPreferences.getString("voitconso","100");
                                    double doubledvoitconso = Double.parseDouble(stringdvoitconso)*100;
                                    if (voitconso[0] <= doubledvoitconso){
                                        voitc.clearColorFilter();
                                    }

                                    ImageButton avc = root.findViewById(R.id.AC);
                                    String stringdavconso = sharedPreferences.getString("avconso","100");
                                    double doubledavconso = Double.parseDouble(stringdavconso)*100;
                                    if (avconso[0] <= doubledavconso){
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
                                double doubledmoyenneQuizz = Double.parseDouble(stringmoyenneQuizz);

                                if (moyenneQuizz[0] >= doubledmoyenneQuizz){
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
                                    Double DoubleglobeT = Double.parseDouble(stringglobeT);
                                    if (intkilometreUser[0] >= DoubleglobeT){
                                        globeT.clearColorFilter();
                                    }



                                    String eco_co2 = document.getString("eco_co2");
                                    TextView toteco_co2 = root.findViewById(R.id.totecoCO2);
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
                                        ImageButton planteV = root.findViewById(R.id.PV);
                                        plante = true;
                                        if (plante){
                                            planteV.clearColorFilter();
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
                                                        Double intConsoMois= Double.parseDouble(txtConsoMois);
                                                        Log.d("compamois", String.valueOf(intConsoMois));
                                                        Log.d("compamois", String.valueOf(finalIntConsoUser));
                                                        Double comparatifmois = (finalIntConsoUser*100/intConsoMois)-100;
                                                        TextView prevMonth = root.findViewById(R.id.totMois);
                                                        prevMonth.setText(comparatifmois+"%");


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
                Log.d("btnimage","click");
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.activity_badges);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                ImageView imgBadges = bottomSheetDialog.findViewById(R.id.imgBadges);
                TextView titleBadges = bottomSheetDialog.findViewById(R.id.titleBadges);
                TextView statueBadges = bottomSheetDialog.findViewById(R.id.statueBadges);
                TextView descBadges = bottomSheetDialog.findViewById(R.id.descBadges);

                LinearLayout modif = bottomSheetDialog.findViewById(R.id.modifdefi);
                modif.removeAllViews();

                titleBadges.setText(getString(R.string.plantV));
                descBadges.setText(getString(R.string.plantVDesc));
                statueBadges.setText("");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click QM");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("moyenneQuizz",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("moyenneQuizz","100");

                titleBadges.setText(getString(R.string.QM));
                statueBadges.setText(getString(R.string.QMSTATUE)+" "+moyenneQuizz[0]+"/"+stringvelodist);
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click voitconso");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("voitconso",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("voitconso","100");

                titleBadges.setText(getString(R.string.VOITC));
                statueBadges.setText(Math.round(voitconso[0])+" < "+stringvelodist+" kg/CO2");
                descBadges.setText(getString(R.string.VOITCDESC));
                imgBadges.setImageResource(R.drawable.ic_voit);

                bottomSheetDialog.show();
            }
        });
        ImageButton bonneH = root.findViewById(R.id.BH);
        bonneH.setColorFilter(filter);

        bonneH.setOnClickListener(new View.OnClickListener() {
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
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click voitconso");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("bonneH",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("bonneH","10");

                titleBadges.setText(getString(R.string.bonneH));
                statueBadges.setText(counthabitude+"/"+stringvelodist);
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click avconso");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("avconso",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("avconso","100");

                titleBadges.setText(getString(R.string.avc));
                statueBadges.setText(Math.round(avconso[0])+" <"+stringvelodist+" kg/CO2");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click Globt");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("globeT",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });



                titleBadges.setText(getString(R.string.globeT));
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringglobeT = sharedPreferences.getString("globeT","1000");
                statueBadges.setText(Math.round(intkilometreUser[0])+" > "+stringglobeT+ " km");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click avconso");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("deuxRconso",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("deuxRconso","100");

                titleBadges.setText(getString(R.string.motoc));
                statueBadges.setText(Math.round(deuxRconso[0])+" <"+stringvelodist+" kg/CO2");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click covoit");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("covoit",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("covoit","100");

                titleBadges.setText(getString(R.string.covoit));
                statueBadges.setText(Math.round(codist[0])+"/"+stringvelodist+" km");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click busdist");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("busdist",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("busdist","100");

                titleBadges.setText(getString(R.string.rosap));
                statueBadges.setText(Math.round(busdist[0])+"/"+stringvelodist+" km");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click busdist");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("raildist",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("raildist","100");

                titleBadges.setText(getString(R.string.interR));
                statueBadges.setText(Math.round(raildist[0])+"/"+stringvelodist+" km");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click marchedist");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("marchedist",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("marchedist","100");

                titleBadges.setText(getString(R.string.marcheB));
                statueBadges.setText(Math.round(marchedist[0])+"/"+stringvelodist+" km");
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

                Button savedef = bottomSheetDialog.findViewById(R.id.btnsavedef);
                EditText ndef = bottomSheetDialog.findViewById(R.id.textDefis);
                savedef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("defi","click velo");
                        String newdef = ndef.getText().toString();
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("velodist",newdef);
                        editor.apply();
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                    }
                });

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Defis",MODE_PRIVATE);
                String stringvelodist = sharedPreferences.getString("velodist","100");

                titleBadges.setText(getString(R.string.maillotJ));
                statueBadges.setText(Math.round(velodist[0])+"/"+stringvelodist+" km");
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
}