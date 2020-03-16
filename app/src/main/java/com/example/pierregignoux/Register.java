package com.example.pierregignoux;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

public class Register extends AppCompatActivity {

    private static final String TAG ="";
    EditText user, password, confirmPass, firstName, lastName;
    private FirebaseAuth mAuth;
    private  FirebaseFirestore db;
    private String prenom, nom, mail, pass, pass2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        firstName = (EditText) findViewById(R.id.txtFirstName);
        lastName = (EditText) findViewById(R.id.txtLastName);
        user = (EditText) findViewById(R.id.txtUser);
        password = (EditText) findViewById(R.id.txtPassword);
        confirmPass = (EditText) findViewById(R.id.txtPassword2);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Title and subtitle
//        toolbar.setTitle(R.string.about_toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_account_circle_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });




    }



    public void Registre (View view) {


        nom = firstName.getText().toString();
        prenom = lastName.getText().toString();
        mail = user.getText().toString();
        pass  = password.getText().toString();
        pass2 = confirmPass.getText().toString();

        Log.d("auth",prenom);
        Log.d("auth",nom);
        Log.d("auth",mail);
        Log.d("auth",pass);
        Log.d("auth",pass2);


        if ((mail.length() != 0) && (pass.length() != 0) && (pass2.length() != 0) && (firstName.length() != 0) && (lastName.length() != 0))
        {

            if (pass.equals(pass2)) {


                mAuth.createUserWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                    if (user != null) {

                                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                                        // authenticate with your backend server, if you have one. Use
                                        // FirebaseUser.getIdToken() instead.
                                        String uid = user.getUid();
                                        Log.d("uid",uid);

                                        Map<String, Object> User = new HashMap<>();
                                        User.put("obj_co2","0");
                                        User.put("eco_co2","0");
                                        User.put("conso_co2","0");
                                        User.put("kilometre","0");



                                        db.collection("users").document(uid)
                                                .set(User)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("Oskour", "DocumentSnapshot successfully written!");

                                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                .setDisplayName(prenom)
                                                                .build();

                                                        Log.d("oskour", prenom);
                                                        user.updateProfile(profileUpdates)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d("Oskour", "User profile updated.");

                                                                            Intent intent = new Intent(Register.this, Main2Activity.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                });



                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("Oskour", "Error writing document", e);
                                                    }
                                                });



                                    }



                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.d("super", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Register.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
            } else {
                Toast.makeText(Register.this,  getString(R.string.invalid_password_confirm),Toast.LENGTH_SHORT).show();
                password.setText("");
                confirmPass.setText("");
            }
        }
    }

    private void updateUI(FirebaseUser user) {
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this,"up-to-bottom");

    }
}
