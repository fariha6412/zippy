package com.example.zippy.helper;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChatCustomAdapter extends RecyclerView.Adapter<ChatCustomAdapter.ViewHolder> {
    private final ArrayList<Messages> messageList;
    private final Activity activity;
    public final int left = 0;
    public final int right = 1;
    FirebaseAuth mAuth;
    FirebaseUser user;

    private SearchCustomAdapter.OnItemClickListener mListener;

    public ChatCustomAdapter(ArrayList<Messages> messageList, Activity activity) {
        this.messageList = messageList;
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView receivedText, sentText, textHolder ;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            //receivedText = itemView.findViewById(R.id.txtviewrecievedtext);
            //sentText = itemView.findViewById(R.id.txtviewsenttext);
            textHolder = itemView.findViewById(R.id.txtviewtext);

        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == left) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiverchat, parent, false);

        }
        else{
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.senderchat, parent, false);

        }
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatCustomAdapter.ViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = messageList.get(position);
        holder.textHolder.setText(messages.getMessageText());

    }

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(messageList.get(position).getMessageSender().equals(user.getUid())){
            return right;
        }
        return left;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
