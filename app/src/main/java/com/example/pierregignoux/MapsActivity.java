package com.example.pierregignoux;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.pierregignoux.models.direction.Trajet;
import com.example.pierregignoux.service.GPSService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import com.example.pierregignoux.models.direction.DirectionFinder;
import com.example.pierregignoux.models.direction.DirectionFinderListener;
import com.example.pierregignoux.models.direction.Route;
import com.example.pierregignoux.service.FetchAddressIntentService;
import com.example.pierregignoux.utils.Connections;
import com.example.pierregignoux.utils.Constants;
import com.example.pierregignoux.utils.PermissionGPS;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment;
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.StringValue;
import com.squareup.picasso.Picasso;

import org.mariuszgromada.math.mxparser.*;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

import static java.lang.Integer.parseInt;




public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, DirectionFinderListener, BottomSheetDialog.BottomSheetListener,BottomSheetDialog2.BottomSheetListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15f;
    private static final long UPDATE_INTERVAL = 500;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 5;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static boolean gpsFirstOn = true;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ImageView vehiculeimage;
    List<Location> locationArrayList;
    LinearLayout linearLayout;
    EditText nbpersonne;
    long starttime = 0;
    long endtime = 0;
    ListView listView;
    int click = 0;
    ArrayList<String> mTitle = new ArrayList<>();
    ArrayList<String> mKilometre = new ArrayList<>();
    ListAdapter listAdapter;
    private LatLng LocationA = new LatLng(46.227638,2.213749);
    private FirebaseFirestore db;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProvider;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location locationgps;
    private ResultReceiver resultReceiver;
    private Marker selectedMarker;
    private LatLng searchLocation;
    private LatLng searchLocation2;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private BroadcastReceiver broadcastReceiver;
    private String Disttrack;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        loadData();

        db = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        if (!Connections.checkConnection(this)) {
            Toast.makeText(this, "Erreur réseau vérifiez votre connexion", Toast.LENGTH_SHORT).show();
            finish();
        }

        init();






        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
                Toast.makeText(getApplicationContext(), addressOutput, Toast.LENGTH_SHORT).show();
            }
        };

        locationgps = new Location("Point A");


        vehiculeimage = findViewById(R.id.mapvehicule);

        final Intent intent = getIntent();
        final String vehicule_titre = intent.getStringExtra("Vehicule titre");
        String vehicule_image = intent.getStringExtra("Vehicule image");

        Picasso.get().load(vehicule_image).into(vehiculeimage);
        final String vehicule_methode = intent.getStringExtra("Vehicule methode");
        String vehicule_calc = intent.getStringExtra("Vehicule calcule");



        if (vehicule_calc.contains("Y")){

            linearLayout = findViewById(R.id.nbperslayout);

            nbpersonne = new EditText(MapsActivity.this);
            nbpersonne.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT

            ));

            linearLayout.addView(nbpersonne);

            nbpersonne.setHint(getString(R.string.nbpersonne));
            nbpersonne.setInputType(0x00000002);
            nbpersonne.setImeOptions(EditorInfo.IME_ACTION_DONE);;

        }

        listView = findViewById(R.id.listpref);

        listAdapter = new ListAdapter(this,mTitle,mKilometre);
        listAdapter.notifyDataSetChanged();
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mKilometre.get(position).equals("")){
                    ((TextView) findViewById(R.id.tvDistance)).setText(1+" Km");
                }else {
                    ((TextView) findViewById(R.id.tvDistance)).setText(mKilometre.get(position)+" Km");
                }

                TextView distance = ((TextView) findViewById(R.id.tvDistance));
                final Intent intentv = getIntent();
                final String vehicule_titre = intentv.getStringExtra("Vehicule titre");

                db.collection("vehicules")
                        .whereEqualTo("titreVehicule",vehicule_titre)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {

                                    for (QueryDocumentSnapshot document : task.getResult()) {


                                        String conso = document.getString("ConsoCalculeVehicule");
                                        String routedist = distance.getText().toString();
                                        String finalRouteDist = routedist.split(" ")[0];
                                        String conso2 = conso.replace("X",""+finalRouteDist+"");


                                        if (nbpersonne!= null)
                                        {
                                            final String[] conso3 = {""};

                                            final String aspassenger = nbpersonne.getText().toString();
                                            if (!aspassenger.equals("")){
                                                conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                                Expression e = new Expression(conso3[0]);
                                                String result = String.valueOf(e.calculate());
                                                ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                            }else {
                                                conso3[0] = conso2.replace("Y",""+1+"");
                                                Expression e = new Expression(conso3[0]);
                                                String result = String.valueOf(e.calculate());
                                                ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                            }
                                            Expression e = new Expression(conso3[0]);
                                            String result = String.valueOf(e.calculate());
                                            ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");

                                            nbpersonne.addTextChangedListener(new TextWatcher() {

                                                public void afterTextChanged(Editable s) {}

                                                public void beforeTextChanged(CharSequence s, int start,
                                                                              int count, int after) {

                                                }

                                                public void onTextChanged(CharSequence s, int start,
                                                                          int before, int count) {
                                                    final String aspassenger = nbpersonne.getText().toString();
                                                    if (!aspassenger.equals("")){
                                                        conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                                        Expression e = new Expression(conso3[0]);
                                                        String result = String.valueOf(e.calculate());
                                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                                    }else {
                                                        conso3[0] = conso2.replace("Y",""+1+"");
                                                        Expression e = new Expression(conso3[0]);
                                                        String result = String.valueOf(e.calculate());
                                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                                    }
                                                }
                                            });

                                        }else {

                                            Expression e = new Expression(conso2);
                                            String result = String.valueOf(e.calculate());
                                            ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                        }


                                    }
                                } else {
                                }
                            }
                        });
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int which_item = position;
                new AlertDialog.Builder(MapsActivity.this)
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .setTitle(getString(R.string.sure))
                        .setMessage(getString(R.string.deleteitem))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTitle.remove(which_item);
                                mKilometre.remove(which_item);
                                listAdapter.notifyDataSetChanged();
                                savedata();
                            }
                        })
                        .setNegativeButton(getString(R.string.no),null)
                        .show();
                return true;
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        setupAutoCompleteFragment();



        ImageButton fm = findViewById(R.id.fbitineraire);
        fm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDeviceLocation(true);

                if (searchLocation != null)
                {
                    final Intent intent = getIntent();
                    final String vehicule_methode = intent.getStringExtra("Vehicule methode");

                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+ searchLocation.latitude+","+searchLocation.longitude+"&mode="+vehicule_methode);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }

                }


            }
        });






        Button nodestination = findViewById(R.id.btnnodest);
        nodestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                starttime = System.currentTimeMillis();

                SharedPreferences sharedPreferences = getSharedPreferences("Time",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("starttime",starttime);
                editor.apply();

                Intent intent = new Intent(MapsActivity.this, GPSService.class);
                startService(intent);

//                nodestination.setEnabled(false);
//                nodestination.setBackgroundResource(R.color.colorGray);
//
//                LinearLayout layouttrak = findViewById(R.id.tracklayout);
//
//                ProgressBar load = new ProgressBar(MapsActivity.this);
//                TextView txtload = new TextView(MapsActivity.this);
//                layouttrak.setLayoutParams(new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                ));
//
//                layouttrak.addView(load);
//                layouttrak.addView(txtload);
//                txtload.setText("Enregistrement des données");
//                txtload.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                load.setIndeterminate(true);

            }
        });

        Button nodestination2 = findViewById(R.id.btnnodest2);
        nodestination2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endtime = System.currentTimeMillis();

                Intent intent = new Intent(MapsActivity.this, GPSService.class);
                stopService(intent);

                SharedPreferences sharedPreferences = getSharedPreferences("Time",MODE_PRIVATE);
                Long nstarttime = sharedPreferences.getLong("starttime",0);
                Log.d("nstarttime", String.valueOf(nstarttime));
                if (nstarttime == 0){
                    nstarttime = endtime;
                }

                Long time = endtime-nstarttime;
                Long intertime = time/1000;
                Long finaltime = intertime/60;

                sharedPreferences.edit().remove("starttime").apply();
                Log.d("nstarttime", String.valueOf(sharedPreferences.getLong("starttime",0)));


                TextView distance = ((TextView) findViewById(R.id.tvDistance));

                TextView textViewtime = ((TextView) findViewById(R.id.tvTime));
                textViewtime.setText(""+finaltime+" minutes");

                final Intent intentv = getIntent();
                final String vehicule_titre = intentv.getStringExtra("Vehicule titre");

                db.collection("vehicules")
                        .whereEqualTo("titreVehicule",vehicule_titre)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {

                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        String conso = document.getString("ConsoCalculeVehicule");
                                        String routedist = distance.getText().toString();
                                        String finalRouteDist = routedist.split(" ")[0];
                                        String conso2 = conso.replace("X",""+finalRouteDist+"");


                                        if (nbpersonne!= null)
                                        {
                                            final String[] conso3 = {""};

                                            final String aspassenger = nbpersonne.getText().toString();
                                            if (!aspassenger.equals("")){
                                                conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                                Expression e = new Expression(conso3[0]);
                                                String result = String.valueOf(e.calculate());
                                                ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                            }else {
                                                conso3[0] = conso2.replace("Y",""+1+"");
                                                Expression e = new Expression(conso3[0]);
                                                String result = String.valueOf(e.calculate());
                                                ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                            }
                                            Expression e = new Expression(conso3[0]);
                                            String result = String.valueOf(e.calculate());
                                            ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");

                                            nbpersonne.addTextChangedListener(new TextWatcher() {

                                                public void afterTextChanged(Editable s) {}

                                                public void beforeTextChanged(CharSequence s, int start,
                                                                              int count, int after) {

                                                }

                                                public void onTextChanged(CharSequence s, int start,
                                                                          int before, int count) {
                                                    final String aspassenger = nbpersonne.getText().toString();
                                                    if (!aspassenger.equals("")){
                                                        conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                                        Expression e = new Expression(conso3[0]);
                                                        String result = String.valueOf(e.calculate());
                                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                                    }else {
                                                        conso3[0] = conso2.replace("Y",""+1+"");
                                                        Expression e = new Expression(conso3[0]);
                                                        String result = String.valueOf(e.calculate());
                                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                                    }
                                                }
                                            });

                                        }else {

                                            Expression e = new Expression(conso2);
                                            String result = String.valueOf(e.calculate());
                                            ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                        }

                                    }
                                } else {
                                }
                            }
                        });





            }
        });
        ImageView kmedit = findViewById(R.id.editkilometre);
        kmedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
                bottomSheetDialog.show(getSupportFragmentManager(),"bottomSheetDialog");
            }
        });

        Button btnaddpreftraj = findViewById(R.id.addPrefTrajet);
        btnaddpreftraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialog2 bottomSheetDialog2 = new BottomSheetDialog2();
                bottomSheetDialog2.show(getSupportFragmentManager(),"bottomSheetDialog2");
            }
        });


        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                TextView distance = ((TextView) findViewById(R.id.tvDistance));
                String comparedist = distance.getText().toString();
                if (searchLocation != null || click != 0 || !comparedist.equals("0.0 Km")){

                    if (user != null){

                        String uid = user.getUid();

                        String conso = ((TextView) findViewById(R.id.tvCO2)).getText().toString();
                        String finalConso = conso.split(" ")[0];

                        String km = ((TextView) findViewById(R.id.tvDistance)).getText().toString();
                        String finalkm = km.split(" ")[0];

//                        Date   now = new Date();
//                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm");
//                        String DateData = formatter.format(now);

                        final Intent intent = getIntent();
                        final String vehicule_titre = intent.getStringExtra("Vehicule titre");
                        String vehicule_image = intent.getStringExtra("Vehicule image");

                        Map<String, Object> Trajet = new HashMap<>();
                        Trajet.put("consoTrajet", finalConso);
                        Trajet.put("dateTrajet", new Timestamp(new Date()));
                        Trajet.put("auteurTrajet", uid);
                        Trajet.put("vehiculeTrajet", vehicule_titre);
                        Trajet.put("distanceTrajet", finalkm);
                        Trajet.put("imageTrajet", vehicule_image);


                        db.collection("trajets").document().set(Trajet).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(MapsActivity.this, getString(R.string.trajet_save),Toast.LENGTH_SHORT).show();
                                onBackPressed();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MapsActivity.this, getString(R.string.trajet_not_save), Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });


                        //CO2 ECONOMISER
                        db.collection("users")
                                .whereEqualTo(FieldPath.documentId() , uid)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {

                                            for (QueryDocumentSnapshot document : task.getResult()) {


                                                String ecoconsoUser=new String(document.getString("eco_co2"));
                                                String consoUser=new String(document.getString("conso_co2"));
                                                String currentConsoUser = conso.split(" ")[0];

                                                String finalConsoUser = currentConsoUser+"+"+consoUser;
                                                Expression e = new Expression(finalConsoUser);
                                                String result = String.valueOf(e.calculate());


                                                Map<String, Object> Conso = new HashMap<>();
                                                Conso.put("conso_co2", result);

                                                db.collection("users").document(uid)
                                                        .set(Conso, SetOptions.merge());

                                                db.collection("vehicules")
                                                        .whereEqualTo("titreVehicule" , "Voiture")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                                        String dist= ((TextView) findViewById(R.id.tvDistance)).getText().toString();
                                                                        String finalDist = dist.split(" ")[0];

                                                                        String consoVoiture = document.getString("ConsoCalculeVehicule");
                                                                        String finalConsoVoiture = consoVoiture.replace("X",""+finalDist+"");

                                                                        Expression g = new Expression(finalConsoVoiture);
                                                                        String result4 = String.valueOf(g.calculate());

                                                                        String co2Eco = result4+"-"+currentConsoUser;
                                                                        String finalCo2Eco = ecoconsoUser+"+"+co2Eco;

                                                                        Expression h = new Expression(finalCo2Eco);
                                                                        String result5 = String.valueOf(h.calculate());



                                                                        Map<String, Object> Conso = new HashMap<>();
                                                                        Conso.put("eco_co2", result5);

                                                                        db.collection("users").document(uid)
                                                                                .set(Conso, SetOptions.merge());


                                                                    }
                                                                } else {
                                                                }
                                                            }
                                                        });



                                                //EURO ECONOMISER
