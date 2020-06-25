package com.example.textapp.recycler_views.Chat;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;


import java.util.ArrayList;


public class messageListAdapter extends RecyclerView.Adapter {

    ArrayList<messageObject> messageList;

    String chatID;
    Context context;

    private Boolean nameSet = false;
    private Boolean forwnameSet = false;

    public messageListAdapter(ArrayList<messageObject> messageList, String chatID, Context context) {
        this.messageList = messageList;
        this.chatID = chatID;
        this.context = context;

    }


    public interface OnSingleItemClickListener {
        void onSingleItemClick(int position, String user);

        void onSingleLongItemClick(int position);
    }

    private OnSingleItemClickListener mlistener;

    public void setSingletOnItemClickListener(OnSingleItemClickListener listener) {
        mlistener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        if (!messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            return 0;
        }
        return 1;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == 0) {
            view = layoutInflater.inflate(R.layout.message_item_receive_layout, parent, false);
            return new messageListViewHolderReceive(view, mlistener);
        }
        view = layoutInflater.inflate(R.layout.message_item_send_layout, parent, false);
        return new messageListViewHolderSend(view, mlistener);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {


        if (!messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            final messageListViewHolderReceive messageListViewHolderReceive = (messageListAdapter.messageListViewHolderReceive) holder;

            messageListViewHolderReceive.message.setText(messageList.get(position).getMessage());

            messageListViewHolderReceive.time.setText(messageList.get(position).getTime());

            messageListViewHolderReceive.sender.setVisibility(View.VISIBLE);

            final DatabaseReference userNameValue = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("user");
            userNameValue.keepSynced(true);

            userNameValue.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.getChildrenCount() > 2) {
                            if (messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                                messageListViewHolderReceive.sender.setText("You");
                            } else {
                                final String userUID = messageList.get(position).getSenderId();
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

                                                    messageListViewHolderReceive.sender.setText(contactName);
                                                    nameSet = true;

                                                }
                                            }
                                            if (nameSet == false) {
                                                messageListViewHolderReceive.sender.setText(number);

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                        } else {
                            messageListViewHolderReceive.sender.setVisibility(View.GONE);
                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if (messageList.get(position).getIsForwarded().equals("-1") || messageList.get(position).getIsDeleted().equals("true")) {
                messageListViewHolderReceive.forwardedMsg_layout.setVisibility(View.GONE);
                messageListViewHolderReceive.forwardedMsg_name.setVisibility(View.GONE);
                messageListViewHolderReceive.forwardedMsg.setVisibility(View.GONE);

            } else {

                messageListViewHolderReceive.forwardedMsg_layout.setVisibility(View.VISIBLE);

                messageListViewHolderReceive.forwardedMsg_name.setVisibility(View.VISIBLE);


                if (messageList.get(Integer.parseInt(messageList.get(position).getIsForwarded())).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                    messageListViewHolderReceive.forwardedMsg_name.setText("You");
                } else {
                    final String userUID = messageList.get(Integer.parseInt(messageList.get(position).getIsForwarded())).getSenderId();
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

                                        messageListViewHolderReceive.forwardedMsg_name.setText(contactName);
                                        forwnameSet = true;

                                    }
                                }
                                if (forwnameSet == false) {
                                    messageListViewHolderReceive.forwardedMsg_name.setText(number);

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }


                messageListViewHolderReceive.forwardedMsg.setVisibility(View.VISIBLE);
                messageListViewHolderReceive.forwardedMsg.setText(messageList.get(Integer.parseInt(messageList.get(position).getIsForwarded())).getMessage());
            }

            if (messageList.get(holder.getAdapterPosition()).getMediaURLlist().isEmpty() || messageList.get(position).getIsDeleted().equals("true")) {
                messageListViewHolderReceive.ViewMedia.setVisibility(View.GONE);
            } else {
                messageListViewHolderReceive.ViewMedia.setVisibility(View.VISIBLE);
            }
            messageListViewHolderReceive.ViewMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new ImageViewer.Builder(view.getContext(), messageList.get(messageListViewHolderReceive.getAdapterPosition()).getMediaURLlist())
                            .setStartPosition(0)
                            .show();
                }
            });

        } else {

            final messageListViewHolderSend messageListViewHolderSend = (messageListAdapter.messageListViewHolderSend) holder;

            messageListViewHolderSend.messageSend.setText(messageList.get(position).getMessage());
            messageListViewHolderSend.timeSend.setText(messageList.get(position).getTime());
            messageListViewHolderSend.senderSend.setVisibility(View.VISIBLE);

            final DatabaseReference userNameValue = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("user");
            userNameValue.keepSynced(true);

            userNameValue.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.getChildrenCount() > 2) {
                            if (messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                                messageListViewHolderSend.senderSend.setText("You");
                            } else {
                                final String userUID = messageList.get(position).getSenderId();
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

                                                    messageListViewHolderSend.senderSend.setText(contactName);
                                                    nameSet = true;

                                                }
                                            }
                                            if (nameSet == false) {
                                                messageListViewHolderSend.senderSend.setText(number);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                        } else {
                            messageListViewHolderSend.senderSend.setVisibility(View.GONE);
                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if (messageList.get(position).getIsForwarded().equals("-1") || messageList.get(position).getIsDeleted().equals("true")) {
                messageListViewHolderSend.forwardedMsg_layout_send.setVisibility(View.GONE);
                messageListViewHolderSend.forwardedMsg_name_send.setVisibility(View.GONE);
                messageListViewHolderSend.forwardedMsg_send.setVisibility(View.GONE);

            } else {

                messageListViewHolderSend.forwardedMsg_layout_send.setVisibility(View.VISIBLE);

                messageListViewHolderSend.forwardedMsg_name_send.setVisibility(View.VISIBLE);

                if (messageList.get(Integer.parseInt(messageList.get(position).getIsForwarded())).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
                    messageListViewHolderSend.forwardedMsg_name_send.setText("You");
                } else {
                    final String userUID = messageList.get(Integer.parseInt(messageList.get(position).getIsForwarded())).getSenderId();
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

                                        messageListViewHolderSend.forwardedMsg_name_send.setText(contactName);
                                        forwnameSet = true;

                                    }
                                }
                                if (forwnameSet == false) {
                                    messageListViewHolderSend.forwardedMsg_name_send.setText(number);

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }


                messageListViewHolderSend.forwardedMsg_send.setVisibility(View.VISIBLE);
                messageListViewHolderSend.forwardedMsg_send.setText(messageList.get(Integer.parseInt(messageList.get(position).getIsForwarded())).getMessage());
            }

            if (messageList.get(holder.getAdapterPosition()).getMediaURLlist().isEmpty() || messageList.get(position).getIsDeleted().equals("true")) {
                messageListViewHolderSend.ViewMediaSend.setVisibility(View.GONE);
            } else {
                messageListViewHolderSend.ViewMediaSend.setVisibility(View.VISIBLE);
            }

            messageListViewHolderSend.ViewMediaSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new ImageViewer.Builder(view.getContext(), messageList.get(messageListViewHolderSend.getAdapterPosition()).getMediaURLlist())
                            .setStartPosition(0)
                            .show();
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }


    class messageListViewHolderReceive extends RecyclerView.ViewHolder {

        LinearLayout layout;
        LinearLayout forwardedMsg_layout;
        TextView message, sender, forwardedMsg_name, forwardedMsg, time;
        Button ViewMedia;

        messageListViewHolderReceive(@NonNull final View itemView, final OnSingleItemClickListener listener) {
            super(itemView);
            layout = itemView.findViewById(R.id.message_layout);
            message = itemView.findViewById(R.id.msgBox);
            sender = itemView.findViewById(R.id.senderBox);
            ViewMedia = itemView.findViewById(R.id.viewmedia);
            time = itemView.findViewById(R.id.timeBox);

            forwardedMsg_name = itemView.findViewById(R.id.forwarded_msg_name_box);
            forwardedMsg = itemView.findViewById(R.id.forwarded_msg_Box);
            forwardedMsg_layout = itemView.findViewById(R.id.forwarded_msg_layout);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleItemClick(position, "receiver");

                        }
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleLongItemClick(position);
                        }
                    }

                    return true;
                }
            });


        }


    }

    class messageListViewHolderSend extends RecyclerView.ViewHolder {

        LinearLayout layoutSend, forwardedMsg_layout_send;
        TextView messageSend, senderSend, forwardedMsg_name_send, forwardedMsg_send, timeSend;
        Button ViewMediaSend;

        public messageListViewHolderSend(@NonNull final View itemView, final OnSingleItemClickListener listener) {
            super(itemView);
            layoutSend = itemView.findViewById(R.id.message_send_layout);
            messageSend = itemView.findViewById(R.id.msgBoxSend);
            senderSend = itemView.findViewById(R.id.senderBoxSend);
            ViewMediaSend = itemView.findViewById(R.id.viewmediaSend);
            timeSend = itemView.findViewById(R.id.timeBoxSend);

            forwardedMsg_name_send = itemView.findViewById(R.id.forwarded_msg_name_box_send);
            forwardedMsg_send = itemView.findViewById(R.id.forwarded_msg_Box_send);
            forwardedMsg_layout_send = itemView.findViewById(R.id.forwarded_msg_layout_send);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleItemClick(position, "sender");


                        }
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSingleLongItemClick(position);
                        }
                    }

                    return true;
                }
            });


        }


    }


}

