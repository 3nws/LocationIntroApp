package com.example.location_intro_app;

import java.util.ArrayList;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter
{
    private String[] titles;
    private ArrayList<Drawable> gridImages;
    private Activity activity;

    public ImageAdapter(Activity activity,String[] titles, ArrayList<Drawable> gridImages) {
        super();
        this.titles = titles;
        this.gridImages = gridImages;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return titles.length;
    }

    @Override
    public String getItem(int position) {
        // TODO Auto-generated method stub
        return titles[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static class ViewHolder
    {
        public ImageView imgView;
        public TextView txtViewTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        LayoutInflater inflator = activity.getLayoutInflater();

        if(convertView==null)
        {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.gridview_row, null);

            view.txtViewTitle = (TextView) convertView.findViewById(R.id.textView1);
            view.imgView = (ImageView) convertView.findViewById(R.id.imageView1);

            convertView.setTag(view);
        }
        else
        {
            view = (ViewHolder) convertView.getTag();
        }

        view.txtViewTitle.setText(titles[position]);
        view.txtViewTitle.setShadowLayer(24,4,4, Color.BLACK);
        view.imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.imgView.setPadding(2, 0, 2, 0);
        view.imgView.setImageDrawable(gridImages.get(position));

        return convertView;
    }
}