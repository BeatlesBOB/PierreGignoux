package com.example.pierregignoux.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pierregignoux.ImageAdapter;
import com.example.pierregignoux.MapsActivity;
import com.example.pierregignoux.R;
import com.example.pierregignoux.Vehicule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class HomeFragment extends Fragment implements ImageAdapter.OnVehiculeListener{

    private HomeViewModel homeViewModel;
    private RecyclerView recyclerview;
    private RecyclerView.LayoutManager layoutManager;
    private ImageAdapter adapter;
    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final List<Vehicule> items = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerview = root.findViewById(R.id.recyclerview);
        recyclerview.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getContext(),3);
        recyclerview.setLayoutManager(layoutManager);

        adapter =  new ImageAdapter(this.getContext() ,items, this);
        recyclerview.setAdapter(adapter);

        loadData();

        String label = getString(R.string.label_fragment_home);
        getActivity().setTitle(label);





        return root;
    }

    private void loadData()
    {
        items.clear();

        db.collection("vehicules")
                .orderBy("ConsoCalculeVehicule", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TEST", document.getId() + " => " + document.getData());
                                Vehicule nVehicule=new Vehicule(document.getId(),document.getString("titreVehicule"),document.getString("imgVehicule"),document.getString("ConsoCalculeVehicule"),document.getString("methodeVehicule"));
                                items.add(nVehicule);

                                recyclerview.removeAllViews();

                            }
                        } else {
                            Log.d("TEST", "Error getting documents: ", task.getException());
                        }
                    }
                });



    }

    @Override
    public void onVehiculeClick(int position) {

        Intent intent = new Intent(getContext(), MapsActivity.class);
        intent.putExtra("Vehicule titre", items.get(position).getTitre());
        intent.putExtra("Vehicule methode", items.get(position).getMethode());
        intent.putExtra("Vehicule image", items.get(position).getImage());
        intent.putExtra("Vehicule calcule", items.get(position).getConsocalcule());

        startActivity(intent);
        CustomIntent.customType(getContext(),"left-to-right");


    }
}