package raf.principal;

import java.net.InetAddress;
import java.util.Date;


/**
 * The {@link AgentBox} class represents a container for storing information related to a mobile {@link Agent}.
 * It encapsulates the mobile agent, its execution thread, the timestamp of its arrival, and the sending host's network address.
 */
public class AgentBox {
    public final Agent agent;
    public Thread thread;
    public final Date timeOfArrival;
    public final InetAddress sendingHost;

    /**
     * Constructs a new AgentBox object.
     *
     * @param agent The RaMobileAgent associated with this RaBox.
     * @param thread The execution thread associated with the mobile agent.
     * @param timeOfArrival The timestamp indicating the arrival of the mobile agent.
     * @param sendingHost The network address of the host from which the mobile agent was sent.
     */
    public AgentBox(Agent agent, Thread thread, Date timeOfArrival, InetAddress sendingHost) {
        this.agent = agent;
        this.thread = thread;
        this.timeOfArrival = timeOfArrival;
        this.sendingHost = sendingHost;
    }
}