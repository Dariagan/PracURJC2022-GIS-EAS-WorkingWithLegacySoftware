package raf.principal.messageHandler;

import raf.principal.RaMessage;

public class SimpleMessageHandler implements MessageHandler {

    public SimpleMessageHandler() {}

    @Override
    public void handleMessage(RaMessage message) {
        System.out.println(message.content);
    }


}