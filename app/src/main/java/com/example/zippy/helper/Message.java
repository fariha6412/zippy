package com.example.zippy.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private String messageText;
    private String messageSender;
    //private String messageReceiver;
    private String messageTime;

    /*public Message(String messageText, String messageSender, String messageReceiver) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        this.messageReceiver = messageReceiver;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

     */



    public Message(String messageText) {
        this.messageText = messageText;
        messageTime = getCurrentTime();
    }

    public Message(String messageSender, String messageText, String messageTime) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        this.messageTime = messageTime;
    }

    public Message(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageSender() {
        return messageSender;
    }
    /*public String getMessageReceiver() {
             return messageReceiver;
    }*/

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    private String getCurrentTime(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}

