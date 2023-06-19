package raf.principal;


/**
 * Reactions to the events which can be asynchronously triggered by any {@link Agent}
 */
public interface AgentListener extends java.util.EventListener{

    void agentTriggersDispatchRequest(AgentEvent e);
    void agentTriggersSleepRequest(AgentEvent e);
    void agentTriggersSendsDestroyRequest(AgentEvent e);
}