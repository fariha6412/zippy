package com.example.zippy.helper;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;


public class CourseCustomAdapter extends RecyclerView.Adapter<CourseCustomAdapter.ViewHolder> {
    private final ArrayList<CourseHelperClass> courseList;
    private final ArrayList<Boolean> courseCompletionStatus;
    private OnItemClickListener mListener;
  
    public CourseCustomAdapter(ArrayList<CourseHelperClass> courseList, ArrayList<Boolean> courseCompletionStatus) {
        this.courseList = courseList;
        this.courseCompletionStatus = courseCompletionStatus;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout linearLayout;
        private final TextView txtViewCourseCode;
        private final TextView txtViewCourseTitle;
        private final TextView txtViewCourseYear;
        private final TextView txtViewCourseCredit;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            txtViewCourseCode = itemView.findViewById(R.id.txtviewcoursecode);
            txtViewCourseTitle = itemView.findViewById(R.id.txtviewcoursetitle);
            txtViewCourseYear = itemView.findViewById(R.id.txtviewcourseyear);
            txtViewCourseCredit = itemView.findViewById(R.id.txtviewcoursecredit);
            linearLayout = itemView.findViewById(R.id.linearLayout);

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

        @SuppressLint("ResourceAsColor")
        private void bind(CourseHelperClass course, Boolean isCompleted){

            txtViewCourseCode.setText(course.getCourseCode());
            txtViewCourseTitle.setText(course.getCourseTitle());
            txtViewCourseYear.setText(course.getCourseYear());
            txtViewCourseCredit.setText(course.getCourseCredit());
            if(isCompleted){
                linearLayout.setBackgroundColor(Color.parseColor("#ebc1be"));
                linearLayout.setBackgroundTintMode(PorterDuff.Mode.MULTIPLY);
            }
            else {
                linearLayout.setBackgroundColor(Color.parseColor("#00000000"));
                linearLayout.setBackgroundResource(R.drawable.custom_border);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(courseList.get(position), courseCompletionStatus.get(position));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }
}