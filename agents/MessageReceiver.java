package raf.agents;

import raf.principal.Agent;
import raf.principal.RaMessage;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
/**
 * The MessageReceiver agent waits for a message and then prints it out.
 */
public class MessageReceiver extends Agent
{
    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public MessageReceiver (String name){
        super("MessageReceiver_" + name);
    }

    /**
     * Prints kind and content of the received message to the screen.
     *
     * @param msg The message that this agent receives.
     */
    public void handleMessage (RaMessage msg){
        System.out.println ("MessageReceiver: Received Message.");
        System.out.println ("           Kind: " + msg.kind);
        System.out.println ("        Content: " + msg.content);
    }

}