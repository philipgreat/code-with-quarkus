package com.doublechiantech.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import com.doublechaintech.realtime.MessageCenterEndPoint;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("/message-center/connections")
public class ConnectionService {
    @Inject
    MessageCenterEndPoint messageCenterEndPoint;

    @GET
    public List<SessionStatus> info(){

        List<SessionStatus> statusList=new ArrayList();
        messageCenterEndPoint.getSessions().entrySet().forEach(stringSessionEntry -> {
            statusList.add(createFromSession(stringSessionEntry.getKey(),stringSessionEntry.getValue()));
        });

        return statusList;

    }

    private SessionStatus createFromSession(String key, Session session) {

        SessionStatus status=new SessionStatus();
        status.setSessionKey(key);
        status.setOpen(session.isOpen());
        return status;


    }
}
