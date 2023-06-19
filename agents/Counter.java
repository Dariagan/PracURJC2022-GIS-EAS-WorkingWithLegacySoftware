package raf.agents;

import java.lang.Thread;

import raf.principal.Agent;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)
/**
 * Demonstrates the handling of time-consuming applications by
 * counting up to a given number.
 */
public class Counter extends Agent
{
    /**
     * Used to end the thread.
     */
    volatile Thread shouldLive;


    private final static int MAX_COUNT = 100_000;

    /**
     * Creates a new counter that counts up to a given number.
     */
    public Counter (String name){
        super("GoodCounter_" + name);
    }

    /**
     * The run method just counts and ends when the thread is set to null.
     * Transfered to another base it continues counting.
     */
    public void run(){
        shouldLive = Thread.currentThread();
        Thread current = Thread.currentThread();
        current.setPriority(4);
        int i = 0;
        while (shouldLive == current && i < MAX_COUNT){
            ++i;
            System.out.println("Counting " + i);
            Thread.yield();
        }
        fireDestroyRequest();
    }

    /**
     * Called by the RA base before the agent is dispatched to another base.
     * Used here to end the current thread.
     */
    public void onDispatch(){
        shouldLive = null;
    }

    /**
     * Called by the RA base when this agent has to be destroyed.
     * Used to end the current thread.
     */
    public void onDestroy(){
        shouldLive = null;
    }
}
