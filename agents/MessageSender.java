package raf.agents;

import raf.principal.Agent;
import raf.principal.RaAddress;
import raf.principal.RaMessage;

import java.util.Enumeration;
import javax.swing.*;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
/**
 * The MessageSender agent sends a message to all agents on the base server.
 */
public class MessageSender extends Agent
{
    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public MessageSender (String name){
        super("MessageSender_" + name);
    }

    /**
     * Sends a message to all agents on this base and destroys itself.
     */
    public void run(){

        String content = JOptionPane.showInputDialog(null, "Please type in a message.");


        Enumeration<String> names = agency.getRaNames(this);
        while (names.hasMoreElements()){
            RaAddress sender = new RaAddress(getName());
            RaAddress recipient = new RaAddress(names.nextElement());
            RaMessage message = new RaMessage(sender, recipient, RaMessage.Kind.TO_AGENT, content, null);
            fireRaMessage(message);
        }

        fireDestroyRequest();
    }

}