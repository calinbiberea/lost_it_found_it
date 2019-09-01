package com.example.lostitfoundit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        /* Initialise the firebase application */
        mAuth = FirebaseAuth.getInstance();

        /* Initialise all the other views */
        initialiseViews();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /* A method that initialises the views needed for password recovery */
    private void initialiseViews() {
        final EditText email = findViewById(R.id.email);
        final Button recover = findViewById(R.id.recover);

        recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.checkRecoverdEmail(email)) {
                    final String emailData = email.getText().toString();
                    mAuth.sendPasswordResetEmail(emailData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    final String taskResultText;
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "mAuth: recovery email sent");
                                        taskResultText = "Recovery email sent.";
                                        final Intent intent = new Intent(
                                                ForgotPasswordActivity.this,
                                                LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.d(TAG, "mAuth: failed to send recovery email");
                                        taskResultText = "Failed to send recovery email.";
                                    }
                                    final Toast taskResult = Toast.makeText(
                                            ForgotPasswordActivity.this,
                                            taskResultText, Toast.LENGTH_SHORT);
                                    taskResult.setGravity(Gravity.CENTER, 0, 0);
                                    taskResult.show();
                                }
                            });
                }
            }
        });
    }
}
