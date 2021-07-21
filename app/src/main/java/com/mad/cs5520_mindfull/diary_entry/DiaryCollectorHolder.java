package com.mad.cs5520_mindfull.diary_entry;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.mad.cs5520_mindfull.R;

public class DiaryCollectorHolder extends RecyclerView.ViewHolder {

    public TextView item_title;
    public TextView item_content;
    public TextView item_date;

    public DiaryCollectorHolder(View listView, final ItemClickListener listener) {
        super(listView);
        item_title = listView.findViewById(R.id.item_title);
        item_content = listView.findViewById(R.id.item_content);
        item_date = listView.findViewById(R.id.item_date);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                    Toast.makeText(v.getContext(),position+"AHHHH", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
