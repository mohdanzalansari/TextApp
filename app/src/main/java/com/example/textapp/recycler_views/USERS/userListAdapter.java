package com.example.textapp.recycler_views.USERS;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textapp.R;
import com.example.textapp.recycler_views.Chat.messageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class userListAdapter extends RecyclerView.Adapter<userListAdapter.userListViewHolder> {


    ArrayList<userObject> userList;


    public userListAdapter(ArrayList<userObject> userList) {

        this.userList=userList;
    }

    public interface OnUserListSingleItemClickListener
    {
        void onUserListSingleItemClick (int position);
        void onUserListLongSingleItemClick(int position);

    }

    private OnUserListSingleItemClickListener mlistener;

    public void setSingleClickItemListener(OnUserListSingleItemClickListener listener)
    {
        mlistener=listener;
    }

    @NonNull
    @Override
    public userListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layout_view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_layout,null,false);
        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layout_view.setLayoutParams(layoutParams);

        userListViewHolder rcv= new userListViewHolder(layout_view,mlistener);

        return rcv;
    }



    @Override
    public void onBindViewHolder(@NonNull userListViewHolder holder, final int position) {

        holder.uname.setText(userList.get(position).getName());
        holder.unum.setText(userList.get(position).getPhnum());

    }




    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class userListViewHolder extends RecyclerView.ViewHolder {

        public TextView uname,unum;
        public LinearLayout layout;

        public userListViewHolder(@NonNull View itemView,final OnUserListSingleItemClickListener listener) {
            super(itemView);
            uname=itemView.findViewById(R.id.user_name);
            unum=itemView.findViewById(R.id.user_num);
            layout=itemView.findViewById(R.id.userItemLayout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                    {
                        int position=getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION)
                        {
                            listener.onUserListSingleItemClick(position);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(listener!=null)
                    {
                        int position=getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION)
                        {
                            listener.onUserListLongSingleItemClick(position);
                        }
                    }
                    return true;
                }
            });

        }


    }


}


