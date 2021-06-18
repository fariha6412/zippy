package com.example.zippy.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;


public class StudentCustomAdapter extends RecyclerView.Adapter<StudentCustomAdapter.ViewHolder> {
    private ArrayList<String> studentNamelist;
    private final ArrayList<String> studentRegistrationNolist;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtViewStudentName;
        private final TextView txtViewStudentRegistrationNo;
        public ImageView deletebtn;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtViewStudentName = itemView.findViewById(R.id.txtviewstudentname);
            txtViewStudentRegistrationNo = itemView.findViewById(R.id.txtviewstudentregistrationno);
            deletebtn = itemView.findViewById(R.id.deletebtn);

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

            deletebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    public StudentCustomAdapter(ArrayList<String> studentNamelist, ArrayList<String> studentRegistrationNolist) {
        this.studentNamelist = studentNamelist;
        this.studentRegistrationNolist = studentRegistrationNolist;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String currentName = studentNamelist.get(position);
        String currentRegistrationNo = studentRegistrationNolist.get(position);

        holder.txtViewStudentName.setText(currentName);
        holder.txtViewStudentRegistrationNo.setText(currentRegistrationNo);
    }

    @Override
    public int getItemCount() {
        return studentNamelist.size();
    }
}