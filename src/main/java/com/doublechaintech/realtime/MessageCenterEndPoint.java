package com.doublechaintech.realtime;
import com.doublechiantech.service.ChannelService;
import com.doublechiantech.service.MessagePostRequest;
import com.doublechiantech.service.MessagePostResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.ObjectUtil;
import io.quarkus.runtime.util.StringUtil;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.common.util.DateUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/message-center/{username}")
@ApplicationScoped

public class MessageCenterEndPoint {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MessageCenterEndPoint.class);
    /*
    static MessageCenterEndPoint messageCenterEndPoint;
    public static synchronized  MessageCenterEndPoint inst(){
        if(messageCenterEndPoint==null){
            messageCenterEndPoint=new MessageCenterEndPoint();
        }
        return messageCenterEndPoint;

    }*/


    public static  String PUBLIC_CHANNEL="public";

    Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.getLogger(ChannelService.class);


    public String getSessionId(String username,Session session){
        if(PUBLIC_CHANNEL.equals(username)){
            return "public:"+session.getId();
        }
        return username;
    }
    public String getMaskedId(String username,Session session){
        if(PUBLIC_CHANNEL.equals(username)){
            return "public:"+session.getId();
        }
        if(username.length() < 4){
            return "*****";
        }
        return "*****"+username.substring(username.length() - 4);
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        String internalId=username;

        sessions.put(getSessionId(username,session), session);
        broadcast(getMaskedId(username,session)+" joined at " + DateUtil.formatDate(new Date(),"YYYY-MM-DD hh:mm:ss.SSS"));
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(getSessionId(username,session));
        broadcast("User " + getMaskedId(username,session) + " left");

    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(getSessionId(username,session));
        broadcast("User " + getMaskedId(username,session) + " left on error: " + throwable);
    }

    @OnMessage
    public void onMessage(Session session,String message, @PathParam("username") String username) {


        try{
            MessagePostRequest request=parseMessage(message);
            multicast(request.getSubscribers(),wrapWithUserName(session,username,request.getMessage()));
            sendBackMessage(session,username,MessagePostResponse.withErrorMessage("ok"));
        }catch (Exception e){
            LOG.error("error occurred: "+ e.getMessage());
            sendBackMessage(session,username,wrapExceptionAsResponse(session,username,e));
        }

        LOG.info("received a message "+message+" form session "+ session.getId()+" with user name: "+ username);
    }

    private String wrapWithUserName(Session session, String username, String message) throws JsonProcessingException {

        MessagePostRequest request=MessagePostRequest.withSourceAndMessage(username,message);

        return getDefaultMapper().writeValueAsString(request);
    }

    private MessagePostResponse wrapExceptionAsResponse(Session session, String username, Exception e) {


        return MessagePostResponse.withErrorMessage(e.getMessage());


    }

    private void sendBackMessage(Session session, String username,MessagePostResponse response){
        try {
            String message=getDefaultMapper().writeValueAsString(response);
            session.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    LOG.error("Unable to send message to : " + session.getId()+"/"+username+" with content: "+result.getException());
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private MessagePostRequest parseMessage(String message) throws JsonProcessingException {
        MessagePostRequest request=getDefaultMapper().readValue(message,MessagePostRequest.class);
        if(StringUtil.isNullOrEmpty(request.getMessageSource())){
            throw new IllegalArgumentException("messageSource is not allowed to be null");
        }
        if(StringUtil.isNullOrEmpty(request.getMessage())){
            throw new IllegalArgumentException("message is not allowed to be null");
        }
        if(StringUtil.isNullOrEmpty(request.getChannelName())){
            throw new IllegalArgumentException("channelName is not allowed to be null");
        }
        if(request.getSubscribers() ==null){
            throw new IllegalArgumentException("subscribers is not allowed to be null");
        }
        if(request.getSubscribers().isEmpty()){
            throw new IllegalArgumentException("subscribers is not allowed to be empty");
        }

        return request;
    }

    private ObjectMapper getDefaultMapper(){
        return new ObjectMapper();
    }

    public synchronized void  multicast(List<String> endPoints, Object message){


        sessions.entrySet().stream()
                .filter(entry->endPoints.contains(entry.getKey()))
                .map(entry -> {
                    LOG.info("sending message to "+ entry.getKey());
                    return entry;
                })
                .map(entry -> entry.getValue()).forEach(session -> {
                    if(!session.isOpen()){
                        return;//not clean here, fine for now
                    }
                    session.getAsyncRemote().sendText(message.toString(), result ->  {
                        if (result.getException() != null) {
                            LOG.error("Unable to send message: " + result.getException());
                        }
                    });
                });
    }

    public void broadcast(String message) {
        sessions.values().forEach(s -> {

            s.getAsyncRemote().sendText(message, result ->  {
                if (result.getException() != null) {
                    LOG.error("Unable to send message: " + result.getException());
                }
            });
        });
    }
}
/*

{"channelName":"123455677","message":"this is a test message","subscribers":["123"]}
{"channelName":"123455677","messageSource":"123","message":"this is a test message","subscribers":["456"]}



**/