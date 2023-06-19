package raf.agents;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;


import raf.principal.Agent;
import raf.principal.RaAddress;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
/**
 * The travel agent just opens a dialog, travels to another base and prints
 * Hello World to the screen. After that it destroys itself.
 */
public class HelloTraveler extends Agent
{
    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique.
     */
    public HelloTraveler(String name){
        super("Traveler_" + name);
    }

    /**
     * This is called when the agent is created and
     * the base has set the important fields.
     */
    public void onCreate(){
        InetAddress target = null;

        try{
            String server = JOptionPane.showInputDialog(null, "Where do you want me to go today?");
            target = InetAddress.getByName(server);
        }
        catch (UnknownHostException e){
            System.err.println("Error: Could not determine host address!");
            fireDestroyRequest();
        }
        destination = new RaAddress(target, 10101, null);
        fireDispatchRequest();
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void onArrival(){
        System.out.println("Hi! I am the traveler. ");
        fireDestroyRequest();
    }


}