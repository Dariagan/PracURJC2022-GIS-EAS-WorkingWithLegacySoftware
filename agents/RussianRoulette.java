package raf.agents;

import raf.principal.Agent;

import java.util.Random;

// MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE PONERSE A CARGAR AGENTES. CTRL+A Y CTRL+/ (DEL NUMPAD)

public class RussianRoulette extends Agent {

    public RussianRoulette(String name){
        super("RussianRoulette_" + name);
    }

    private final Random rand = new Random();

    private int numberOfDispatches = 0;
    private int losingJump;

    public void onCreate(){
        losingJump = rand.nextInt(6);
    }


    public void onDispatch(){
        numberOfDispatches++;
        System.out.println("Current shot: " + numberOfDispatches);

        if (numberOfDispatches == losingJump) {
            System.err.println("YOU LOST :( Shutting down your JVM...");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }
    }

}
