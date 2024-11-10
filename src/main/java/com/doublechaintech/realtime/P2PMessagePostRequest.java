package com.doublechaintech.realtime;

import com.doublechiantech.service.MessagePostRequest;

import java.util.List;

public class P2PMessagePostRequest extends MessagePostRequest {


    @Override
    public List<String> getSubscribers() {
        return  null;
    }
}
