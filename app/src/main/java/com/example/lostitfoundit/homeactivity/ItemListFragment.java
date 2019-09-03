package com.example.lostitfoundit.homeactivity;

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
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lostitfoundit.BuildConfig;
import com.example.lostitfoundit.R;
import com.example.lostitfoundit.models.Item;
import com.example.lostitfoundit.viewholder.ItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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


    /* Useful Views */
    private RecyclerView recyclerView;
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

        /* Create a recycler view */
        recyclerView = view.findViewById(R.id.item_list_view);
        initialiseRecyclerView();

        return view;
    }

    private void initialiseRecyclerView() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("items")
                .limitToLast(50);

        FirebaseRecyclerOptions<Item> recyclerOptions = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        FirebaseRecyclerAdapter<Item, ItemViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i, @NonNull Item item) {

                itemViewHolder.lostItemName.setText(item.getItemName());
                itemViewHolder.lostItemTime.setText(item.getItemTime());

            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container, parent, false);

                return new ItemViewHolder(view);
            }
        };

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerAdapter.startListening();
        recyclerView.setAdapter(recyclerAdapter);

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
                final Item item = new Item();
                item.setItemName(addItemName.getText().toString());
                item.setItemTime(addItemTime.getText().toString());
                item.setItemDescription(addItemDescription.getText().toString());
                item.setItemAddress("");

                boolean canUpload = true;
                if (TextUtils.isEmpty(item.getItemName())) {
                    canUpload = false;
                    addItemName.setError("An item name is required.");
                }
                if (TextUtils.isEmpty(item.getItemTime())) {
                    canUpload = false;
                    addItemTime.setError("The time it was lost is required.");
                }
                if (TextUtils.isEmpty(item.getItemDescription())) {
                    canUpload = false;
                    addItemDescription.setError("An item description is required.");
                }
                /*
                 if (TextUtils.isEmpty(item.itemAddress)) {
                    canUpload = false;
                    addItemAddress.setError("The address it was lost around is required.");
                }
                */
                if (canUpload) {
                    mDatabase.child("items").child(mUser.getUid() + "" + item.getItemName().hashCode())
                            .setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                        String taskResultText;

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                taskResultText = "Uploaded the item details.";
                            } else {
                                taskResultText = "Failed to upload the item details.";
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
        } catch (Exception ex) {
            // Google Play services is not available...
        }
    }

    private void initImageBitmaps() {

    }

    private void refreshData() {
        Toast.makeText(getActivity(), "THIS IS A REFRESH", Toast.LENGTH_SHORT).show();
    }

}
