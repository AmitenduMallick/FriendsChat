package com.example.friendschat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.Viewholder> {

    List<Contacts> userList;
    Context context;

    public ContactsAdapter(List<Contacts> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
        return new ContactsAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, final int position) {

        final Contacts contacts=userList.get(position);
        holder.userProfileName.setText(contacts.getName());
        holder.userProfileStatus.setText(contacts.getStatus());
        Picasso.get().load(contacts.getImageurl()).placeholder(R.drawable.profile_image).into(holder.userProfileImage);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String uid=contacts.getUid();
                Intent intent=new Intent(context,ViewProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visited_ID",uid);
                context.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{

        TextView userProfileName;
        TextView userProfileStatus;
        CircleImageView userProfileImage;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            userProfileName=itemView.findViewById(R.id.user_profile_name);
            userProfileStatus=itemView.findViewById(R.id.user_profile_status);
            userProfileImage=itemView.findViewById(R.id.users_profile_image);


        }
    }

}
