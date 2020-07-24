package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.icu.text.Edits;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView displayTextMessage;
    private EditText userInputMessage;
    private ImageButton sendMessage;
    private ScrollView scrollView;
    private String getGroupName;
    private FirebaseAuth auth;
    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        getGroupName=getIntent().getStringExtra("groupName");
        auth=FirebaseAuth.getInstance();
        userId=auth.getCurrentUser().getUid();
        
        InitializeFields();
        getUserInfo();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInfoToDatabase();

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().getReference().child("Groups").child(getGroupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot snapshot) {

        Iterator iterator=snapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName+":\n"+chatMessage+"\n"+chatTime+"     "+chatDate+"\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }


    }

    private void getUserInfo() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userName=snapshot.child("name").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendInfoToDatabase() {
        String message=userInputMessage.getText().toString();
        final String currentDate;
        final String currentTime;
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Nothing is written!!", Toast.LENGTH_SHORT).show();
        }else{
            Calendar callForDate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MM dd, yyyy");
            currentDate=currentDateFormat.format(callForDate.getTime());

            Calendar callForTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(callForTime.getTime());

            HashMap<String,Object> map=new HashMap<>();
            map.put("name",userName);
            map.put("message",message);
            map.put("date",currentDate);
            map.put("time",currentTime);

            FirebaseDatabase.getInstance().getReference().child("Groups").child(getGroupName).push().setValue(map);
            userInputMessage.setText("");

        }

    }

    private void InitializeFields() {

        toolbar=findViewById(R.id.group_chat_appbar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getGroupName);
        sendMessage=findViewById(R.id.group_message_send_button);
        displayTextMessage=findViewById(R.id.group_chat_text_display);
        userInputMessage=findViewById(R.id.input_group_message);
        scrollView=findViewById(R.id.my_scroll_view);

    }
}