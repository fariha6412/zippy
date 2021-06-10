package com.example.zippy.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CourseCustomAdapter extends RecyclerView.Adapter<CourseCustomAdapter.ViewHolder> {
    private List<CourseHelperClass> courses;
    public CourseCustomAdapter(List<CourseHelperClass> courses) {
        this.courses = courses;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtViewCourseCode, txtViewCourseTitle, txtViewCourseYear, txtViewCourseCredit;

        public ViewHolder(View view) {
            super(view);

            txtViewCourseCode = view.findViewById(R.id.txtviewcoursecode);
            txtViewCourseTitle = view.findViewById(R.id.txtviewcoursetitle);
            txtViewCourseYear = view.findViewById(R.id.txtviewcourseyear);
            txtViewCourseCredit = view.findViewById(R.id.txtviewcoursecredit);
        }
        public void setData(CourseHelperClass course){
            txtViewCourseCode.setText(course.getCourseCode());
            txtViewCourseTitle.setText(course.getCourseTitle());
            txtViewCourseYear.setText(course.getCourseYear());
            txtViewCourseCredit.setText(course.getCourseCredit());
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public @NotNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.course_list, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        //viewHolder.getTextView().setText(localDataSet[position]);
        viewHolder.setData(courses.get(position));
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
       return courses.size();
    }
}
