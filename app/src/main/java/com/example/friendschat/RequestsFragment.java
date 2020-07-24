package com.example.friendschat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    View view;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestsList=view.findViewById(R.id.requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_decline_button).setVisibility(View.VISIBLE);

                final String listUserId=getRef(position).getKey();
                DatabaseReference typeRef=getRef(position).child("request_type").getRef();

                typeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String type=snapshot.getValue().toString();
                            Log.i("type",type);


                            if(type.equals("received")){



                                FirebaseDatabase.getInstance().getReference().child("Users").child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Log.i("User",listUserId);

                                        if(snapshot.hasChild("imageurl")){
                                            holder.username.setText(snapshot.child("name").getValue().toString());
                                            holder.status.setText(snapshot.child("status").getValue().toString());
                                            Picasso.get().load(snapshot.child("imageurl").getValue().toString()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }else{
                                            holder.username.setText(snapshot.child("name").getValue().toString());
                                            holder.status.setText(snapshot.child("status").getValue().toString());
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                               CharSequence options[]=new CharSequence[]
                                                       {
                                                               "Accept",
                                                               "Cancel"
                                                       };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i==0){

                                                            FirebaseDatabase.getInstance().getReference().child("Contact List").child(currentUserId).child(listUserId)
                                                                    .child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){
                                                                        FirebaseDatabase.getInstance().getReference().child("Contact List").child(listUserId).child(currentUserId)
                                                                                .child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful()){
                                                                                    ChatRequestRef.child(currentUserId).child(listUserId)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if (task.isSuccessful()){
                                                                                                ChatRequestRef.child(listUserId).child(currentUserId)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if(task.isSuccessful()){
                                                                                                            Toast.makeText(getContext(), "Contacts saved", Toast.LENGTH_SHORT).show();
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
                                                            });

                                                        }
                                                        if(i==1){

                                                            ChatRequestRef.child(currentUserId).child(listUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        ChatRequestRef.child(listUserId).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "Declined", Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            });


                                                        }

                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else if(type.equals("sent")){

                                Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_button);
                                request_sent_btn.setText("Request sent");
                                holder.itemView.findViewById(R.id.request_decline_button).setVisibility(View.INVISIBLE);

                                FirebaseDatabase.getInstance().getReference().child("Users").child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Log.i("User",listUserId);

                                        if(snapshot.hasChild("imageurl")){
                                            holder.username.setText(snapshot.child("name").getValue().toString());
                                            holder.status.setText(snapshot.child("status").getValue().toString());
                                            Picasso.get().load(snapshot.child("imageurl").getValue().toString()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }else{
                                            holder.username.setText(snapshot.child("name").getValue().toString());
                                            holder.status.setText(snapshot.child("status").getValue().toString());
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        };
                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already sent request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {

                                                        if(i==0){

                                                            ChatRequestRef.child(currentUserId).child(listUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        ChatRequestRef.child(listUserId).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            });


                                                        }

                                                    }
                                                });

                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                RequestViewHolder viewHolder=new RequestViewHolder(view);
                return viewHolder;
            }
        };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        TextView status;
        CircleImageView profileImage;
        Button acceptButton,declineButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.user_profile_name);
            status=itemView.findViewById(R.id.user_profile_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            acceptButton=itemView.findViewById(R.id.request_accept_button);
            declineButton=itemView.findViewById(R.id.request_decline_button);
        }
    }
}