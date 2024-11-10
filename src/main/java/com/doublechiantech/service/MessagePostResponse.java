package com.doublechiantech.service;

import java.util.Date;

public class MessagePostResponse {

    public static MessagePostResponse withMessage(String message){
        MessagePostResponse messagePostResponse=new MessagePostResponse();
        messagePostResponse.setMessage(message);
        return  messagePostResponse;
    }
    private String message="yes"+new Date().getTime();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    private int result=0;

    public static MessagePostResponse withErrorMessage(String message){
        MessagePostResponse messagePostResponse=new MessagePostResponse();
        messagePostResponse.setMessage(message);
        messagePostResponse.setResult(1);
        return  messagePostResponse;
    }

}
