package com.example.zippy.helper;

import java.util.Date;

public class Messages {
    private String messageText;
    private String messageSender;
    private String messageReceiver;
    private long messageTime;

    public Messages(String messageText, String messageSender, String messageReceiver) {
        this.messageText = messageText;
        this.messageSender = messageSender;
        this.messageReceiver = messageReceiver;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public Messages(){

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
    public String getMessageReceiver() {
             return messageReceiver;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }
    public void setMessageReceiver(String messageReceiver) {
        this.messageSender = messageReceiver;
    }


    public long getMessageTime() {
        return messageTime;
    }
    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}

