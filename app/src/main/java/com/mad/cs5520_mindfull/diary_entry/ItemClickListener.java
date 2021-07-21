package com.mad.cs5520_mindfull.diary_entry;

public interface ItemClickListener {
    void onItemClick(int position);

//    String onClickNavigateButton(int position);

    String setText(int position, String text);
}
