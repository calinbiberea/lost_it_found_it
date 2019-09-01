package com.example.lostitfoundit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import static com.example.lostitfoundit.Utils.SPLASH_TIME_OUT;

public class LoadingActivity extends AppCompatActivity {

    private static final String TAG = "LoadingActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        /* Initialise the Firebase application to check if a user is already logged in. */
        mAuth = FirebaseAuth.getInstance();

        delayAndCheck();
    }

    /* This is a method that created a delay for making a splash screen. It also passes the login
     * screen if the user is already logged in, moving to the home activity directly.
     */
    private void delayAndCheck() {
        final boolean userLoggedIn = (mAuth.getCurrentUser() != null);
        final Intent intent;
        if (userLoggedIn) {
            intent = new Intent(LoadingActivity.this, HomeActivity.class);
        } else {
            intent = new Intent(LoadingActivity.this, LoginActivity.class);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                if (userLoggedIn) {
                    Log.d(TAG, "mAuth: user logged in, sending to home");
                    final Toast success = Toast.makeText(LoadingActivity.this,
                            "Welcome back!", Toast.LENGTH_SHORT);
                    success.setGravity(Gravity.CENTER, 0, 0);
                    success.show();
                } else {
                    Log.d(TAG, "mAuth: sending user to login screen");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
