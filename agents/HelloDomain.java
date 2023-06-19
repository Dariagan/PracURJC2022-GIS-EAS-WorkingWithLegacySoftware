package raf.agents;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import raf.principal.Agent;
import raf.principal.RaAddress;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
/**
 * Pops up a hello Window on every server in the domain when
 * a domain server is installed.
 */
public class HelloDomain extends Agent
{
    /**
     * All the servers connected to the domain.
     */
    private final Vector<RaAddress> raAddresses = new Vector<>();

    /**
     * Points to the next destination in raAddresses.
     */
    int i;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public HelloDomain(String name){
        super("HelloDomain_" + name);
    }

    /**
     * Fills up raAddresses with all the servers connected to the domain.
     */
    public void onCreate(){
        i = 0;
        Enumeration<RaAddress> servers = agency.getServers(this).elements();
        while (servers.hasMoreElements()){
            raAddresses.addElement(servers.nextElement());
        }

    }

    /**
     * Shows a window.
     */
    public void onArrival(){
        new Popup().start();
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void run(){
        try{
            if (i < raAddresses.size()){
                destination = raAddresses.elementAt(i);
                ++i;
                System.out.println("Try to dispatch");
                fireDispatchRequest();
            }
            else fireDestroyRequest();
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.err.println ("HelloDomain: Index out of bounds!");
            fireDestroyRequest();
        }
    }

    /**
     * Use a thread to let a window pop up.
     */
    public static class Popup extends Thread implements Serializable{

        /**
         * Pop up window.
         */
        public void run(){
            JOptionPane.showMessageDialog(null, "Hello there.");
        }
    }

}