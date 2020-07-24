package com.example.friendschat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    View view;
    private RecyclerView chatList;
    private FirebaseAuth mAuth;
    private String currentUser;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_chats, container, false);
        chatList=view.findViewById(R.id.chats_list);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser().getUid();
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Contact List").child(currentUser),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                final String usersId=getRef(position).getKey();
                final String[] retImage = {"default"};
                FirebaseDatabase.getInstance().getReference().child("Users").child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()) {

                            if (snapshot.hasChild("imageurl")) {
                                retImage[0] = snapshot.child("imageurl").getValue().toString();
                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            final String retName = snapshot.child("name").getValue().toString();
                            final String retStatus = snapshot.child("status").getValue().toString();


                            holder.username.setText(retName);
                            Log.i("Text",holder.username.getText().toString());

                            if(snapshot.child("userState").hasChild("state")){
                                String date=snapshot.child("userState").child("date").getValue().toString();
                                String time =snapshot.child("userState").child("time").getValue().toString();
                                String state=snapshot.child("userState").child("state").getValue().toString();

                                if(state.equals("online")){
                                    holder.status.setText("online");
                                    holder.onlineStatus.setVisibility(View.VISIBLE);
                                }else if(state.equals("offline")){
                                    holder.status.setText("Last seen: "  + date +" "+ time);
                                    holder.onlineStatus.setVisibility(View.INVISIBLE);
                                }
                            }else{
                                holder.status.setText("offline");
                            }



                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent=new Intent(getContext(),ChatActivity.class);
                                    intent.putExtra("visit_user_id",usersId);
                                    intent.putExtra("visit_user_image", retImage[0]);
                                    intent.putExtra("visit_username",retName);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout,parent,false);
                ChatsViewHolder viewHolder=new ChatsViewHolder(view);
                return viewHolder;
            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        TextView status;
        CircleImageView profileImage;
        ImageView onlineStatus;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            username=itemView.findViewById(R.id.user_profile_name);
            status=itemView.findViewById(R.id.user_profile_status);
            onlineStatus=itemView.findViewById(R.id.user_online_status);
        }
    }


}