package com.example.pierregignoux.ui.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pierregignoux.Main2Activity;
import com.example.pierregignoux.MainActivity;
import com.example.pierregignoux.MapsActivity;
import com.example.pierregignoux.Questions;
import com.example.pierregignoux.Quizz;
import com.example.pierregignoux.QuizzAdapter;
import com.example.pierregignoux.R;
import com.example.pierregignoux.Register;
import com.example.pierregignoux.TrajetAdapter;
import com.example.pierregignoux.models.direction.Trajet;
import com.example.pierregignoux.ui.home.HomeFragment;
import com.example.pierregignoux.ui.profil.ProfilViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class TestFragment extends Fragment implements QuizzAdapter.OnQuizzListener {


    RadioButton radio1,radio2,radio3,radio4;
    TextView question;
    RadioGroup rgroup;
    ProgressBar gprog;
    Button btnAnswer;



    private String mAnswer;

    private int mScore = 0;

    int r;

    private RecyclerView recyclerview;
    QuizzAdapter adapter;
    final List<Quizz> items = new ArrayList<>();
    Questions mQuestions;


    private FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    private TestViewModel testViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        testViewModel = ViewModelProviders.of(this).get(TestViewModel.class);
        View root = inflater.inflate(R.layout.fragment_test, container, false);

        final Context context = this.getContext();
        db = FirebaseFirestore.getInstance();

        String label = getString(R.string.label_fragment_test);
        getActivity().setTitle(label);

        gprog=root.findViewById(R.id.globalprogress);

        mQuestions = new Questions(getContext());

        recyclerview = root.findViewById(R.id.quizzrecycler);
        recyclerview.setLayoutManager(new LinearLayoutManager((getActivity())));
        adapter =  new QuizzAdapter(this.getContext() ,items, this);
        recyclerview.setAdapter(adapter);

        rgroup = root.findViewById(R.id.radiogroup);
        radio1= root.findViewById(R.id.radio_rep1);
        radio2= root.findViewById(R.id.radio_rep2);
        radio3= root.findViewById(R.id.radio_rep3);
        radio4= root.findViewById(R.id.radio_rep4);

        question= root.findViewById(R.id.question);

        btnAnswer = root.findViewById(R.id.btnquizz);

        r= 0;
        updateQuestion(r);





        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (r != 0){

                    if (user != null) {

                        String uid = user.getUid();

                        Map<String, Object> Quizz = new HashMap<>();
                        Quizz.put("scoreQuizz", ""+mScore);
                        Quizz.put("auteurQuizz", uid);
                        Quizz.put("dateQuizz", new Timestamp(new Date()));


                        db.collection("quizz").document().set(Quizz).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(context, getString(R.string.quizz_done), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context,getString(R.string.quizz_not_done), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }

            }
        });

        radio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r++;
                String select = (String) radio1.getText();
                if (select.equals(mAnswer)){

                    Log.d("score", String.valueOf(mScore));

                    if (r+1 == 7){
                        mScore += 10;
                        gprog.setProgress(mScore);
                    }
                    if (r+1>=7)
                    {
                        Log.d("index","oui");
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {
                        mScore += 10;
                        gprog.setProgress(mScore);
                        updateQuestion(r);

                    }



                }else {

                    if (r+1>7 )
                    {
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {

                        if (r+1 == 6 ){
                            r++;

                        }else {
                            updateQuestion(r);
                        }
                    }


                }

                rgroup.clearCheck();
            }
        });

        radio2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r++;
                String select = (String) radio2.getText();
                if (select.equals(mAnswer)){

                    Log.d("score", String.valueOf(mScore));

                    if (r+1 == 7){
                        mScore += 10;
                        gprog.setProgress(mScore);
                    }
                    if (r+1>=7)
                    {
                        Log.d("index","oui");
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {
                        mScore += 10;
                        gprog.setProgress(mScore);
                        updateQuestion(r);

                    }

                }else {

                    if (r+1>7)
                    {
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {

                        if (r+1 == 6 ){
                            r++;
                        }else {
                            updateQuestion(r);
                        }
                    }


                }

                rgroup.clearCheck();
            }
        });

        radio3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r++;
                String select = (String) radio3.getText();
                if (select.equals(mAnswer)){

                    Log.d("score", String.valueOf(mScore));

                    if (r+1 == 7){
                        mScore += 10;
                        gprog.setProgress(mScore);
                    }
                    if (r+1>=7)
                    {
                        Log.d("index","oui");
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {
                        mScore += 10;
                        gprog.setProgress(mScore);
                        updateQuestion(r);

                    }

                }else {

                    if (r+1>7)
                    {
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {
                        if (r+1 == 6 ){
                            r++;
                        }else {
                            updateQuestion(r);
                        }
                    }



                }

                rgroup.clearCheck();
            }
        });

        radio4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r++;
                String select = (String) radio4.getText();
                if (select.equals(mAnswer)){

                    Log.d("score", String.valueOf(mScore));

                    if (r+1 == 7){
                        mScore += 10;
                        gprog.setProgress(mScore);
                    }
                    if (r+1>=7)
                    {
                        Log.d("index","oui");
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {
                        mScore += 10;
                        gprog.setProgress(mScore);
                        updateQuestion(r);

                    }

                }else {

                    if (r+1>7)
                    {
                        Toast.makeText(context, getString(R.string.quizz_complete), Toast.LENGTH_LONG).show();
                    } else {
                        if (r+1 == 6 ){
                            r++;

                        }else {
                            updateQuestion(r);
                        }
                    }




                }

                rgroup.clearCheck();
            }
        });

        return root;
    }
    private void updateQuestion(int num){

        question.setText(mQuestions.getQuestion(num));
        radio1.setText(mQuestions.getChoice1(num));
        radio2.setText(mQuestions.getChoice2(num));
        radio3.setText(mQuestions.getChoice3(num));
        radio4.setText(mQuestions.getChoice4(num));
        mAnswer = mQuestions.getCorrectAnswer(num);

    }

    public void function (){

        items.clear();


        if (user != null) {

            String uid = user.getUid();

            db.collection("quizz")
                    .whereEqualTo("auteurQuizz" , uid)
                    .orderBy("dateQuizz", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Quizz nQuizz = new Quizz(document.getId(),document.getString("scoreQuizz"),document.getString("auteurQuizz"),document.getTimestamp("dateQuizz") );

                                    items.add(nQuizz);

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
        function ();
    }



    @Override
    public void onQuizzClick(int position) {

        Log.d(TAG,"onTicketClick: clicked.");

    }




}