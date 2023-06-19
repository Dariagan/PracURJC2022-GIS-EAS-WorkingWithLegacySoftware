package raf.agents;

import java.util.Enumeration;

import raf.principal.Agent;
import raf.principal.RaAddress;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
/**
 * Utility agent that prints out a list of all servers connected to the domain.
 * Note how easy it is to extend an existing program with agents.
 * Future versions of kaariboga will probably contain special agents that
 * are automatically integrated into the menu structure.
 */
public class ServerLister extends Agent
{
    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public ServerLister(String name){
        super("ServerLister_" + name);
    }

    /**
     * Prints out the names of all servers connected to the domain.
     */
    public void run(){
        RaAddress address;
        Enumeration serversEnumeration = agency.getServers(this).elements();
        System.out.println("---------------------------------------------");
        System.out.println("Servers connected to the domain:");
        while (serversEnumeration.hasMoreElements()){
            address = (RaAddress) serversEnumeration.nextElement();
            System.out.println (address.host.toString() + ":" + address.port);
        }
        System.out.println("---------------------------------------------");
        fireDestroyRequest();
    }

}
