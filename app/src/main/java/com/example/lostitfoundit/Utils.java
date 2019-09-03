package com.example.lostitfoundit;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

public class Utils {

    /* Some Global Variables that are used for processing data */
    private static final int PASS_LENGTH = 6;
    public static final int SPLASH_TIME_OUT = 1000;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    public static final int REQUEST_IMAGE_CAPTURE = 1001;
    public static final int REQUEST_PLACE_PICKER = 1002;

    static boolean checkRecoverdEmail(EditText email) {
        boolean valid = true;
        if (isEmpty(email)) {
            email.setError("Email is required.");
            valid = false;
        } else {
            if (!isEmail(email)) {
                email.setError("The entered email is not valid.");
                valid = false;
            }
        }
        return valid;
    }

    static boolean checkEnteredDetailsAreCorrect(EditText email, EditText password) {
        boolean valid = true;
        if (isEmpty(email)) {
            email.setError("Email is required.");
            valid = false;
        } else {
            if (!isEmail(email)) {
                email.setError("The entered email is not valid.");
                valid = false;
            }
        }
        if (isEmpty(password)) {
            password.setError("Password is required.");
            valid = false;
        } else {
            if (!hasLength(password, PASS_LENGTH)) {
                password.setError("The minimum password length is 6.");
                valid = false;
            }
        }
        return valid;
    }

    private static boolean isEmpty(EditText text) {
        return TextUtils.isEmpty(text.getText().toString());
    }

    private static boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private static boolean hasLength(EditText text, int length) {
        return text.getText().toString().length() >= length;
    }
}
