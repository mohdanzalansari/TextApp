package com.example.textapp.recycler_views.Chat.ImagesMessage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.textapp.R;
import com.example.textapp.recycler_views.Chat.messageActivity;
import com.example.textapp.recycler_views.Chat.messageListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

import static android.provider.CalendarContract.CalendarCache.URI;

public class ImagesMessages extends AppCompatActivity {


    private RecyclerView ImageDisplayView;
    private ImageListAdapter image_list_adapter;
    private RecyclerView.LayoutManager image_list_layout_manager;
    private ProgressDialog progressDialog;
    private ImageView dispImg;
    private String chatID;
    private EditText mesgBox;
    private Button sendbtn;
    private DatabaseReference messageReference;

    private ArrayList<String> mediaURIlist;
    private ArrayList<String> mediaIdList;
    private int mediaIdIterator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_messages);


        mediaURIlist = new ArrayList<>();
        mediaIdList = new ArrayList<>();
        mediaIdIterator = 0;

        mediaURIlist = getIntent().getStringArrayListExtra("ImageURIs");
        chatID = getIntent().getExtras().getString("chatId");
        dispImg = findViewById(R.id.imageset);
        mesgBox = findViewById(R.id.msgBox);
        sendbtn = findViewById(R.id.send_button);

        messageReference = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID);
        messageReference.keepSynced(true);

        setImage(mediaURIlist.get(0));
        initialize_recycler_view();

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImagemessage();
            }
        });


    }

    private void sendImagemessage() {

        progressDialog=new ProgressDialog(ImagesMessages.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending Image(s)");
        progressDialog.show();

        String messageId = messageReference.push().getKey();
        final DatabaseReference newMesg = messageReference.child(messageId);
        final HashMap newMessageMap = new HashMap();
        String msg = mesgBox.getText().toString();

        if (!msg.isEmpty()) {
            newMessageMap.put("text", msg);

        }
        newMessageMap.put("isDeleted", "false");
        newMessageMap.put("isForwarded", "-1");
        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String datetime = sdf.format(cal.getTime());
        newMessageMap.put("time", datetime);

        if (!mediaURIlist.isEmpty()) {

            for (String mediaUri : mediaURIlist) {
                final String mediaId = newMesg.child("media").push().getKey();

                mediaIdList.add(mediaId);

                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("TextApp").child("UserChat").child(chatID).child("images").child(messageId).child(mediaId);


                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mediaUri));
                } catch (IOException e) {
                    e.printStackTrace();
                    mediaURIlist.clear();
                    mediaIdList.clear();
                    mesgBox.setText(null);
                    Intent intent = new Intent(ImagesMessages.this, messageActivity.class);
                    intent.putExtra("chatID", chatID);
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                    return;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                byte[] data = baos.toByteArray();


                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                newMessageMap.put("/media/" + mediaIdList.get(mediaIdIterator) + "/", uri.toString());
                                mediaIdIterator++;

                                if (mediaIdIterator == mediaURIlist.size()) {
                                    newMesg.updateChildren(newMessageMap);
                                    mediaURIlist.clear();
                                    mediaIdList.clear();
                                    image_list_adapter.notifyDataSetChanged();
                                    mesgBox.setText(null);
                                    Intent intent = new Intent(ImagesMessages.this, messageActivity.class);
                                    intent.putExtra("chatID", chatID);
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Image(s) Sent", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        });

                    }
                });


            }
        }


    }



    public void setImage(String uri) {


        Glide.with(getApplicationContext()).load(Uri.parse(uri)).into(dispImg);


    }


    private void initialize_recycler_view() {

        ImageDisplayView = findViewById(R.id.displayImages);
        ImageDisplayView.setNestedScrollingEnabled(false);
        ImageDisplayView.setHasFixedSize(false);
        image_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        ImageDisplayView.setLayoutManager(image_list_layout_manager);
        image_list_adapter = new ImageListAdapter(getApplicationContext(), mediaURIlist);
        ImageDisplayView.setAdapter(image_list_adapter);

        image_list_adapter.setOnItemClickListener(new ImageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                setImage(mediaURIlist.get(position));
            }
        });


    }


}
