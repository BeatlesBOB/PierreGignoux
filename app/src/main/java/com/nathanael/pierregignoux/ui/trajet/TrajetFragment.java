package com.nathanael.pierregignoux.ui.trajet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.SetOptions;
import com.nathanael.pierregignoux.MapsActivity;
import com.nathanael.pierregignoux.R;
import com.nathanael.pierregignoux.TrajetAdapter;
import com.nathanael.pierregignoux.models.direction.Trajet;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.mariuszgromada.math.mxparser.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class TrajetFragment extends Fragment implements TrajetAdapter.OnTrajetListener  {

    private RecyclerView recyclerview;
    TrajetAdapter adapter;
    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final List<Trajet> items = new ArrayList<>();




    private TrajetViewModel trajetViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        trajetViewModel = ViewModelProviders.of(this).get(TrajetViewModel.class);
        View root = inflater.inflate(R.layout.fragment_trajet, container, false);
        recyclerview= root.findViewById(R.id.recyclerviewtrajet);
        recyclerview.setLayoutManager(new LinearLayoutManager((getActivity())));
        adapter =  new TrajetAdapter(this.getContext() ,items, this);
        recyclerview.setAdapter(adapter);

        String label = getString(R.string.label_fragment_trajet);
        getActivity().setTitle(label);


        return root;



    }

    public void funtion (){

        items.clear();

        db = FirebaseFirestore.getInstance();



        if (user != null) {

            String uid = user.getUid();

            db.collection("trajets")
                    .whereEqualTo("auteurTrajet" , uid)
                    .orderBy("dateTrajet", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("TEST", document.getId() + " => " + document.getData());
                                    Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("distanceTrajet"),document.getString("imageTrajet"),document.getString("ecoTrajet"));

                                    items.add(nTrajet);

                                    recyclerview.removeAllViews();

                                }
                            } else {
                                Log.d("TEST", "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }



    }
    @Override
    public void onResume() {
        super.onResume();
        funtion ();
    }


    @Override
    public void onTrajetClick(Trajet trajet, int position) {
        if (user != null){
            String uid = user.getUid();

            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_delete_black_24dp)
                    .setTitle(getString(R.string.sure))
                    .setMessage(getString(R.string.deleteitem))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            db.collection("users")
                                    .whereEqualTo(FieldPath.documentId() , uid)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    String kilousser=document.getString("kilometre");

                                                    String suppkilo = kilousser+"-"+trajet.getKilometre();

                                                    Expression e = new Expression(suppkilo);
                                                    String nkilouser = String.valueOf(e.calculate());

                                                    String kilomois=document.getString("kilometre");

                                                    String suppkilomois = kilomois+"-"+trajet.getKilometre();

                                                    Expression f = new Expression(suppkilomois);
                                                    String nkilomois = String.valueOf(f.calculate());

                                                    Map<String, Object> Kilo = new HashMap<>();
                                                    Kilo.put("kilometre", nkilouser);
                                                    Kilo.put("kilometre_mois", nkilomois);

                                                    db.collection("users").document(uid)
                                                            .set(Kilo, SetOptions.merge());


                                                    String consoUser= document.getString("conso_co2");
                                                    int trajetconso = trajet.getConsommation();

                                                    String finalConsoUser = consoUser+"-"+trajetconso;
                                                    Expression g = new Expression(finalConsoUser);
                                                    String result = String.valueOf(g.calculate());

                                                    String consomoisUser=document.getString("conso_mois");
                                                    String finalConsoMoisUser = consomoisUser+"-"+trajetconso;
                                                    Expression h = new Expression(finalConsoMoisUser);
                                                    String resultmois = String.valueOf(h.calculate());

                                                    String ecoconsoUser = document.getString("eco_co2");
                                                    String fincaleco = ecoconsoUser+"-"+trajet.getEcoCO2();
                                                    Expression i = new Expression(fincaleco);
                                                    String resulteco = String.valueOf(i.calculate());

                                                    String ecoconsoUsermois = document.getString("eco_mois");
                                                    String fincalecomois = ecoconsoUsermois+"-"+trajet.getEcoCO2();
                                                    Expression j = new Expression(fincalecomois);
                                                    String resultecomois = String.valueOf(j.calculate());


                                                    Map<String, Object> Conso = new HashMap<>();
                                                    Conso.put("conso_co2", result);
                                                    Conso.put("conso_mois", resultmois);
                                                    Conso.put("eco_co2", resulteco);
                                                    Conso.put("eco_mois", resultecomois);



                                                    db.collection("users").document(uid)
                                                            .set(Conso, SetOptions.merge());

                                                    db.collection("trajets").document(trajet.getId())
                                                            .delete();

                                                    items.remove(position);
                                                    adapter.notifyItemRemoved(position);
                                                    adapter.notifyItemRangeChanged(position, items.size());
                                                    

                                                }
                                            } else {
                                                Log.d("TEST", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });





                        }
                    })
                    .setNegativeButton(getString(R.string.no),null)
                    .show();

        }
    }
}