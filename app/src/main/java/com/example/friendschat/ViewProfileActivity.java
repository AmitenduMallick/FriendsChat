package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {


    private TextView username;
    private TextView userStatus;
    private CircleImageView profileImage;
    private Button sendRequest,declineRequest;
    String receiverUserId,Current_state;
    FirebaseAuth mAuth;
    String senderUserId;
    private DatabaseReference NotificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        receiverUserId=getIntent().getStringExtra("visited_ID");

        username=findViewById(R.id.visit_profile_username);
        userStatus=findViewById(R.id.visit_profile_status);
        profileImage=findViewById(R.id.visit_profile_image);
        sendRequest=findViewById(R.id.send_message_request_button);
        declineRequest=findViewById(R.id.decline_message_request_button);
        Current_state="new";
        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");


        FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child("imageurl").exists()){
                    username.setText(snapshot.child("name").getValue().toString());
                    userStatus.setText(snapshot.child("status").getValue().toString());
                    Picasso.get().load(snapshot.child("imageurl").getValue().toString()).placeholder(R.drawable.profile_image).into(profileImage);
                    ManageChatRequests();
                }else{
                    username.setText(snapshot.child("name").getValue().toString());
                    userStatus.setText(snapshot.child("status").getValue().toString());
                    ManageChatRequests();
                }
                


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ManageChatRequests() {

        FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild(receiverUserId)){
                    String request_type=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        Current_state="request_sent";
                        sendRequest.setText("Cancel Request");
                    }
                    else if(request_type.equals("received")){
                        Current_state="request_received";
                        sendRequest.setText("Accept Chat Request");
                        declineRequest.setVisibility(View.VISIBLE);
                        declineRequest.setEnabled(true);

                        declineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelRequest();
                            }
                        });

                    }
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Contact List").child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                Current_state="friends";
                                sendRequest.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!(senderUserId.equals(receiverUserId))){
            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequest.setEnabled(false);

                    if(Current_state.equals("new")){
                        SendChatRequest();
                    }
                    if(Current_state.equals("request_sent")){
                        sendRequest.setEnabled(true);
                        CancelRequest();
                    }
                    if(Current_state.equals("request_received")){
                        AcceptChatRequest();
                    }

                    if(Current_state.equals("friends")){
                        RemoveContact();
                    }
                }
            });
        }else{

            sendRequest.setVisibility(View.INVISIBLE);
        }


    }

    private void RemoveContact() {

        FirebaseDatabase.getInstance().getReference().child("Contact List").child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isComplete()){
                    FirebaseDatabase.getInstance().getReference().child("Contact List").child(receiverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            sendRequest.setEnabled(true);

                            sendRequest.setText("Send Request");
                            Current_state="new";
                            declineRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setEnabled(false);

                        }
                    });
                }

            }
        });
    }

    private void AcceptChatRequest() {

        FirebaseDatabase.getInstance().getReference().child("Contact List").child(senderUserId).child(receiverUserId).child("Contacts")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference().child("Contact List").child(receiverUserId).child(senderUserId)
                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(senderUserId).child(receiverUserId)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            FirebaseDatabase.getInstance().getReference().child(receiverUserId).child(senderUserId)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    sendRequest.setEnabled(true);
                                                    Current_state="friends";
                                                    sendRequest.setText("Remove this Contact");
                                                    declineRequest.setVisibility(View.INVISIBLE);

                                                }
                                            });
                                        }

                                    }
                                });
                            }

                        }
                    });
                }

            }
        });
    }

    private void CancelRequest() {
        FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isComplete()){
                    FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(receiverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            sendRequest.setEnabled(true);

                            sendRequest.setText("Send Request");
                            Current_state="new";
                            declineRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setEnabled(false);

                        }
                    });
                }

            }
        });

    }

    private void SendChatRequest() {

        FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            FirebaseDatabase.getInstance().getReference().child("Chat Requests").child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        HashMap<String,Object> chatNotify=new HashMap<>();
                                        chatNotify.put("from",senderUserId);
                                        chatNotify.put("type","request");
                                        NotificationRef.child(receiverUserId).push().setValue(chatNotify).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    sendRequest.setEnabled(true);
                                                    Current_state="request_sent";
                                                    sendRequest.setText("Cancel Request");

                                                }

                                            }
                                        });


                                    }

                                }
                            });
                        }

                    }
                });
    }
}