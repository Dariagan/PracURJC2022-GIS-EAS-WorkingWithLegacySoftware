package raf.principal;


/**
 * Listener interface to events occurring in the agency
 */
public interface AgencyListener extends java.util.EventListener{

    /**
     * Reaction when an agent is created at the local {@link Agency},
     * which calls {@link Agency#fireAgentCreated(String)}, triggering this event.
     */
    void agencyRaCreated (AgencyEvent e);

    /**
     * Reaction when an agent arrives at the local {@link Agency},
     * which calls {@link Agency#fireAgentArrived(String)}, triggering this event.
     */
    void agencyRaArrived (AgencyEvent e);

    /**
     * Reaction when an agent leaves the local {@link Agency},
     * which calls {@link Agency#fireAgentLeft(String)}, triggering this event.
     */
    void agencyRaLeft (AgencyEvent e);

    /**
     * Reaction when an agent is destroyed at the local {@link Agency},
     * which calls {@link Agency#fireAgentDestroyed(String)}, triggering this event.
     */
    void agencyRaDestroyed (AgencyEvent e);

}