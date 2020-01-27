package com.example.pierregignoux.ui.profil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.pierregignoux.ForgotPass;
import com.example.pierregignoux.ModifProfil;
import com.example.pierregignoux.R;
import com.example.pierregignoux.models.direction.Trajet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;
import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

public class ProfilFragment extends Fragment {

    private ProfilViewModel profilViewModel;

    ConstraintLayout consoExpandableView;
    Button consoArrowButton;
    CardView consoCardView;
    TextView name;

    private int REQUEST_IMAGE_CAPTURE = 1001;
    ImageView proimg;

    CardView euroCardView;

    ConstraintLayout consoLayout;
    ConstraintLayout euroLayout;

    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private BarChart consochart;
    private PieChart consotypechart;
    private CombinedChart comparechart;
    private BarChart eurochart;



    final List<Trajet> items = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profilViewModel = ViewModelProviders.of(this).get(ProfilViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profil, container, false);

        db = FirebaseFirestore.getInstance();
        ArrayList<PieEntry> dataVals1 = new ArrayList<>();
        ArrayList<BarEntry> dataVals2 = new ArrayList<>();
        ArrayList<BarEntry> dataVals3 = new ArrayList<>();
        ArrayList<BarEntry> dataVals4 = new ArrayList<>();
        ArrayList<Entry> dataVals5 = new ArrayList<>();

        name= root.findViewById(R.id.profilnom);

        consoExpandableView = root.findViewById(R.id.consoExpandableView);
        consoArrowButton = root.findViewById(R.id.consoArrowButton);
        consoCardView = root.findViewById(R.id.consoCardView);

        consoLayout = root.findViewById(R.id.consoLayout);
        euroLayout = root.findViewById(R.id.euroLayout);

        String objectif = getString(R.string.objectif);
        String consommation = getString(R.string.consommation);
        String France = getString(R.string.France);
        String Chine = getString(R.string.Chine);
        String US = getString(R.string.US);
        String Angleterre = getString(R.string.Angleterre);

        String label = getString(R.string.label_fragment_profil);
        getActivity().setTitle(label);





        euroCardView = root.findViewById(R.id.euroCardView);

        consoArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (consoExpandableView.getVisibility() == View.GONE){
                    TransitionManager.beginDelayedTransition(consoCardView, new AutoTransition());
                    consoExpandableView.setVisibility(View.VISIBLE);
                    consoArrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                }else{
                    TransitionManager.beginDelayedTransition(consoCardView, new AutoTransition());
                    consoExpandableView.setVisibility(View.GONE);
                    consoArrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });


        proimg = root.findViewById(R.id.imageprofil);



        if (user!= null) {
            String uid = user.getUid();

            TextView modprofil = root.findViewById(R.id.modifprofil);
            modprofil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getContext(), ModifProfil.class);
                    startActivity(intent);
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

            db.collection("trajets")
                    .whereEqualTo("auteurTrajet", uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                Map<String, Integer> sum = null;
                                for (QueryDocumentSnapshot document : task.getResult()) {


                                    Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("kilometreTrajet"),document.getString("imageTrajet"));

                                    items.add(nTrajet);

                                    sum = items.stream().collect(
                                            Collectors.groupingBy(Trajet::getVehicule, Collectors.summingInt(Trajet::getConsommation)));

                                }

                                if (sum != null) {
                                    sum.forEach((k, v) -> {
                                        Log.d("please", "Item : " + k + " Count : " + v);

                                        dataVals1.add(new PieEntry(v, "" + k));


                                    });
                                }


                                consotypechart = root.findViewById(R.id.consotypeChart);
                                PieDataSet pieDataSet1 = new PieDataSet(dataVals1, "");

                                consotypechart.setUsePercentValues(true);
                                pieDataSet1.setColors(PIERRE_COLORS);



                                PieData pieData1 = new PieData(pieDataSet1);
                                consotypechart.setData(pieData1);
                                consotypechart.invalidate();

                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });



            db.collection("users")
                    .whereEqualTo(FieldPath.documentId(), uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String consoUser = document.getString("conso_co2");
                                    String objCo2User = document.getString("obj_co2");

                                    double interConsoUser = Double.parseDouble(consoUser);
                                    double interobjCo2User = Double.parseDouble(objCo2User);

                                    int finalConsoUser = (int)interConsoUser;
                                    int finalobjCo2User = (int)interobjCo2User;



                                    dataVals2.add(new BarEntry(1, finalConsoUser));
                                    dataVals2.add(new BarEntry(2, finalobjCo2User));


                                    consochart = root.findViewById(R.id.consoChart);

                                    String[] nBar = new String[] {
                                            objectif, consommation
                                    };

                                    XAxis xAxis = consochart.getXAxis();
                                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                    xAxis.setAxisMinimum(0.5f);
                                    xAxis.setGranularity(1f);
                                    xAxis.setValueFormatter(new ValueFormatter() {
                                        @Override
                                        public String getFormattedValue(float value) {
                                            return nBar[(int) value % nBar.length];
                                        }
                                    });

                                    BarDataSet barDataSet2 = new BarDataSet(dataVals2,"");

                                    if(finalConsoUser > finalobjCo2User){
                                        barDataSet2.setColors(PIERRE_COLORS1);
                                    }else if(finalConsoUser < finalobjCo2User){
                                        barDataSet2.setColors(PIERRE_COLORS2);
                                    }else {
                                        barDataSet2.setColors(PIERRE_COLORS3);
                                    }


                                    BarData barData2 = new BarData(barDataSet2);
                                    consochart.setData(barData2);
                                    consochart.invalidate();






                                }




                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });


            db.collection("parametres")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {



                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String consoChine = document.getString("consoChine");
                                    String consoUS = document.getString("consoUS");
                                    String consoUK = document.getString("consoUK");
                                    String consoFrance = document.getString("consoFrance");

                                    double doubleconsoChine = Double.parseDouble(consoChine);
                                    double doubleconsoUS = Double.parseDouble(consoUS);
                                    double doubleconsoUK = Double.parseDouble(consoUK);
                                    double doubleconsoFrance = Double.parseDouble(consoFrance);

                                    int finalconsoChine = (int)doubleconsoChine;
                                    int finalconsoUS = (int)doubleconsoUS;
                                    int finalconsoUK = (int)doubleconsoUK;
                                    int finalconsoFrance = (int)doubleconsoFrance;


                                    db.collection("users")
                                            .whereEqualTo(FieldPath.documentId(), uid)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {

                                                        int finalconsoUser =0;

                                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                                            String consoUser = document.getString("conso_co2");
                                                            double doubleconsoUser = Double.parseDouble(consoUser);
                                                            finalconsoUser = (int)doubleconsoUser;
                                                            dataVals5.add(new Entry(1, finalconsoUser));
                                                            dataVals5.add(new Entry(2, finalconsoUser));
                                                            dataVals5.add(new Entry(3, finalconsoUser));
                                                            dataVals5.add(new Entry(4, finalconsoUser));






                                                        }
                                                    } else {
                                                        Log.d("TEST", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });



                                    int x = 1;
                                    dataVals3.add(new BarEntry(x,finalconsoChine));
                                    dataVals3.add(new BarEntry(2,finalconsoUS));
                                    dataVals3.add(new BarEntry(3,finalconsoUK));
                                    dataVals3.add(new BarEntry(4,finalconsoFrance));




                                }




                                LineData d = new LineData();

                                LineDataSet set = new LineDataSet(dataVals5, "Line");
