package raf.principal.messageHandler;

import raf.principal.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;

public class AgentMessageHandler implements MessageHandler {
    private final Agency agency;
    private final InetAddress address;

    public AgentMessageHandler(Agency agency, InetAddress address) {
        this.agency = agency;
        this.address = address;
    }

    @Override
    public void handleMessage(RaMessage message) {
        ByteArrayInputStream bInStream = new ByteArrayInputStream(message.binary);
        try (AgentClassInputStream mis = new AgentClassInputStream(bInStream)) {
            Agent agent = (Agent) mis.readObject();
            agency.addAgentOnArrival(agent, address);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
