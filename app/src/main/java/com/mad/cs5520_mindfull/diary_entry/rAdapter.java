package com.mad.cs5520_mindfull.diary_entry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.cs5520_mindfull.R;

import java.util.ArrayList;

public class rAdapter extends RecyclerView.Adapter<DiaryCollectorHolder> {
    public ArrayList<DiaryItem> rDiaryList;
    public ItemClickListener listListener;
    private onItemClickListener listener;


    public rAdapter(ArrayList<DiaryItem> itemList) {
        this.rDiaryList = itemList;
    }

    public void setOnClickListener(ItemClickListener listener) {
        this.listListener = listener;
    }

    /**
     * onCreateViewHolder() initializes DiaryCollectorHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public DiaryCollectorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_diary_item, parent, false);
        return new DiaryCollectorHolder(view, listListener);
    }

    /**
     * onBindViewHolder() add DiaryItem to holder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull DiaryCollectorHolder holder, int position) {
        DiaryItem item = rDiaryList.get(position);
        holder.item_title.setText(item.getdTextTitle());
        holder.item_content.setText(item.getdTextParagraph());
        holder.item_date.setText(item.getdDate());
    }

    @Override
    public int getItemCount() {
        return rDiaryList.size();
    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }
}
