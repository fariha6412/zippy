package com.example.zippy.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;


public class AttendanceCustomAdapter extends RecyclerView.Adapter<AttendanceCustomAdapter.ViewHolder> {
    private ArrayList<String> studentlist;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onChecked(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtViewStudentName;
        public CheckBox chkPresentChecker;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtViewStudentName = itemView.findViewById(R.id.txtviewstudentname);
            chkPresentChecker = itemView.findViewById(R.id.presentcheckercheckbox);

            chkPresentChecker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onChecked(position);
                        }
                    }
                }
            });
        }
    }

    public AttendanceCustomAdapter(ArrayList<String> exampleList) {
        studentlist = exampleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String currentName = studentlist.get(position);

        holder.txtViewStudentName.setText(currentName);
    }

    @Override
    public int getItemCount() {
        return studentlist.size();
    }
}