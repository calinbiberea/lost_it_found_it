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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /* Initialise the firebase application */
        mAuth = FirebaseAuth.getInstance();

        /* Initialise the wanted views */
        initialiseViews();
    }

    /* This method initializes all the view that are required */
    private void initialiseViews() {
        final Button register = findViewById(R.id.register);
        final EditText mEmail = findViewById(R.id.email);
        final EditText mPass = findViewById(R.id.password);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.checkEnteredDetailsAreCorrect(mEmail, mPass)) {
                    registerUser(mEmail.getText().toString(), mPass.getText().toString());
                }
            }
        });
    }

    /* This method registers the user in the Firebase Database; */
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        final String taskResultText;
                        if (task.isSuccessful()) {
                            Log.d(TAG, "mAuth: successfully registered the user");
                            taskResultText = "Successfully registered.";
                            final Intent intent = new Intent(RegisterActivity.this
                                    , HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d(TAG, "mAuth: failed to register the user");
                            taskResultText = "Registration failed. Try again later.";
                        }
                        final Toast taskResult = Toast.makeText(RegisterActivity.this,
                                taskResultText, Toast.LENGTH_SHORT);
                        taskResult.setGravity(Gravity.CENTER, 0, 0);
                        taskResult.show();
                    }
                });
    }

    /* This method switches the user to the login activity */
    public void switchToLogin(View view) {
        final Intent intent = new Intent(RegisterActivity.this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
