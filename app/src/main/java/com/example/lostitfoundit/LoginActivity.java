package com.example.lostitfoundit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Initialise the firebase application to check if a user is already logged in. */
        mAuth = FirebaseAuth.getInstance();

        /* Initialization of the required views */
        initialiseViews();
    }

    /* This method initializes all the view that are required */
    private void initialiseViews() {
        final Button sign_in = findViewById(R.id.sign_in);
        final Button forgot_pass = findViewById(R.id.forgot_pass);
        final EditText mEmail = findViewById(R.id.email);
        final EditText mPass = findViewById(R.id.password);

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.checkEnteredDetailsAreCorrect(mEmail, mPass)) {
                    loginUser(mEmail.getText().toString(), mPass.getText().toString());
                }
            }
        });

        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(LoginActivity.this,
                        ForgotPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }

    /* This method logs in the user using the Firebase Info */
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        final String taskResultText;
                        if (task.isSuccessful()) {
                            Log.d(TAG, "mAuth: successfully logged the user in");
                            final Intent intent = new Intent(LoginActivity.this,
                                    HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            taskResultText = "Login successful.";
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d(TAG, "mAuth: failed to log the user in");
                            taskResultText = "Login failed. Check email and password.";
                        }
                        final Toast taskResult = Toast.makeText(LoginActivity.this,
                                taskResultText, Toast.LENGTH_SHORT);
                        taskResult.setGravity(Gravity.CENTER, 0, 0);
                        taskResult.show();
                    }
                });
    }

    /* This method switches the user to the register activity */
    public void switchToRegister(View view) {
        final Intent intent = new Intent(LoginActivity.this, RegisterActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
