package com.example.lostitfoundit.viewholder;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostitfoundit.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public TextView lostItemName;
    public TextView lostItemTime;
    public ImageView lostItemIcon;

    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);

        lostItemName = itemView.findViewById(R.id.lost_item_name);
        lostItemTime = itemView.findViewById(R.id.lost_item_time);
        lostItemIcon = itemView.findViewById(R.id.lost_item_icon);
    }

}