//                                set.setColor(Color.rgb(131, 199, 213));
                                set.setColors(Color.rgb(131, 199, 213));
                                set.setLineWidth(2.5f);
                                set.setCircleColor(Color.rgb(131, 199, 213));
                                set.setCircleRadius(5f);
                                set.setFillColor(Color.rgb(131, 199, 213));
                                set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                                set.setDrawValues(true);
                                set.setValueTextSize(10f);
                                set.setValueTextColor(Color.rgb(127, 127, 127));

                                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                                d.addDataSet(set);




                                BarDataSet set1 = new BarDataSet(dataVals3, "Bar");
                                //set1.setColor(Color.rgb(60, 220, 78));
                                set1.setColors(PIERRE_COLORS);
                                set1.setValueTextColor(Color.rgb(127, 127, 127));
                                set1.setValueTextSize(10f);
                                set1.setAxisDependency(YAxis.AxisDependency.LEFT);

                                float barWidth = 0.45f; // x2 dataset


                                BarData dbar = new BarData(set1);
                                dbar.setBarWidth(barWidth);

                                String[] nPays = new String[] {
                                        France,Chine,US,Angleterre
                                };

                                comparechart = root.findViewById(R.id.consocompareChart);
                                comparechart.setDrawOrder(new CombinedChart.DrawOrder[]{
                                        CombinedChart.DrawOrder.BAR,  CombinedChart.DrawOrder.LINE
                                });
                                Legend l = comparechart.getLegend();
                                l.setWordWrapEnabled(true);
                                l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                                l.setOrientation(Legend.LegendOrientation.HORIZONTAL);

                                YAxis rightAxis = comparechart.getAxisRight();
                                rightAxis.setDrawGridLines(false);
                                rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                                YAxis leftAxis = comparechart.getAxisLeft();
                                leftAxis.setDrawGridLines(false);
                                leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                                XAxis xAxis = comparechart.getXAxis();
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setAxisMinimum(1f);
                                xAxis.setGranularity(1f);
                                xAxis.setValueFormatter(new ValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value) {
                                        return nPays[(int) value % nPays.length];
                                    }
                                });

                                CombinedData data = new CombinedData();

                                data.setData(d);
                                data.setData(dbar);


                                comparechart.setData(data);
                                comparechart.invalidate();
//                                BarDataSet barDataSet = new BarDataSet(dataVals3, "Votre connsommation face au reste du monde");
//                                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
//
//                                BarData barData = new BarData(barDataSet);
//                                comparechart.setData(barData);
//                                comparechart.invalidate();



                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });


            db.collection("users")
                    .whereEqualTo(FieldPath.documentId(), uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String euroUser = document.getString("eco_euro");
                                    String objEuroUser = document.getString("obj_euro");

                                    double interEuroUser = Double.parseDouble(euroUser);
                                    double interobjEuroUser = Double.parseDouble(objEuroUser);

                                    int finalEuroUser = (int)interEuroUser;
                                    int finalobjEuroUser = (int)interobjEuroUser;



                                    dataVals4.add(new BarEntry(1, finalEuroUser));
                                    dataVals4.add(new BarEntry(2, finalobjEuroUser));



                                    eurochart = root.findViewById(R.id.euroChart);


                                    String[] nBar = new String[] {
                                            objectif,consommation
                                    };

                                    XAxis xAxis = eurochart.getXAxis();
                                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                    xAxis.setAxisMinimum(0.5f);
                                    xAxis.setGranularity(1f);
                                    xAxis.setValueFormatter(new ValueFormatter() {
                                        @Override
                                        public String getFormattedValue(float value) {
                                            return nBar[(int) value % nBar.length];
                                        }
                                    });

                                    BarDataSet barDataSet3 = new BarDataSet(dataVals4, "");

                                    if(finalEuroUser > finalobjEuroUser){
                                        barDataSet3.setColors(PIERRE_COLORS2);
                                    }else if(finalEuroUser < finalobjEuroUser){
                                        barDataSet3.setColors(PIERRE_COLORS3);
                                    }else {
                                        barDataSet3.setColors(PIERRE_COLORS3);
                                    }

                                    BarData barData3 = new BarData(barDataSet3);
                                    eurochart.setData(barData3);
                                    eurochart.invalidate();


                                }



                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });



        }else {
            Snackbar snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.no_account), Snackbar.LENGTH_LONG);snackBar.show();
        }

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

    public static final int[] PIERRE_COLORS1 = {
            rgb("#FF9AA2"), rgb("#83C7D5")
    };

    public static final int[] PIERRE_COLORS2 = {
            rgb("#B5EAD7"), rgb("#83C7D5")
    };

    public static final int[] PIERRE_COLORS3 = {
            rgb("#FFDAC1"), rgb("#83C7D5")
    };

    public static final int[] PIERRE_COLORS = {
            rgb("#83C7D5"), rgb("#FF9AA2"),rgb("#FFB7B2"),rgb("#FFDAC1"),rgb("#B5EAD7")
    };




}