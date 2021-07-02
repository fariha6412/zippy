package com.example.zippy.adapter;

import android.graphics.Color;
import android.text.BoringLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;


public class StudentCustomAdapter extends RecyclerView.Adapter<StudentCustomAdapter.ViewHolder> {
    private final ArrayList<String> studentNameList;
    private final ArrayList<String> studentRegistrationNoList;
    private final ArrayList<Integer> selectedPositions;
    private OnItemClickListener mListener;
    private Boolean multipleSelection = false;

    public interface OnItemClickListener {
        void multipleSelection(int position);
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout linearLayout;
        private final TextView txtViewStudentName;
        private final TextView txtViewStudentRegistrationNo;
        public ImageView deleteBtn;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            txtViewStudentName = itemView.findViewById(R.id.txtViewStudentName);
            txtViewStudentRegistrationNo = itemView.findViewById(R.id.txtviewstudentregistrationno);
            deleteBtn = itemView.findViewById(R.id.deletebtn);

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

    public StudentCustomAdapter(ArrayList<String> studentNameList, ArrayList<String> studentRegistrationNoList, ArrayList<Integer> selectedPositions) {
        this.studentNameList = studentNameList;
        this.studentRegistrationNoList = studentRegistrationNoList;
        this.selectedPositions = selectedPositions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    public void setMultipleSelection(Boolean multipleSelection) {
        this.multipleSelection = multipleSelection;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String currentName = studentNameList.get(position);
        String currentRegistrationNo = studentRegistrationNoList.get(position);

        holder.txtViewStudentName.setText(currentName);
        holder.txtViewStudentRegistrationNo.setText(currentRegistrationNo);

        holder.linearLayout.setBackgroundColor(selectedPositions.contains(Integer.valueOf(position)) ? Color.parseColor("#ebc1be"): Color.parseColor("#00000000"));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!multipleSelection) mListener.onItemClick(position);
                else {
                    if (selectedPositions.contains(Integer.valueOf(position)))
                        selectedPositions.remove(Integer.valueOf(position));
                    else selectedPositions.add(position);
                    holder.linearLayout.setBackgroundColor(selectedPositions.contains(Integer.valueOf(position)) ? Color.parseColor("#ebc1be") : Color.parseColor("#00000000"));
                }
            }
        };
        holder.itemView.setOnClickListener(onClickListener);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!multipleSelection)multipleSelection = true;
                selectedPositions.add(position);
                holder.linearLayout.setBackgroundColor(selectedPositions.contains(Integer.valueOf(position)) ? Color.parseColor("#ebc1be") : Color.WHITE);
                mListener.multipleSelection(position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentNameList.size();
    }
}