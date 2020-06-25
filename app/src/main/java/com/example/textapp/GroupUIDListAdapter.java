package com.example.textapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textapp.recycler_views.chatListView.chatListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupUIDListAdapter extends RecyclerView.Adapter<GroupUIDListAdapter.GroupUIDlistViewHolder> {

    Context context;
    ArrayList<String> groupMemberUIDlist;

    private Boolean nameSet=false;

    public GroupUIDListAdapter(Context context, ArrayList<String> groupMemberUIDlist) {
        this.context = context;
        this.groupMemberUIDlist = groupMemberUIDlist;
    }

    @NonNull
    @Override
    public GroupUIDlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layout_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_member_info_item, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout_view.setLayoutParams(layoutParams);

        GroupUIDlistViewHolder rcv = new GroupUIDlistViewHolder(layout_view);

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupUIDlistViewHolder holder,final int position) {

        final DatabaseReference userNameValue = FirebaseDatabase.getInstance().getReference().child("user").child(groupMemberUIDlist.get(position)).child("phone");
        userNameValue.keepSynced(true);

        userNameValue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
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

                            holder.profile_name.setText(contactName);
                            nameSet=true;
                        }
                    }

                    if (nameSet==false)
                    {
                        holder.profile_name.setText(number);
                    }


                    DatabaseReference userprofilepic = FirebaseDatabase.getInstance().getReference().child("user").child(groupMemberUIDlist.get(position)).child("profilePicture");
                    userprofilepic.keepSynced(true);

                    userprofilepic.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                if (!dataSnapshot.getValue().toString().equals("-1"))
                                {
                                    Glide.with(context).load(Uri.parse(dataSnapshot.getValue().toString())).into(holder.profile_Image);
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

    @Override
    public int getItemCount() {
        return groupMemberUIDlist.size();
    }


    public class GroupUIDlistViewHolder extends RecyclerView.ViewHolder{


        public TextView profile_name;
        public LinearLayout profile_layout;
        public ImageView profile_Image;
        public GroupUIDlistViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_layout=itemView.findViewById(R.id.groupinfo_layout);
            profile_name=itemView.findViewById(R.id.profilenameBOX);
            profile_Image=itemView.findViewById(R.id.profileimgBOX);

        }
    }
}
