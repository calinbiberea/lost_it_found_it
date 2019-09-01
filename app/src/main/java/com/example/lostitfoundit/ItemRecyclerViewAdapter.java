package com.example.lostitfoundit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public final class ItemRecyclerViewAdapter extends
        RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "ItemRecyclerViewAdapter";

    private ArrayList<String> mImageNames;
    private ArrayList<String> mImages;

    private Context mContext;

    public ItemRecyclerViewAdapter(Context mContext, ArrayList<String> imageNames,
                                   ArrayList<String> images ) {
        mImageNames = imageNames;
        mImages = images;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onVindViewHolder: called");

        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.itemImage);

        holder.itemName.setText(mImageNames.get(position));
    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView itemImage;
        private TextView itemName;
        private RelativeLayout itemContainer;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemContainer = itemView.findViewById(R.id.item_container);
            itemImage = itemView.findViewById(R.id.lost_item_icon);
            itemName = itemView.findViewById(R.id.lost_item_name);
        }
    }
}
