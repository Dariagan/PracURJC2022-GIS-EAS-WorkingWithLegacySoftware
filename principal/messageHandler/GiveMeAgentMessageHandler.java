package raf.principal.messageHandler;

import raf.principal.Agency;
import raf.principal.AgentBox;
import raf.principal.RaMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

public class GiveMeAgentMessageHandler implements MessageHandler {
    private final Agency agency;
    private final InetAddress address;
    private final ObjectOutputStream outStream;

    public GiveMeAgentMessageHandler(Agency agency, InetAddress address, ObjectOutputStream outStream) {
        this.agency = agency;
        this.address = address;
        this.outStream = outStream;
    }

    @Override
    public void handleMessage(RaMessage message) {
        AgentBox target = agency.getAgentBoxes().get(message.content);

        if (target != null) {
            target.agent.onDispatch();
            target.thread = null;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(target.agent);

                RaMessage outMessage = new RaMessage(agency.getAgencyAddress(), message.sender, RaMessage.Kind.AGENT,
                        message.content, bos.toByteArray());

                outStream.writeObject(outMessage);
                outStream.flush();

                agency.getAgentBoxes().remove(message.content);
                agency.getClassManager().removeOne(message.content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            RaMessage outMessage = new RaMessage(agency.getAgencyAddress(), message.sender, RaMessage.Kind.AGENT_NOT_FOUND,
                    "Agent not found", null);

            try {
                outStream.writeObject(outMessage);
                outStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}