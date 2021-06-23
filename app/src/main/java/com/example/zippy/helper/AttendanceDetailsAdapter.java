package com.example.zippy.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;
import java.util.HashMap;


public class AttendanceDetailsAdapter extends RecyclerView.Adapter<AttendanceDetailsAdapter.ViewHolder> {
    private final ArrayList<String> dates;
    private final ArrayList<String> presentStatus;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtViewDate;
        private final TextView txtViewPresentStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            txtViewDate = itemView.findViewById(R.id.txtviewdatetoday);
            txtViewPresentStatus = itemView.findViewById(R.id.txtviewpresentstatus);
        }
    }

    public AttendanceDetailsAdapter(ArrayList<String> dates, ArrayList<String> presentStatus) {
        this.dates = dates;
        this.presentStatus = presentStatus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_list, parent, false);
        ViewHolder evh = new ViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String date = dates.get(position);
        String present = presentStatus.get(position);

        holder.txtViewDate.setText(date);
        holder.txtViewPresentStatus.setText(present);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }
}