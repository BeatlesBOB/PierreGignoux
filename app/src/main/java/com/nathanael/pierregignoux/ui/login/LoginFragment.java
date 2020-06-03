package com.nathanael.pierregignoux.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.nathanael.pierregignoux.ForgotPass;
import com.nathanael.pierregignoux.Main2Activity;
import com.nathanael.pierregignoux.R;
import com.nathanael.pierregignoux.Register;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import maes.tech.intentanim.CustomIntent;

public class LoginFragment extends Fragment {

    private Button btnregister;
    private FloatingActionButton btncnx;
    private FirebaseAuth mAuth;
    EditText user, password;
    private static  final String TAG = " ";
    private DatabaseReference mDatabase;
    TextView passforgot;


    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Context context = this.getContext();

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        user = (EditText) root.findViewById(R.id.textEmail);
        user.setCompoundDrawablePadding(30);
        password = (EditText) root.findViewById(R.id.textPassword);
        password.setCompoundDrawablePadding(30);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btnregister = root.findViewById(R.id.btnregister);

        btncnx = root.findViewById(R.id.btnConnexion);

        passforgot= root.findViewById(R.id.tv_forgot_password);

        passforgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), ForgotPass.class);
                startActivity(intent);


            }
        });


        btncnx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String utilisateur = user.getText().toString();
                String passe = password.getText().toString();

                if ((utilisateur.length() != 0 ) && (passe.length() != 0))
                {
                    mAuth.signInWithEmailAndPassword(utilisateur, passe)
                            .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                        Toast.makeText(context, getString(R.string.auth_sucess),Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(context, Main2Activity.class);
                                        startActivity(intent);

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(context, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                        updateUI(null);
                                    }

                                    // ...
                                }
                            });

                }

            }
        });

        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, Register.class);
                startActivity(intent);
                CustomIntent.customType(getContext(),"bottom-to-up");


            }
        });

        return root;

    }

    private void updateUI(FirebaseUser user) {
    }

    private void mdp(View view){

    }



}

