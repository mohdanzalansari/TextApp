package com.example.textapp.recycler_views.chatListView;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textapp.R;
import com.example.textapp.recycler_views.Chat.messageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.chatListViewHolder> {

    ArrayList<chatObject> chatList;
    Context context;


    private String messageActivityTitle;
    private Boolean nameSet=false;

    public chatListAdapter(Context context, ArrayList<chatObject> chatList) {

        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    public chatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layout_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_layout, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout_view.setLayoutParams(layoutParams);

        chatListAdapter.chatListViewHolder rcv = new chatListAdapter.chatListViewHolder(layout_view);

        return rcv;
    }


    public void onBindViewHolder(@NonNull final chatListViewHolder holder, final int position) {



        DatabaseReference userChildCount = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatList.get(position).getChatid()).child("groupName");
        userChildCount.keepSynced(true);

        userChildCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    holder.title.setText(dataSnapshot.getValue().toString());

                    DatabaseReference userGroupprofilepic = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatList.get(position).getChatid()).child("groupProfilePicture");
                    userGroupprofilepic.keepSynced(true);

                    userGroupprofilepic.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                if (!dataSnapshot.getValue().toString().equals("-1"))
                                {
                                    Glide.with(context).load(Uri.parse(dataSnapshot.getValue().toString())).into(holder.profileImage);
                                }
                                else
                                {
                                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_group_image);
                                    holder.profileImage.setImageBitmap(bitmap);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });





                } else {
                    final DatabaseReference userNameValue = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatList.get(position).getChatid()).child("user");
                    userNameValue.keepSynced(true);


                    userNameValue.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                                    if (!childSnapShot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                        final String userUID = childSnapShot.getKey();


                                        DatabaseReference userNumber = FirebaseDatabase.getInstance().getReference().child("user").child(userUID).child("phone");

                                        userNumber.keepSynced(true);


                                        userNumber.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    String number = dataSnapshot.getValue().toString();

                                                    Cursor ph = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                                                    while (ph.moveToNext()) {
                                                        String contactName = ph.getString(ph.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                                        String contactNumber = ph.getString(ph.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                                        contactNumber = contactNumber.replace(" ", "");
                                                        contactNumber = contactNumber.replace("-", "");
                                                        contactNumber = contactNumber.replace("(", "");
                                                        contactNumber = contactNumber.replace(")", "");

                                                        if (!String.valueOf(contactNumber.charAt(0)).equals("+")) {
                                                            contactNumber = "+91" + contactNumber;
                                                        }

                                                        if (contactNumber.equals(number)) {
                                                            messageActivityTitle = contactName;
                                                            holder.title.setText(messageActivityTitle);
                                                            nameSet=true;
                                                        }
                                                    }

                                                    if (nameSet==false)
                                                    {
                                                        holder.title.setText(number);
                                                    }


                                                            DatabaseReference userprofilepic = FirebaseDatabase.getInstance().getReference().child("user").child(userUID).child("profilePicture");
                                                            userprofilepic.keepSynced(true);

                                                            userprofilepic.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.exists())
                                                                    {
                                                                        if (!dataSnapshot.getValue().toString().equals("-1"))
                                                                        {
                                                                            Glide.with(context).load(Uri.parse(dataSnapshot.getValue().toString())).into(holder.profileImage);
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });











                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference userLastMessage = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatList.get(position).getChatid()).child("lastMessage");
        userLastMessage.keepSynced(true);

        userLastMessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    holder.lastMessage.setText(dataSnapshot.getValue().toString());
                }
                else
                {
                    holder.lastMessage.setText("No chat started yet, click to start chat...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });







        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), messageActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("chatID", chatList.get(holder.getAdapterPosition()).getChatid());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });
    }


    public int getItemCount() {
        return chatList.size();
    }

    public class chatListViewHolder extends RecyclerView.ViewHolder {

        public TextView title,lastMessage;
        public LinearLayout layout;
        public ImageView profileImage;

        public chatListViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.chat_user_title);
            lastMessage=itemView.findViewById(R.id.last_text_box);
            profileImage=itemView.findViewById(R.id.Profile_image);
            layout = itemView.findViewById(R.id.chatItemLayout);
        }
    }


}
