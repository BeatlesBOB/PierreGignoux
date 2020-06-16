package com.nathanael.pierregignoux;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.Date;
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
                                    Historique nHistorique=new Historique(document.getId(),document.getString("user"),document.getTimestamp("date"),document.getString("consommation"),document.getString("economie_co2"), document.getString("kilometre"));
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
    public void onHistoriqueClick(Historique historique,int position) {
        Date histodate = historique.getDateDate();

        Calendar cStart = Calendar.getInstance();
        cStart.setTime(histodate);// this takes current date
        cStart.set(Calendar.DAY_OF_MONTH, 1);
        Date dateStart = cStart.getTime();
        long dateStartLong = dateStart.getTime();

        Calendar cEnd = Calendar.getInstance();   // this takes current date
        cEnd.setTime(histodate);
        cEnd.set(Calendar.DATE, cEnd.getActualMaximum(Calendar.DATE));
        Date dateEnd = cEnd.getTime();
        long dateEndLong = dateEnd.getTime();

        Intent intent = new Intent(HistoriqueActivity.this, InfoActivity.class);
        intent.putExtra("From","Month");
        intent.putExtra("DateStart",dateStartLong);
        intent.putExtra("DateEnd",dateEndLong);
        startActivity(intent);
    }

    @Override
    public void onHistoriqueLongClick(Historique historique, int position) {
        if (user != null) {

            new AlertDialog.Builder(HistoriqueActivity.this)
                    .setIcon(R.drawable.ic_delete_black_24dp)
                    .setTitle(getString(R.string.sure))
                    .setMessage(getString(R.string.deleteitem))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            db.collection("historique").document(historique.getId())
                                    .delete();

                            items.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, items.size());

                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();

        }
    }


}
