package com.example.zippy.helper;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.AboutActivity;
import com.example.zippy.CourseDetailsActivity;
import com.example.zippy.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CourseCustomAdapter extends RecyclerView.Adapter<CourseCustomAdapter.ViewHolder> {
    private List<CourseHelperClass> courses;
    private Activity context;
    public CourseCustomAdapter(List<CourseHelperClass> courses, Activity context) {
        this.courses = courses;
        this.context = context;
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
        private void bind(CourseHelperClass course, Activity context){

            txtViewCourseCode.setText(course.getCourseCode());
            txtViewCourseTitle.setText(course.getCourseTitle());
            txtViewCourseYear.setText(course.getCourseYear());
            txtViewCourseCredit.setText(course.getCourseCredit());

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // get position
                    int pos = getAdapterPosition();

                    // check if item still exists
                    if(pos != RecyclerView.NO_POSITION){
                        String clickedCoursePassCode = course.getCoursePassCode();
                        context.startActivity(new Intent(context, CourseDetailsActivity.class));
                        Toast.makeText(v.getContext(), "You clicked " + clickedCoursePassCode, Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
        //viewHolder.setData(courses.get(position));
        viewHolder.bind(courses.get(position), context);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
       return courses.size();
    }
}
