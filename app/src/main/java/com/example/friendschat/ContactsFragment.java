package com.example.friendschat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ContactsFragment extends Fragment {

    View ContactsView;
    private RecyclerView myContactList;
    FirebaseAuth mAuth;
    String currentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView=inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser().getUid();

        myContactList=ContactsView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Contact List").child(currentUser),Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Contacts model) {

                String userId=getRef(position).getKey();
                FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            if(snapshot.child("userState").hasChild("state")){
                                String date=snapshot.child("userState").child("date").getValue().toString();
                                String time =snapshot.child("userState").child("time").getValue().toString();
                                String state=snapshot.child("userState").child("state").getValue().toString();

                                if(state.equals("online")){
                                    holder.userOnlineStatus.setVisibility(View.VISIBLE);
                                }else if(state.equals("offline")){

                                    holder.userOnlineStatus.setVisibility(View.INVISIBLE);
                                }
                            }else{
                                holder.userOnlineStatus.setVisibility(View.INVISIBLE);
                            }


                            if(snapshot.child("imageurl").exists()){
                                holder.username.setText(snapshot.child("name").getValue().toString());
                                holder.status.setText(snapshot.child("status").getValue().toString());
                                Picasso.get().load(snapshot.child("imageurl").getValue().toString()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else{

                                holder.username.setText(snapshot.child("name").getValue().toString());
                                holder.status.setText(snapshot.child("status").getValue().toString());

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visited_ID=getRef(position).getKey();
                        Intent intent=new Intent(holder.itemView.getContext(),ViewProfileActivity.class);
                        intent.putExtra("visited_ID",visited_ID);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        TextView status;
        CircleImageView profileImage;
        ImageView userOnlineStatus;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.user_profile_name);
            status=itemView.findViewById(R.id.user_profile_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            userOnlineStatus=itemView.findViewById(R.id.user_online_status);
        }
    }
}