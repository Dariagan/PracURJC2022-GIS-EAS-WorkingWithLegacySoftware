package raf.principal;

/**
 * Agent request-events fired by {@link Agent} and to be reacted to by {@link Agency}.<br>
 * Listener interface: {@link AgentListener}.
 */
public class AgentEvent extends java.util.EventObject
{
    private final Request request;

    public enum Request {
        DISPATCH, SLEEP, DESTROY
    }

    public AgentEvent(Object obj, Request request){
        super(obj); 
        this.request = request;
    }

    public Request getRequestType() {
        return request;
    }
}