package raf.principal;

import java.io.Serializable;

/**
 Base agent class for RA mobile agents. Agents can travel from host to host, where they run on their own instantiated thread.
 */
public class Agent implements Runnable, Serializable
{

    /**
     Launches agent events to the AgentListener.
     This thread is needed to launch events asynchronously.
     Events are asynchronous because the result of an event
     can be the destruction of an agent. Also, some reactions to those
     events may consume time.
     */
    class FireEventThread extends Thread implements Serializable{

        /**
         The event to be launched.
         */
        private final AgentEvent event;

        /**
         @param e The event to be launched.
         */
        public FireEventThread (AgentEvent e){
            event = e;
        }

        /**
         Checks the IDs of the method calls to the appropriate
         event listener.
         */
        public void run(){
            switch (event.getRequestType()) {
                case DISPATCH -> agentListener.agentTriggersDispatchRequest(event);
                case DESTROY -> agentListener.agentTriggersSendsDestroyRequest(event);
                case SLEEP -> agentListener.agentTriggersSleepRequest(event);
                default -> throw new RuntimeException("Not implemented");
            }
        }
    }

    /**
     Launches RaMessage to the RaMessageListener.
     This thread is needed to launch messages asynchronously.
     Messages must be launched asynchronously because a result of the message launch
     could be to continue communication between
     objects.
     */
    class FireMessageThread extends Thread implements Serializable{

        /**
         The event to be launched.
         */
        private final RaMessageEvent e;

        /**
         @param e The message to be launched.
         */
        public FireMessageThread (RaMessageEvent e){
            this.e = e;
        }

        /**
         Calls the RaMessage(m) method of the RaMessageListener.
         */
        public void run(){
            messageListener.raMessageArrived(e);
        }
    }

    /**
     The agency is the Agency that hosts the agent. An agent
     can only live in one agency.
     */
    protected transient Agency agency = null;

    /**
     This is the name of the agent.
     */
    private String name;
    public void setName(String name) {this.name = name;}

    /**
     This is the destination the agent wants to be transferred to.
     It is read by the agency through the getDestination() method.
     */
    protected RaAddress destination;

    /**
     The event listener that receives all RaEvents. It is usually set
     by the agency upon arrival.
     */
    protected transient AgentListener agentListener;

    /**
     The message listener that receives all RaMessages. It is usually set by the agency
     upon arrival.
     */
    protected transient RaMessageListener messageListener;

    /**
     Constructs a new agent with its name.
     The name must be unique, as it is used to
     manage agents on the server.
     @param name The name of the agent. The Agency class provides a
     method that generates a unique name.
     */
    public Agent(String name){
        this.name = name;
    }

    /**
     Run is the main method of the agent thread. It is called by the agency
     if it receives or creates a new agent object.
     */
    public void run(){}

    /**
     Sets the host agency.<br>
     It is called by the agency if a new agent is created or received.
     An agent can only exist in one agency.
     @param hostAgency The Agency that hosts the agent.
     */
    public final void setAgency(Agency hostAgency){
        agency = hostAgency;
    }

    /**
     Returns the name of this agent.
     */
    public String getName(){
        return name;
    }

    /**
     This function is called on the first creation of the agent.
     At this point, the agent is initialized.
     */
    public void onCreate(){}

    /**
     This is called before the agent is removed by the agency.
     */
    public void onDestroy(){}

    /**
     This is called when the agent is dispatched to another agency
     */
    public void onDispatch(){}

    /**
     This is called when the agent arrives at a new agency.
     */
    public void onArrival(){}

    /**
     This is called when the agent is set to sleep
     */
    public void onSleep(){}
    public void onAwake(){}
   
    public void handleMessage(RaMessage msg){}

    
    protected void fireDispatchRequest(){
        AgentEvent event = new AgentEvent(this, AgentEvent.Request.DISPATCH);
        new FireEventThread(event).start();
    }
    protected void fireDestroyRequest(){
        AgentEvent event = new AgentEvent(this, AgentEvent.Request.DESTROY);
        new FireEventThread(event).start();
    }
    protected void fireSleepRequest(){
        AgentEvent event = new AgentEvent(this, AgentEvent.Request.SLEEP);
        new FireEventThread(event).start();
    }
    protected void fireRaMessage(RaMessage m){
        RaMessageEvent event = new RaMessageEvent(this, m);
        new FireMessageThread(event).start();
    }

    public RaAddress getDestination (){
        return destination;
    }

    public void addRaListener(AgentListener l){
        agentListener = l;
    }
   
    public void addRaMessageListener(RaMessageListener l){
        messageListener = l;
    }
    
    public String toString(){
        return name;
    }

}