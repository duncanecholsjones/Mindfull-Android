package com.mad.cs5520_mindfull;

import android.graphics.Bitmap;
public class FriendCard implements FriendClickListener {

    private final String username;
    private final String token;
    private String imageSource;

    public FriendCard(String username, String imageSource) {
        this.username = username;
        this.token = "Click here to see friend's mood chart";
        this.imageSource = imageSource;
    }

    public String getUsername() {
        return username;
    }

    public void setImageUrl(String imageUrl) {
        this.imageSource = imageUrl;
    }

    public String getToken() {return token;}

    public String getImageSource() {
        return imageSource;
    }

    @Override
    public void onUserClick(int position) {
        System.out.println("Clicked at position: " + position);
    }
}
