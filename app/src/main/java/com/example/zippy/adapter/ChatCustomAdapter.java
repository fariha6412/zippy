package com.example.zippy.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;
import com.example.zippy.helper.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatCustomAdapter extends RecyclerView.Adapter<ChatCustomAdapter.ViewHolder> {
    private final ArrayList<Message> messageList;
    public final int left = 0;
    public final int right = 1;
    FirebaseUser user;
    FirebaseAuth mAuth;

    public ChatCustomAdapter(ArrayList<Message> messageList) {
        this.messageList = messageList;
        sortMessages();
    }

    private void sortMessages(){
        //sort by time
        Collections.sort(messageList, new Comparator<Message>() {
            public int compare(Message o1, Message o2) {
                if (o1.getMessageTime() == null || o2.getMessageTime() == null)
                    return 0;
                return o1.getMessageTime().compareTo(o2.getMessageTime());
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView receivedText, sentText, textHolder , receivedTime, sentTime;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            receivedText = itemView.findViewById(R.id.txtviewreceived);
            sentText = itemView.findViewById(R.id.txtviewsent);
            receivedTime = itemView.findViewById(R.id.txtviewrecievedtime);
            sentTime = itemView.findViewById(R.id.txtviewsenttime);

            //  textHolder = itemView.findViewById(R.id.txtviewtext);

        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v;
        //v = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiverchat, parent, false);
        mAuth = FirebaseAuth.getInstance();
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.senderchat, parent, false);

        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatCustomAdapter.ViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message message = messageList.get(position);

        String fromUserId = message.getMessageSender();

        holder.receivedText.setVisibility(View.GONE);
        holder.sentText.setVisibility(View.GONE);
        holder.receivedTime.setVisibility(View.GONE);
        holder.sentTime.setVisibility(View.GONE);

        if(fromUserId.equals(messageSenderId)){
            holder.sentText.setVisibility(View.VISIBLE);
            holder.sentText.setText(message.getMessageText());
            holder.sentTime.setVisibility(View.VISIBLE);
            holder.sentTime.setText(message.getMessageTime());
        }
        else{
            holder.receivedText.setVisibility(View.VISIBLE);
            holder.receivedText.setText(message.getMessageText());
            holder.receivedTime.setVisibility(View.VISIBLE);
            holder.receivedTime.setText(message.getMessageTime());
        }
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
