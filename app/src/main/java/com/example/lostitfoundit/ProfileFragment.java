package com.example.lostitfoundit;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.example.lostitfoundit.Utils.REQUEST_IMAGE_CAPTURE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseUser mUser;

    private ImageView mProfilePicture;
    private TextView mEmail;
    private Button mUpload;
    private EditText mUserName;
    private EditText mPhone;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        /* Setting up all Firebase related elements */
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Here we initialise all the rest of the views */
        mProfilePicture = view.findViewById(R.id.profile_image);
        mUpload = view.findViewById(R.id.upload);
        mUserName = view.findViewById(R.id.username);
        mPhone = view.findViewById(R.id.phone_number);
        mEmail = view.findViewById(R.id.email_data);

        initialiseViews();

        retrieveUserInfo();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            Log.d(TAG, "onActivityResult: started image retrieval process");
            final Uri filePath = data.getData();
            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), filePath);
                uploadImageAndUri(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* This method retrieves and updates the user info if there is data uploaded in the database */
    private void retrieveUserInfo() {
        mDatabase.child("users").child(mUser.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: started retrieving user info");
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("username")) {
                                final String username = dataSnapshot.child("username")
                                        .getValue().toString();
                                mUserName.setText(username);
                            }
                            if (dataSnapshot.hasChild("phone")) {
                                final String phone = dataSnapshot.child("phone")
                                        .getValue().toString();
                                mPhone.setText(phone);
                            }

                            if (dataSnapshot.hasChild("image")) {
                                final String image = dataSnapshot.child("image")
                                        .getValue().toString();
                                Glide.with(ProfileFragment.this)
                                        .load(image).into(mProfilePicture);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError.toException();
                    }
                });
    }

    /* This method initializes all the view that are required */
    private void initialiseViews() {
        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profilePictureIntent();
            }
        });

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUserName.getText().toString();
                final String phone = mPhone.getText().toString();
                final HashMap<String, Object> profileMap = new HashMap<>();
                if (!TextUtils.isEmpty(username)) {
                    profileMap.put("username", username);
                }
                if (!TextUtils.isEmpty(phone)) {
                    profileMap.put("phone", phone);
                }
                if (!profileMap.isEmpty()) {
                    mDatabase.child("users").child(mUser.getUid()).updateChildren(profileMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                String taskResultText;

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        taskResultText = "Uploaded new details.";
                                    } else {
                                        taskResultText = "Failed to upload new details.";
                                    }
                                    final Toast taskResult = Toast.makeText(getContext(),
                                            taskResultText
                                            , Toast.LENGTH_SHORT);
                                    taskResult.setGravity(Gravity.CENTER, 0, 0);
                                    taskResult.show();
                                }
                            });
                }
            }
        });
        mEmail.setText(mUser.getEmail());
    }

    /* This processes the image capture for saving the new profile picture */
    private void profilePictureIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
    }

    /* This method uploads the image to the storage */
    private void uploadImageAndUri(final Bitmap bitmap) {
        ByteArrayOutputStream compressionResult = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressionResult);
        byte[] image = compressionResult.toByteArray();

        final StorageReference saveLocation = mStorage.child("pics").child(mUser.getUid());
        saveLocation.putBytes(image).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                /* Continue with the task to get the download URL */
                return saveLocation.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    /* Set the image and find the download URL */
                    mProfilePicture.setImageBitmap(bitmap);
                    Uri downloadUri = task.getResult();
                    mDatabase.child("users").child(mUser.getUid()).child("image")
                            .setValue(downloadUri.toString()).addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast failure = Toast.makeText(getContext(),
                                                "Couldn't link the saved image to user."
                                                , Toast.LENGTH_SHORT);
                                        failure.setGravity(Gravity.CENTER, 0, 0);
                                        failure.show();
                                    }
                                }
                            });
                } else {
                    Toast failure = Toast.makeText(getContext(),
                            "Uploading the image failed. Try again later."
                            , Toast.LENGTH_SHORT);
                    failure.setGravity(Gravity.CENTER, 0, 0);
                    failure.show();
                }
            }
        });
    }

}
