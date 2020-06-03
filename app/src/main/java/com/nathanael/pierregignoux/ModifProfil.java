package com.nathanael.pierregignoux;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ModifProfil extends AppCompatActivity {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modif_profil);

        EditText nom = findViewById(R.id.nnom);

        nom.setText(user.getDisplayName());

        EditText adressmail = findViewById(R.id.nadress);

        adressmail.setText(user.getEmail());

        EditText mdp = findViewById(R.id.npass);


        Button btnupdate = findViewById(R.id.sendupdate);
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String finalnom = nom.getText().toString();
                String finaladressmail = adressmail.getText().toString();
                String finalmdp = mdp.getText().toString();

                Log.d("modif",finaladressmail);
                Log.d("modif",finalnom);


                if (finaladressmail.length() != 0){

                    Log.d("super","super1");
                    if (!finaladressmail.equals(user.getEmail())){
                        Log.d("super","super1");

                        user.updateEmail(finaladressmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("", "User email address updated.");

                                            Toast.makeText(ModifProfil.this, getString(R.string.update_email),Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                    }else {
                        Toast.makeText(ModifProfil.this, getString(R.string.not_update_email) ,Toast.LENGTH_SHORT).show();
                    }


                }

                    if (finalnom.length() != 0){

                        if (!finalnom.equals(user.getDisplayName())){

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(finalnom)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(ModifProfil.this, getString(R.string.update_name),Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }else {
                            Toast.makeText(ModifProfil.this, getString(R.string.not_update_name),Toast.LENGTH_SHORT).show();

                        }

                    }

                if (finalmdp.length() != 0){

                    Log.d("super","super1");

                        Log.d("super","super1");

                        user.updatePassword(finalmdp)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ModifProfil.this, getString(R.string.update_mdp),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });



                }
            }

        });
    }






}
