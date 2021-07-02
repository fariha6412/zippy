package com.example.zippy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;


public class StudentCustomAdapter extends RecyclerView.Adapter<StudentCustomAdapter.ViewHolder> {
    private final ArrayList<String> studentNameList;
    private final ArrayList<String> studentRegistrationNoList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void multipleSelection(int position);
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtViewStudentName;
        private final TextView txtViewStudentRegistrationNo;
        public ImageView deleteBtn;
        private Boolean longPress;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtViewStudentName = itemView.findViewById(R.id.txtViewStudentName);
            txtViewStudentRegistrationNo = itemView.findViewById(R.id.txtviewstudentregistrationno);
            deleteBtn = itemView.findViewById(R.id.deletebtn);
            longPress = false;

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    System.out.println("longPressed");
                    if (listener != null) {
                        if(!longPress) longPress = true;
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.multipleSelection(position);
                        }
                    }
                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if(!longPress)listener.onItemClick(position);
                            else listener.multipleSelection(position);
                        }
                    }
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
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

    public StudentCustomAdapter(ArrayList<String> studentNameList, ArrayList<String> studentRegistrationNoList) {
        this.studentNameList = studentNameList;
        this.studentRegistrationNoList = studentRegistrationNoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String currentName = studentNameList.get(position);
        String currentRegistrationNo = studentRegistrationNoList.get(position);

        holder.txtViewStudentName.setText(currentName);
        holder.txtViewStudentRegistrationNo.setText(currentRegistrationNo);
    }

    @Override
    public int getItemCount() {
        return studentNameList.size();
    }
}