package com.mad.cs5520_mindfull;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RviewHolder extends RecyclerView.ViewHolder {

    public TextView username;
    public TextView token;
    public ImageView itemIcon;

    /**
     * constructor
     * @param itemView
     * @param listener
     */
    public RviewHolder(View itemView, final FriendClickListener listener) {
        super(itemView);

        // Get token and username
        username = itemView.findViewById(R.id.username);
        token = itemView.findViewById(R.id.token);
        itemIcon = itemView.findViewById(R.id.profile_pic);

        // Set onclick for each user pill
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onUserClick(position);
                    }
                }
            }
        });
    }
}
