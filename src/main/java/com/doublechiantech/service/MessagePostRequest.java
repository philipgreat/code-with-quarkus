package com.doublechiantech.service;

import java.util.ArrayList;
import java.util.List;

public class MessagePostRequest {
    private String channelName;
    private String message;

    public static MessagePostRequest withSourceAndMessage(String username, String message) {

        MessagePostRequest req=new MessagePostRequest();
        req.setMessageSource(username);
        req.setChannelName("p2p");
        req.setMessage(message);
        return req;

    }

    public String getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(String messageSource) {
        this.messageSource = messageSource;
    }

    private String messageSource;


    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getSubscribers() {
        if(subscribers==null){
            subscribers = new ArrayList<>();
        }
        return subscribers;
    }

    public void setSubscribers(List<String> subscribers) {
        this.subscribers = subscribers;
    }

    List<String> subscribers;
}
