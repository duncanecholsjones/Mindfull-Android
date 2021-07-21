package com.mad.cs5520_mindfull.diary_entry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiaryItem implements ItemClickListener, Comparable<DiaryItem> {
    private String dTextTitle;
    private String dTextParagraph;
    private String dDate;

    public DiaryItem(String _dTextTitle, String _dTextParagraph, String _dDate) {

        this.dTextTitle =_dTextTitle;
        this.dTextParagraph = _dTextParagraph;
        this.dDate = _dDate;
    }

    @Override
    public void onItemClick(int position) { }

    @Override
    public String setText(int position, String url) {
        return null;
    }

    public String getdTextTitle() {
        return dTextTitle;
    }

    public String getdTextParagraph() {
        return dTextParagraph;
    }

    public String getdDate() {
        return dDate;
    }

    @Override
    public int compareTo(DiaryItem o) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
        try {
            return sdf.parse(this.getdDate()).compareTo(sdf.parse(o.dDate));
        } catch (ParseException e) {
            e.printStackTrace();
        };
        return -1;
    }
}
