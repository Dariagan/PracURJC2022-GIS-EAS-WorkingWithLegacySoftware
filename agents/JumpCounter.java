package raf.agents;

import raf.principal.Agent;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
public class JumpCounter extends Agent
{

    public JumpCounter(String name){
        super("JumpCounter_" + name);
    }

    private int numberOfAgenciesJumped = 0;

    /**
     * Prints out the names of all servers connected to the domain.
     */
    public void run(){
        System.out.println("jumps: "+ numberOfAgenciesJumped);
    }

    @Override
    public void onArrival() {
        numberOfAgenciesJumped++;
    }
}
