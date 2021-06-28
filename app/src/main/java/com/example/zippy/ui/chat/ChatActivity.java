package com.example.zippy.ui.chat;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zippy.R;
import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.helper.ChatCustomAdapter;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.Messages;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    BottomNavigationView bottomNavigationView;

    SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";
    final String strClickedUid = "clickedUid";
    String clickedUid, senderUid, receiverUid ;
    String loggedProfile;

    private ImageView imgView;
    private TextView fullname;
    private EditText inputText;
    private ImageView sendButton;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceInstructor, reference;

    ChatCustomAdapter chatAdapter;
    ArrayList<Messages> mChat;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        auth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        senderUid = user.getUid();
        receiverUid = clickedUid;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedUid = mPrefs.getString(strClickedUid, "");
        loggedProfile = mPrefs.getString(loggedStatus, "nouser");

        imgView = findViewById(R.id.imgview);
        fullname = findViewById(R.id.txtviewfullname);
        inputText = findViewById(R.id.edittxtinputtext);
        sendButton = findViewById(R.id.imgbtnsend);

        recyclerView = findViewById(R.id.recyclerview);
       // LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //recyclerView.setLayoutManager(linearLayoutManager);


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

        bottomNavigationView = findViewById(R.id.bottom_navigatin_view);
        BottomNavigationHelper bottomNavigationHelper = new BottomNavigationHelper(bottomNavigationView, this);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationHelper.handle();
        //bottomNavigationView.setSelectedItemId(R.id.navigation_chat);
    }

    private void setChatMenu() {
        referenceInstructor = rootNode.getReference("instructors/" + clickedUid);
        referenceStudent = rootNode.getReference("students/" + clickedUid);

        referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StudentHelperClass studentHelper = snapshot.getValue(StudentHelperClass.class);
                    assert studentHelper != null;
                    fullname.setText(studentHelper.getFullName());
                    Glide.with(getBaseContext()).load(studentHelper.getImage()).into(imgView);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    InstructorHelperClass instructorHelper = snapshot.getValue(InstructorHelperClass.class);
                    assert instructorHelper != null;
                    fullname.setText(instructorHelper.getFullName());
                    Glide.with(getBaseContext()).load(instructorHelper.getImage()).into(imgView);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void sendMessage(String messageText){

        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("sender",senderUid);
        hashMap.put("receiver", clickedUid);
        hashMap.put("text", messageText);
        reference.child("messages").push().setValue(hashMap);

    }

    public void showMessage(){
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    if(messages.getMessageSender().equals(senderUid) && messages.getMessageReceiver().equals(receiverUid) ||
                     messages.getMessageReceiver().equals(senderUid) && messages.getMessageSender().equals(receiverUid)){
                        mChat.add(messages);
                    }

                    //chatAdapter = new ChatCustomAdapter(mChat, ChatActivity.this);
                   // recyclerView.setAdapter(chatAdapter);
                    initRecyclerView(mChat);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }



    private void initRecyclerView(ArrayList<Messages> messagesArrayList) {
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        chatAdapter = new ChatCustomAdapter(messagesArrayList, this);
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {
        BottomNavigationHelper.backToProfile(bottomNavigationView, this);
    }
}

