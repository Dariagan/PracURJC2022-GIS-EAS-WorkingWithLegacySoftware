package raf.principal.messageHandler;
import raf.principal.Agency;
import raf.principal.RaMessage;

import java.io.ObjectOutputStream;
import java.net.InetAddress;

/**
 * Factory class for creating message handlers based on the message kind.
 */

public class MessageHandlerFactory {
    private final Agency agency;
    private final InetAddress address;
    private final ObjectOutputStream outStream;

    /**
     * Constructs a MessageHandlerFactory.
     *
     * @param agency     The agency object.
     * @param address    The IP address.
     * @param outStream  The ObjectOutputStream.
     */

    public MessageHandlerFactory(Agency agency, InetAddress address, ObjectOutputStream outStream) {
        this.agency = agency;
        this.address = address;
        this.outStream = outStream;
    }

    /**
     * Creates a message handler based on the message kind.
     *
     * @param kind  The kind of message.
     * @return      The created MessageHandler instance.
     */

    public MessageHandler createMessageHandler(RaMessage.Kind kind) {
        return switch (kind) {
            case AGENCIES_LIST ->  new AgenciesListMessageHandler(agency);
            case AGENT -> new AgentMessageHandler(agency, address);
            case GIVE_ME_AGENT -> new GiveMeAgentMessageHandler(agency, address, outStream);
            case TO_AGENT -> new ToAgentMessageHandler(agency);
            case SIMPLE_MESSAGE -> new SimpleMessageHandler();
            default -> throw new RuntimeException("message kind not implemented");
        };
    }
}