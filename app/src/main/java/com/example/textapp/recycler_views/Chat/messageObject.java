package com.example.textapp.recycler_views.Chat;

import java.util.ArrayList;

public class messageObject {

    String message,messageId,senderId,isForwarded,isDeleted,time;
    ArrayList<String> mediaURLlist;


    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public messageObject(String message, String messageId, String senderId, String isForwarded, ArrayList<String> mediaURLlist, String isDeleted, String time) {
        this.message = message;
        this.messageId = messageId;
        this.senderId = senderId;
        this.isForwarded=isForwarded;
        this.mediaURLlist=mediaURLlist;
        this.isDeleted=isDeleted;
        this.time=time;
    }

    public ArrayList<String> getMediaURLlist() {
        return mediaURLlist;
    }

    public String getIsForwarded() {
        return isForwarded;
    }
    public String getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }
}
