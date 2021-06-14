package com.example.zippy.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import java.util.ArrayList;


public class CourseCustomAdapter extends RecyclerView.Adapter<CourseCustomAdapter.ViewHolder> {
    private final ArrayList<CourseHelperClass> courseList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        private void bind(CourseHelperClass course){
            txtViewCourseCode.setText(course.getCourseCode());
            txtViewCourseTitle.setText(course.getCourseTitle());
            txtViewCourseYear.setText(course.getCourseYear());
            txtViewCourseCredit.setText(course.getCourseCredit());
        }
    }

    public CourseCustomAdapter(ArrayList<CourseHelperClass> exampleList) {
        courseList = exampleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_list, parent, false);
        ViewHolder evh = new ViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(courseList.get(position));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }
}