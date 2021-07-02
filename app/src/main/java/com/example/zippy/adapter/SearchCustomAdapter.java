package com.example.zippy.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zippy.R;

import java.util.ArrayList;


public class SearchCustomAdapter extends RecyclerView.Adapter<SearchCustomAdapter.ViewHolder> {
    private final ArrayList<String> userList;
    private final ArrayList<String> imgList;
    private final Activity activity;
    private OnItemClickListener mListener;

    public SearchCustomAdapter(ArrayList<String> userList, ArrayList<String> imgList, Activity activity) {
        this.userList = userList;
        this.imgList = imgList;
        this.activity = activity;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtViewFullName;
        private final ImageView imgView;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            txtViewFullName = itemView.findViewById(R.id.txtViewFullName);
            imgView = itemView.findViewById(R.id.imgView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }

        private void bind(String fullName, String img, Activity activity){
            txtViewFullName.setText(fullName);
            Glide.with(activity.getBaseContext()).load(img).into(imgView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(userList.get(position), imgList.get(position), activity);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}