package com.example.textapp.recycler_views.Chat.ImagesMessage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.textapp.recycler_views.Chat.ImagesMessage.ImagesMessages;
import com.bumptech.glide.Glide;
import com.example.textapp.R;


import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.imageListViewHolder> {


    private ArrayList<String> mediaURIlist;
    private Context context;

    public ImageListAdapter(Context context, ArrayList<String> mediaURIlist) {

        this.mediaURIlist=mediaURIlist;
        this.context=context;

    }

    public interface OnItemClickListener
    {
        void onItemClick(int position);
    }

    private OnItemClickListener mlistener;



    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mlistener=listener;
    }





    @NonNull
    @Override
    public imageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout_view= LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_imagemessage_object,null,false);
        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layout_view.setLayoutParams(layoutParams);
        ImageListAdapter.imageListViewHolder rcv= new ImageListAdapter.imageListViewHolder(layout_view,mlistener);

        return rcv;

    }

    @Override
    public void onBindViewHolder(@NonNull imageListViewHolder holder,final int position) {

        Glide.with(context).load(Uri.parse(mediaURIlist.get(position))).into(holder.dispImage);


    }

    @Override
    public int getItemCount() {
        return mediaURIlist.size();
    }


    public class imageListViewHolder extends RecyclerView.ViewHolder
    {
        ImageView dispImage;

        public imageListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            dispImage=itemView.findViewById(R.id.messageImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null)
                    {
                        int position=getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });



        }
    }
}