//                                                db.collection("parametres")
//                                                        .get()
//                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                                if (task.isSuccessful()) {
//
//                                                                    for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                                                        String prixCO2=new String(document.getString("prixCO2"));
//
//                                                                        db.collection("vehicules")
//                                                                                .whereEqualTo("titreVehicule" , "Voiture")
//                                                                                .get()
//                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                                                    @Override
//                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                                                        if (task.isSuccessful()) {
//                                                                                            for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                                                                                String dist= ((TextView) findViewById(R.id.tvDistance)).getText().toString();
//                                                                                                String finalDist = dist.split(" ")[0];
//
//                                                                                                String consoVoiture = document.getString("ConsoCalculeVehicule");
//                                                                                                String finalConsoVoiture = consoVoiture.replace("X",""+finalDist+"");
//
//                                                                                                String euroEcovoiture = finalConsoVoiture+"*"+prixCO2;
//
//                                                                                                Expression g = new Expression(euroEcovoiture);
//                                                                                                String eurovoiture = String.valueOf(g.calculate());
//
//                                                                                                String currentConsoUser = conso.split(" ")[0];
//                                                                                                String cout = prixCO2+"*"+currentConsoUser;
//
//                                                                                                Expression e = new Expression(cout);
//                                                                                                String eurouser = String.valueOf(e.calculate());
//
//
//                                                                                                String finalEuroEco = eurovoiture+"-"+eurouser;
//                                                                                                String finalEuroEco2 = euroUser +"+"+ finalEuroEco;
//
//                                                                                                Expression i = new Expression(finalEuroEco2);
//                                                                                                String result10 = String.valueOf(i.calculate());
//
//                                                                                                Map<String, Object> Conso = new HashMap<>();
//                                                                                                Conso.put("eco_euro", result10);
//
//                                                                                                db.collection("users").document(uid)
//                                                                                                        .set(Conso, SetOptions.merge());
//
//
//                                                                                            }
//                                                                                        } else {
//                                                                                            Log.d("TEST", "Error getting documents: ", task.getException());
//                                                                                        }
//                                                                                    }
//                                                                                });
//
//
//
//
//
//
//
//
//                                                                        db.collection("users").document(uid)
//                                                                                .set(Conso, SetOptions.merge());
//
//
//                                                                    }
//                                                                } else {
//                                                                    Log.d("TEST", "Error getting documents: ", task.getException());
//                                                                }
//                                                            }
//                                                        });



                                            }
                                        } else {
                                            Log.d("TEST", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });


                        //KILOMETRE PARCOURUE
                        db.collection("users")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {

                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                String kilousser=document.getString("kilometre");
                                                Log.d("kilo",kilousser);

                                                String currentkilo = ((TextView) findViewById(R.id.tvDistance)).getText().toString().split(" ")[0];

                                                String addkilo = kilousser+"+"+currentkilo;
                                                Log.d("kilo",addkilo);

                                                Expression e = new Expression(addkilo);
                                                String nkilouser = String.valueOf(e.calculate());
                                                Log.d("kilo",nkilouser);




                                                Map<String, Object> Kilo = new HashMap<>();
                                                Kilo.put("kilometre", nkilouser);

                                                db.collection("users").document(uid)
                                                        .set(Kilo, SetOptions.merge());


                                            }
                                        } else {
                                            Log.d("TEST", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });








                    }else{
                        Toast.makeText(MapsActivity.this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();

                    }
                }else{
                    Toast.makeText(MapsActivity.this, getString(R.string.no_trajet), Toast.LENGTH_SHORT).show();
                }








            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location locationUpdate : locationResult.getLocations()) {
                    locationgps = locationUpdate;
                    if (gpsFirstOn) {
                        gpsFirstOn = false;
                        getDeviceLocation(true);
                    }
                }
            }
        };

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void setupAutoCompleteFragment() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocompleate_fragment);

        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocompleate_fragment2);

        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {


            @Override
            public void onPlaceSelected(com.google.android.libraries.places.compat.Place place) {
                searchLocation2 = place.getLatLng();
                try {
                    final Intent intent = getIntent();
                    final String vehicule_methode = intent.getStringExtra("Vehicule methode");

                    if (searchLocation2 != null){
                        String origin = searchLocation2.latitude + "," + searchLocation2.longitude;
                        new DirectionFinder(MapsActivity.this, origin, searchLocation.latitude + "," + searchLocation.longitude,vehicule_methode).execute(getString(R.string.google_maps_key));

                    }else {
                        Toast.makeText(MapsActivity.this, getString(R.string.no_destination), Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("mtn", "search");
            }

            @Override
            public void onError(Status status) {
                Log.e("Error", status.getStatusMessage());
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {


            @Override
            public void onPlaceSelected(com.google.android.libraries.places.compat.Place place) {
                searchLocation = place.getLatLng();
                try {
                    final Intent intent = getIntent();
                    final String vehicule_methode = intent.getStringExtra("Vehicule methode");
                    String vehicule ="";

                    if(vehicule_methode.equals("d")){
                        vehicule = "driving";
                   }else if(vehicule_methode.equals("w")){
                        vehicule = "walking";
                   } else if(vehicule_methode.equals("b")){
                        vehicule = "bicycling";
                   }

                    String origin = locationgps.getLatitude() + "," + locationgps.getLongitude();
                    new DirectionFinder(MapsActivity.this, origin, searchLocation.latitude + "," + searchLocation.longitude,vehicule).execute(getString(R.string.google_maps_key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("mtn", "search");
            }

            @Override
            public void onError(Status status) {
                Log.e("Error", status.getStatusMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        map = gMap;

        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);

        if (!checkPermission()){
            requestPermission();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationA, DEFAULT_ZOOM));
        }else {
            final Task<Location> locationResult = fusedLocationProvider.getLastLocation();
            locationResult.addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    lastKnownLocation = task.getResult();
                    LatLng me = new LatLng(lastKnownLocation.getLatitude()+0.003,lastKnownLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(me, DEFAULT_ZOOM));
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.getException());
                    showSnackbar(R.string.no_location_detected, Snackbar.LENGTH_LONG, 0, null);
                }
            });

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location locationchanged) {
                    LatLng nlatlng =  new LatLng(locationchanged.getLatitude()+0.003,locationchanged.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(nlatlng, DEFAULT_ZOOM));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates("gps",5000,100,locationListener);

        }

        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setCompassEnabled(true);

        // TODO : location
        map.getProjection().getVisibleRegion();
        getDeviceLocation(false);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, getString(R.string.wait), getString(R.string.near_location_search), true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarker != null) {
            for (Marker marker : destinationMarker) {
                marker.remove();
            }
        }
        if (polyLinePaths != null) {
            for (Polyline polylinePath : polyLinePaths) {
                polylinePath.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();





        for (Route route : routes) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 15.5f));
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
            ((TextView) findViewById(R.id.tvTime)).setText(route.duration.text);




            final Intent intent = getIntent();
            final String vehicule_titre = intent.getStringExtra("Vehicule titre");

            db.collection("vehicules")
                    .whereEqualTo("titreVehicule",vehicule_titre)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String conso = new String (document.getString("ConsoCalculeVehicule"));
                                    String routedist = route.distance.text;
                                    String finalRouteDist = routedist.split(" ")[0];
                                    Log.d("dist",finalRouteDist);
                                    String conso4 = conso.replace("X",""+finalRouteDist+"");
                                    String conso2 = conso4.replace(",","");

                                    Log.d("conso",conso2);
                                    Log.d("conso",conso4);


                                    if (nbpersonne!= null)
                                    {
                                        final String[] conso3 = {""};

                                        final String aspassenger = nbpersonne.getText().toString();
                                        if (!aspassenger.equals("")){
                                            conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                            Expression e = new Expression(conso3[0]);
                                            String result = String.valueOf(e.calculate());
                                            ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                        }else {
                                            conso3[0] = conso2.replace("Y",""+1+"");
                                            Log.d("conso3", conso3[0]);
                                            Expression e = new Expression(conso3[0]);
                                            String result = String.valueOf(e.calculate());
                                            ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                        }
                                        Expression e = new Expression(conso3[0]);
                                        String result = String.valueOf(e.calculate());
                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");

                                        nbpersonne.addTextChangedListener(new TextWatcher() {

                                            public void afterTextChanged(Editable s) {}

                                            public void beforeTextChanged(CharSequence s, int start,
                                                                          int count, int after) {

                                            }

                                            public void onTextChanged(CharSequence s, int start,
                                                                      int before, int count) {
                                                final String aspassenger = nbpersonne.getText().toString();
                                                if (!aspassenger.equals("")){
                                                    conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                                    Expression e = new Expression(conso3[0]);
                                                    String result = String.valueOf(e.calculate());
                                                    ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                                }else {
                                                    conso3[0] = conso2.replace("Y",""+1+"");
                                                    Log.d("conso3", conso3[0]);
                                                    Expression e = new Expression(conso3[0]);
                                                    String result = String.valueOf(e.calculate());
                                                    ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                                }
                                            }
                                        });

                                    }else {

                                        Expression e = new Expression(conso2);
                                        String result = String.valueOf(e.calculate());
                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                    }





                                    Log.d("dist",conso2);




                                }
                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });

            destinationMarker.add(map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .color(getResources().getColor(R.color.colorPrimary))
                    .width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }

            polyLinePaths.add(map.addPolyline(polylineOptions));
        }

    }

    private void getDeviceLocation(final boolean MyLocation) {
        if (!MyLocation)

            if (checkPermission()) {
                if (map != null) {
                    map.setMyLocationEnabled(true);
                }

                final Task<Location> locationResult = fusedLocationProvider.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        lastKnownLocation = task.getResult();
                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.getException());
                        showSnackbar(R.string.no_location_detected, Snackbar.LENGTH_LONG, 0, null);
                    }
                });
            } else { // !checkPermission()
                Log.d(TAG, getString(R.string.current_location_null));
            }
    }

    @Override
    public void onMapClick(final LatLng point) {
        selectedMarker = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(selectedMarker)) {
            selectedMarker = null;
            return true;
        }

        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        selectedMarker = marker;
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Connections.checkConnection(this)) {
            new PermissionGPS(this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Connections.checkConnection(this)) {
            new PermissionGPS(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Connections.checkConnection(this)) {
            if (checkPermission()) {
                fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }

        if (broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d("petard","stp");
                    int intDisttrack = (int) intent.getExtras().get("coordinates");
                    Log.d("petardtrack", String.valueOf(intDisttrack));
                    TextView distance = ((TextView) findViewById(R.id.tvDistance));
                    distance.setText(String.valueOf(intDisttrack));


                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else { // grantResults.length > 0
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation(false);
                } else {
                    showSnackbar(R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE, android.R.string.ok, view -> requestPermission());
                }
            }
        }
    }

    private void showSnackbar(int textStringId, int length, int actionStringId, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), textStringId, length);
        if (listener != null) {
            snackbar.setAction(actionStringId, listener);
        }
        snackbar.show();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this,"right-to-left");

    }

    @Override
    public void onTextChanged(String text) {
        if (text.equals("")){
            ((TextView) findViewById(R.id.tvDistance)).setText(1+" Km");
        }else {
            ((TextView) findViewById(R.id.tvDistance)).setText(text+" Km");

        }

        TextView distance = ((TextView) findViewById(R.id.tvDistance));
        final Intent intentv = getIntent();
        final String vehicule_titre = intentv.getStringExtra("Vehicule titre");

        db.collection("vehicules")
                .whereEqualTo("titreVehicule",vehicule_titre)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d("aprefini","pouete pouete poti rigolo");

                                String conso = document.getString("ConsoCalculeVehicule");
                                String routedist = distance.getText().toString();
                                String finalRouteDist = routedist.split(" ")[0];
                                Log.d("dist",finalRouteDist);
                                String conso2 = conso.replace("X",""+finalRouteDist+"");


                                if (nbpersonne!= null)
                                {
                                    final String[] conso3 = {""};

                                    final String aspassenger = nbpersonne.getText().toString();
                                    if (!aspassenger.equals("")){
                                        conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                        Expression e = new Expression(conso3[0]);
                                        String result = String.valueOf(e.calculate());
                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                    }else {
                                        conso3[0] = conso2.replace("Y",""+1+"");
                                        Log.d("conso3", conso3[0]);
                                        Expression e = new Expression(conso3[0]);
                                        String result = String.valueOf(e.calculate());
                                        ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                    }
                                    Expression e = new Expression(conso3[0]);
                                    String result = String.valueOf(e.calculate());
                                    ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");

                                    nbpersonne.addTextChangedListener(new TextWatcher() {

                                        public void afterTextChanged(Editable s) {}

                                        public void beforeTextChanged(CharSequence s, int start,
                                                                      int count, int after) {

                                        }

                                        public void onTextChanged(CharSequence s, int start,
                                                                  int before, int count) {
                                            final String aspassenger = nbpersonne.getText().toString();
                                            if (!aspassenger.equals("")){
                                                conso3[0] = conso2.replace("Y",""+ aspassenger +"");
                                                Expression e = new Expression(conso3[0]);
                                                String result = String.valueOf(e.calculate());
                                                ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                            }else {
                                                conso3[0] = conso2.replace("Y",""+1+"");
                                                Log.d("conso3", conso3[0]);
                                                Expression e = new Expression(conso3[0]);
                                                String result = String.valueOf(e.calculate());
                                                ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                            }
                                        }
                                    });

                                }else {

                                    Expression e = new Expression(conso2);
                                    String result = String.valueOf(e.calculate());
                                    ((TextView) findViewById(R.id.tvCO2)).setText(result+" g/CO2");
                                }

                                Log.d("dist",conso2);

                            }
                        } else {
                            Log.d("TEST", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void addPrefTraj(String title, String distance) {
        mTitle.add(title);
        mKilometre.add(distance);
        savedata();
        listAdapter.notifyDataSetChanged();
    }





    private void loadData(){
        Log.d("track","Load");

        SharedPreferences sharedPreferences = getSharedPreferences("Trajet favorie",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("titre pref traj",null);
        String json2 = sharedPreferences.getString("kilometre pref traj",null);
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        mTitle = gson.fromJson(json, type);
        mKilometre = gson.fromJson(json2, type);

        if(mTitle == null){
            mTitle = new ArrayList<>();
        }
        if(mKilometre == null){
            mKilometre = new ArrayList<>();
        }
        

    }

    private void savedata(){

        SharedPreferences sharedPreferences = getSharedPreferences("Trajet favorie",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mTitle);
        String json2 = gson.toJson(mKilometre);
        editor.putString("titre pref traj",json);
        editor.putString("kilometre pref traj",json2);
        editor.apply();
    }




    @Override
    public void onPause(){
        super.onPause();
        Log.d("track","Pause");

    }


    class ListAdapter extends ArrayAdapter<String>{

        Context context;
        ArrayList<String> rTitle = new ArrayList<>();
        ArrayList<String> rKilometre = new ArrayList<>();



        ListAdapter(Context c,  ArrayList<String> title,  ArrayList<String> kilo){
            super(c,R.layout.layout_row,R.id.preftitle,title);
            this.context =c;
            this.rTitle = title;
            this.rKilometre = kilo;
        }



        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.layout_row, parent,false);
            TextView myTitle = row.findViewById(R.id.preftitle);
            TextView myKilo = row.findViewById(R.id.prefkilo);

            myTitle.setText(rTitle.get(position));
            myKilo.setText(rKilometre.get(position));

            return row;
        }

    }
}