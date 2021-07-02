package com.example.zippy.ui.chat;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zippy.R;
import com.example.zippy.adapter.ChatCustomAdapter;
import com.example.zippy.classes.Instructor;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.helper.Message;
import com.example.zippy.classes.Student;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";
    final String strClickedUid = "clickedUid";
    String clickedUid, senderUid, receiverUid ;
    String loggedProfile;

    private ImageView imgView;
    private TextView fullName;
    private EditText inputText;
    private ImageView sendButton;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceInstructor, reference,referenceSent, referenceReceived;

    ChatCustomAdapter chatAdapter;
    ArrayList<Message> mChat;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MenuHelper menuHelperClass = new MenuHelper(toolbar, this);
        menuHelperClass.handle();

        auth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedUid = mPrefs.getString(strClickedUid, "");
        loggedProfile = mPrefs.getString(loggedStatus, "nouser");


        senderUid = user.getUid();
        receiverUid = clickedUid;

        imgView = findViewById(R.id.imgView);
        fullName = findViewById(R.id.txtViewFullName);
        inputText = findViewById(R.id.editTxtInputText);
        sendButton = findViewById(R.id.imgBtnSend);

        mChat = new ArrayList<>();
        initRecyclerView();

        setChatMenu();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputText.getText().toString();
                if(!text.isEmpty()){
                    sendMessage(text);
                    inputText.setText("");
                }
            }
        });
        showMessage();
    }

    private void setChatMenu() {
        referenceInstructor = rootNode.getReference("instructors/" + clickedUid);
        referenceStudent = rootNode.getReference("students/" + clickedUid);

        referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Student studentHelper = snapshot.getValue(Student.class);
                    assert studentHelper != null;
                    fullName.setText(studentHelper.getFullName());
                    Glide.with(getBaseContext()).load(studentHelper.getImage()).into(imgView);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        referenceInstructor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Instructor instructorHelper = snapshot.getValue(Instructor.class);
                    assert instructorHelper != null;
                    fullName.setText(instructorHelper.getFullName());
                    Glide.with(getBaseContext()).load(instructorHelper.getImage()).into(imgView);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }




    public void sendMessage(String messageText){
        reference = FirebaseDatabase.getInstance().getReference("messages/"+senderUid+"/"+receiverUid);
        Message message = new Message(messageText);
        reference.push().setValue(message);
        message.setMessageSender(senderUid);
        mChat.add(message);
        chatAdapter.notifyDataSetChanged();
    }


    public void showMessage(){
        referenceSent = FirebaseDatabase.getInstance().getReference("messages/"+senderUid+"/"+receiverUid);
        referenceSent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
               // clearDisplayedText(senderUid);
               // mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String msgText = (String)dataSnapshot.child("messageText").getValue();
                    String msgTime = (String) Objects.requireNonNull(dataSnapshot.child("messageTime").getValue());
                    mChat.add(new Message(senderUid, msgText, msgTime));

                }
                showReceivedMessage();


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    public void showReceivedMessage(){
        referenceReceived = FirebaseDatabase.getInstance().getReference("messages/"+receiverUid+"/"+senderUid);
        referenceReceived.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                  clearDisplayedText(receiverUid);
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String msgText = (String)dataSnapshot.child("messageText").getValue();
                    String msgTime = (String) Objects.requireNonNull(dataSnapshot.child("messageTime").getValue());
                    mChat.add(new Message(receiverUid, msgText, msgTime));
                    sortMessages();
                    chatAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void sortMessages(){
        //sort by time
        Collections.sort(mChat, new Comparator<Message>() {
            public int compare(Message o1, Message o2) {
                if (o1.getMessageTime() == null || o2.getMessageTime() == null)
                    return 0;
                return o1.getMessageTime().compareTo(o2.getMessageTime());
            }
        });
    }

    private void clearDisplayedText(String sender){
        for(int i = 0; i < mChat.size();i++){
            Message alreadyShowed = mChat.get(i);
            if(alreadyShowed.getMessageSender().equals(sender)){
                mChat.remove(i);
            }
        }
    }



    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


        chatAdapter = new ChatCustomAdapter(mChat);
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();

    }

    private void smoothScrolling(int position){
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }


    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    @Override
    protected void onStart() {
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        super.onStart();

    }

    @Override
    protected void onStop(){
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}

