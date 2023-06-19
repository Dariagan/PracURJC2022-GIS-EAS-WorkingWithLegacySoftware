package raf.agents;

import raf.principal.Agent;

import java.lang.Thread;


/**
 * The Reproducer agent is able to load other agents.
 */
public class Reproducer extends Agent
{
    /**
     * Number of childs the agent will create.
     */
    private int MAX_CHILDREN = 3;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public Reproducer(String name){
        super("Reproducer_" + name);
    }

    /**
     * After an initial sleep period (grow up) the agent gives birth to new children.
     */
    public void run(){
        try{
            System.out.println ("Hurray, I am born.");
            Thread.sleep(500);
            for (int i = 0; i < MAX_CHILDREN; i++){
                Reproducer agent = new Reproducer(agency.generateAgentName());
                agent.MAX_CHILDREN = MAX_CHILDREN - 1;
                agency.addAgentOnCreation(agent, null);
            }
        }
        catch(java.lang.InterruptedException ignored){}

        System.out.println ("Agent dies after " + MAX_CHILDREN + " children.");
        fireDestroyRequest();
    }
}