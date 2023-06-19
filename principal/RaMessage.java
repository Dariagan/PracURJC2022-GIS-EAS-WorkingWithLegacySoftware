package raf.principal;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class to encapsulate a message which can be sent between agencies, domain server to agencies, agents to agencies, agencies to agents, etc.
 */
public class RaMessage implements Serializable{

    public final int version;
    public final RaAddress recipient;
    public final RaAddress sender;
    public final Kind kind;
    public final String content;
    public final byte[] binary;

    public enum Kind {
        AGENCY_ONLINE, AGENCY_OFFLINE, AGENCIES_LIST, AGENT, GIVE_ME_AGENT, AGENT_NOT_FOUND, TO_AGENT, SIMPLE_MESSAGE
    }
   
    public RaMessage(RaAddress sender,
                     RaAddress recipient,
                     Kind kind,
                     String content,
                     byte[] binary){
        this.version = 0;
        this.sender = sender;
        this.recipient = recipient;
        this.kind = kind;
        this.content = content;
        this.binary = binary;
    }

    public String toString() {
        final String MESSAGE_CONTENT;
        MESSAGE_CONTENT = Objects.requireNonNullElse(content, "[binary]");

        return String.format("RaMessage\nFrom: %s\nTo: %s\nKind: %s\nContent: %s", sender, recipient, kind, MESSAGE_CONTENT);
    }
}

