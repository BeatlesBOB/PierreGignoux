package com.nathanael.pierregignoux;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;

import com.nathanael.pierregignoux.models.direction.Historique;
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

public class HistoriqueActivity extends AppCompatActivity implements HistoriqueAdapter.OnHistoriqueListener {

    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerview;
    private RecyclerView.LayoutManager layoutManager;
    private HistoriqueAdapter adapter;
    final List<Historique> items = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        db = FirebaseFirestore.getInstance();
        recyclerview = findViewById(R.id.recyclerviewhistorique);
        recyclerview.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);

        adapter =  new HistoriqueAdapter(this ,items, this);
        recyclerview.setAdapter(adapter);
        items.clear();


        if (user!= null) {
            String uid = user.getUid();
            db.collection("historique")
                    .whereEqualTo("user",uid)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Historique nHistorique=new Historique(document.getId(),document.getString("user"),document.getTimestamp("date"),document.getString("consommation"));
                                    items.add(nHistorique);
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
    public void onHistoriqueClick(int position) {
    }
}
