package com.mad.cs5520_mindfull;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RviewAdapter extends RecyclerView.Adapter<RviewHolder> {
    private final ArrayList<FriendCard> friendList;
    private FriendClickListener listener;
    private Context context;


    /**
     * constructor
     * @param userList
     */
    public RviewAdapter(ArrayList<FriendCard> userList, Context context) {
        this.friendList = userList;
        this.context = context;
    }

    public void setOnItemClickListener(FriendClickListener listener) {
        this.listener = listener;
    }

    /**
     * onCreateViewHOlder()
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate each user card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card, parent, false);
        return new RviewHolder(view, listener);
    }

    /**
     * onBindViewHolder()
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RviewHolder holder, int position) {
        // Bind each username and token to card
        FriendCard currentItem = friendList.get(position);
        holder.username.setText(currentItem.getUsername());
        holder.token.setText(currentItem.getToken());
        Picasso.with(context)
                .load(currentItem.getImageSource())
                .into(holder.itemIcon);
    }

    /**
     * getItemCount()
     * @return
     */
    @Override
    public int getItemCount() {
        return friendList.size();
    }
    public void changePic(String url, RviewHolder holder, int position) {
        FriendCard currentItem = friendList.get(position);
        Picasso.with(context)
                .load(currentItem.getImageSource())
                .into(holder.itemIcon);
    }
}
