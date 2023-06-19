package raf.principal.messageHandler;

import raf.principal.Agency;
import raf.principal.AgentBox;
import raf.principal.RaMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ToAgentMessageHandler implements MessageHandler {
    private final Agency agency;

    private static final Logger logger = Logger.getLogger(ToAgentMessageHandler.class.getName());

    public ToAgentMessageHandler(Agency agency) {
        this.agency = agency;
    }

    @Override
    public void handleMessage(RaMessage message) {
        if (message.recipient.name != null) {
            logger.log(Level.INFO, "Trying to return a message to the local agent: {0}", message.recipient.name);
            AgentBox box = agency.getAgentBoxes().get(message.recipient.name);

            if (box != null) {
                box.agent.handleMessage(message);
            }
        } else logger.log(Level.WARNING, "TOAGENT message received with null recipient");
    }
}