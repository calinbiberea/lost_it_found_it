package com.example.lostitfoundit;

import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rtchagas.pingplacepicker.PingPlacePicker;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.example.lostitfoundit.Utils.REQUEST_IMAGE_CAPTURE;
import static com.example.lostitfoundit.Utils.REQUEST_PLACE_PICKER;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemListFragment extends Fragment {

    private static final String TAG = "ItemListFragment";

    /* Firebase Related Variables */
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseUser mUser;

    /* Adding an item views */
    private CircleImageView addItemImage;
    private Place place;
    private TextView addItemAddress;
    private byte[] uploadResult = null;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();

    private SwipeRefreshLayout mPullToRefresh;
    private FloatingActionButton mAddItemButton;

    public ItemListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        /* Setting up all Firebase related elements */
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Initialising the views */
        mPullToRefresh = view.findViewById(R.id.pullToRefresh);
        mAddItemButton = view.findViewById(R.id.add_item);
        initialiseViews();


        RecyclerView recyclerView;
        initImageBitmaps();

        recyclerView = view.findViewById(R.id.item_list_view);
        ItemRecyclerViewAdapter adapter = new ItemRecyclerViewAdapter(getContext(),
                mNames, mImageUrls);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                makeLocalImageChange(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ((requestCode == REQUEST_PLACE_PICKER) && (resultCode == RESULT_OK)) {
            place = PingPlacePicker.getPlace(data);
            if (place != null) {
                addItemAddress.setText(place.getName());
            }
        }
    }

    /* This method shows the changed image locally and sets up the byte array for upload */
    private void makeLocalImageChange(Bitmap bitmap) {
        ByteArrayOutputStream compressionResult = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressionResult);
        uploadResult = compressionResult.toByteArray();
        addItemImage.setImageBitmap(bitmap);
    }

    /* This is a method that initialises the views */
    private void initialiseViews() {
        mPullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                mPullToRefresh.setRefreshing(false);
            }
        });

        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.item_adder_dialog);
                initialiseDialogAddItem(dialog);
                dialog.show();
            }
        });

    }

    /* This sets up the views in the dialog so one can add an item to the search list */
    private void initialiseDialogAddItem(Dialog dialog) {
        addItemImage = dialog.findViewById(R.id.lost_item_image);
        addItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profilePictureIntent();
            }
        });

        final EditText addItemName = dialog.findViewById(R.id.add_item_name);
        final EditText addItemTime = dialog.findViewById(R.id.add_item_time);
        addItemAddress = dialog.findViewById(R.id.add_item_address);
        final EditText addItemDescription = dialog.findViewById(R.id.add_item_description);

        addItemAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlacePicker();
            }
        });

        final Button addItemButton = dialog.findViewById(R.id.add_item);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String itemName = addItemName.getText().toString();
                final String itemTime = addItemTime.getText().toString();
                final String itemDescription = addItemDescription.getText().toString();
                final String itemAddress = addItemAddress.getText().toString();

                boolean canUpload = true;
                if (TextUtils.isEmpty(itemName)) {
                    canUpload = false;
                    addItemName.setError("An item name is required.");
                }
                if (TextUtils.isEmpty(itemTime)) {
                    canUpload = false;
                    addItemTime.setError("The time it was lost is required.");
                }
                if (TextUtils.isEmpty(itemDescription)) {
                    canUpload = false;
                    addItemDescription.setError("An item description is required.");
                }
                if (TextUtils.isEmpty(itemAddress)) {
                    canUpload = false;
                    addItemAddress.setError("The address it was lost around is required.");
                }
                if (canUpload) {
                }

            }
        });
    }

    /* This method tries to upload the image in the storage and the uri in the database */
    private boolean uploadImageAndUri() {
        final StorageReference saveLocation = mStorage.child("pics").child(mUser.getUid());
        saveLocation.putBytes(uploadResult).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
        return true;
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


    private void showPlacePicker() {
        final String maps_key = BuildConfig.MapKey;
        final String android_key = BuildConfig.AndroidKey;
        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
        builder.setAndroidApiKey(android_key)
                .setMapsApiKey(maps_key);

        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        // builder.setLatLng(new LatLng(37.4219999, -122.0862462))

        try {
            Intent placeIntent = builder.build(getActivity());
            startActivityForResult(placeIntent, REQUEST_PLACE_PICKER);
        }
        catch (Exception ex) {
            // Google Play services is not available...
        }
    }

    private void initImageBitmaps() {
        mImageUrls.add("https://c1.staticflickr.com/5/4636/25316407448_de5fbf183d_o.jpg");
        mNames.add("Havasu Falls");

        mImageUrls.add("https://i.redd.it/tpsnoz5bzo501.jpg");
        mNames.add("Trondheim");
    }

    private void refreshData() {
        Toast.makeText(getActivity(), "THIS IS A REFRESH", Toast.LENGTH_SHORT).show();
    }

}
