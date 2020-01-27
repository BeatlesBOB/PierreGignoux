package com.example.pierregignoux.ui.trajet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pierregignoux.R;
import com.example.pierregignoux.TrajetAdapter;
import com.example.pierregignoux.models.direction.Trajet;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
                                    Trajet nTrajet = new Trajet(document.getId(), document.getString("auteurTrajet"), document.getTimestamp("dateTrajet"), document.getString("consoTrajet"), document.getString("vehiculeTrajet"), document.getString("distanceTrajet"),document.getString("imageTrajet"));

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
    public void onTrajetClick(int position) {

        Log.d(TAG,"onTicketClick: clicked.");

    }
}