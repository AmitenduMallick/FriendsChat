package com.example.friendschat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList){

        this.userMessagesList=userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String currentSavedDate,currentSavedTime;


        final String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);
        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild("imageurl")){
                    String receiverImage=snapshot.child("imageurl").toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageReceiverImageView.setVisibility(View.GONE);
        holder.messageSenderImageView.setVisibility(View.GONE);

        if(fromMessageType.equals("text")){




            if(fromUserId.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage()+"\n\n"+messages.getDate());
            }else{



                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setText(messages.getMessage()+"\n\n"+messages.getDate());



            }
        }
        else if(fromMessageType.equals("image")){
            if(fromUserId.equals(messageSenderId)){
                holder.messageSenderImageView.setVisibility(View.VISIBLE);
                holder.senderMessageText.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderImageView);
                holder.senderMessageText.setText(messages.getDate());

            }else{
                holder.messageReceiverImageView.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImageView);
                holder.receiverMessageText.setText(messages.getDate());
            }
        }
        else{
            if(fromUserId.equals(messageSenderId)){
                holder.messageSenderImageView.setVisibility(View.VISIBLE);
                holder.messageSenderImageView.setBackgroundResource(R.drawable.file);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }else{
                holder.messageReceiverImageView.setVisibility(View.VISIBLE);
                holder.messageReceiverImageView.setBackgroundResource(R.drawable.file);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        if(fromUserId.equals(messageSenderId)){
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(userMessagesList.get(position).getType().equals("pdf")){
                        CharSequence options[]=new CharSequence[]
                                {
                                  "Delete for me","Delete for everyone","cancel"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0){

                                    deleteSentMessage(position,holder);




                                }
                                else if(i==1){
                                    int a=userMessagesList.indexOf(position);
                                    Log.i("Position is ",a+"");
                                    deleteForEveryone(position,holder);


                                }

                            }
                        });
                        builder.show();

                    }

                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me","Delete for everyone","cancel"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0){

                                    deleteSentMessage(position,holder);


                                }
                                else if(i==1){
                                    deleteForEveryone(position,holder);
                                    //holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(),MainActivity.class));








                                }

                            }
                        });
                        builder.show();

                    }

                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me","Delete for everyone","cancel"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0){
                                    deleteSentMessage(position,holder);

                                }
                                else if(i==1){

                                    deleteForEveryone(position,holder);



                                }

                            }
                        });
                        builder.show();

                    }



                    return true;
                }
            });
        }
        else{
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me","cancel"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0){

                                    deleteReceivedMessage(position,holder);

                                }


                            }
                        });
                        builder.show();

                    }

                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me","cancel"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0){

                                    deleteReceivedMessage(position,holder);

                                }

                            }
                        });
                        builder.show();

                    }

                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me","cancel"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0){

                                    deleteReceivedMessage(position,holder);

                                }


                            }
                        });
                        builder.show();

                    }



                    return true;
                }
            });
        }

        holder.messageReceiverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Delete for me","View This Image","cancel"
                        };

                AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Delete Message");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i==0){

                            deleteReceivedMessage(position,holder);

                        }
                        if(i==1){

                            Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                            intent.putExtra("url",userMessagesList.get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);

                        }


                    }
                });
                builder.show();



            }
        });
        holder.messageSenderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Delete for me","View This Image","Delete for everyone","cancel"
                        };

                AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Delete Message");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i==0){

                            deleteReceivedMessage(position,holder);

                        }
                        if(i==1){

                            Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                            intent.putExtra("url",userMessagesList.get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);

                        }
                        if(i==2){
                            deleteForEveryone(position,holder);
                        }


                    }
                });
                builder.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessage(final int position,final MessageViewHolder holder){

        DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();
        Rootref.child("Messages").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Sucessfully", Toast.LENGTH_SHORT).show();
                }
                
            }
        });

    }
    private void deleteReceivedMessage(final int position,final MessageViewHolder holder){
        DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();
        Rootref.child(("Messages")).child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted sucessfully", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void deleteForEveryone(final int position,final MessageViewHolder holder){
        final DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();

        Rootref.child("Messages").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Rootref.child("Messages").child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getFrom())
                        .child(userMessagesList.get(position).getMessageid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Deleted Sucessfully", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView senderMessageText,receiverMessageText;
        public ImageView messageSenderImageView;
        public ImageView messageReceiverImageView;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText=itemView.findViewById(R.id.sender_message_text);

            receiverMessageText=itemView.findViewById(R.id.receiver_message_text);
            messageSenderImageView=itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverImageView=itemView.findViewById(R.id.message_receiver_image_view);
        }
    }

}
