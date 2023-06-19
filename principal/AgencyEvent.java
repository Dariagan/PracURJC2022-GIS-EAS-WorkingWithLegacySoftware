package raf.principal;

/**
 The events that can be triggered by the Ra {@link Agency}.
 These events are triggered when a Ra agent is added or
 removed from an agency.
 Listener interface: {@link AgencyListener}.
 */
public class AgencyEvent extends java.util.EventObject {

     /**
      * The name of the agent that has been added or removed from the agency.
     */
     private final String name;

    /**
     Creates a new agency event.
     @param obj The object that created the event.
     @param name The name of the agent.
     */
    public AgencyEvent(Object obj, String name) {
        super(obj);
        this.name = name;
    }

    /**
     Returns the name of the agent that has been added or removed from the agency.
     */
    public String getName() {
        return name;
    }
}