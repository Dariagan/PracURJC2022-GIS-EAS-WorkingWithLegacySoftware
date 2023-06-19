package raf.principal;

/**
 * Message event to be reacted to by {@link Agency}.<br>
 * Contains a {@link RaMessage}.<br>
 * Listener interface: {@link RaMessageListener}.
 */
public class RaMessageEvent extends java.util.EventObject
{
    private final RaMessage m;

    public RaMessageEvent(Object obj, RaMessage m){
        super(obj);
        this.m = m;
    }
    public RaMessage getMessage(){return m;}
}