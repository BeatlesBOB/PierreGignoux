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

public class ForgotPass extends AppCompatActivity {

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        Button passreset = findViewById(R.id.sendpassreset);


        passreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText mail = findViewById(R.id.resetemail);
                String finalresetemail = mail.getText().toString();
                if (finalresetemail.length() != 0)
                {

                    auth.sendPasswordResetEmail(finalresetemail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("mail", "Email sent.");

                                        Toast.makeText(ForgotPass.this, getString(R.string.email_sent),Toast.LENGTH_SHORT).show();


                                    }
                                }
                            });

                }

            }
        });
    }
}
