package raf.agenthandler;

import raf.principal.Agency;

import javax.swing.*;
import java.io.File;


/**
 * abstract class used as an interface between AgentHandler and AgentHandlerView, it is an abstract class instead
 * of an <code>interface</code> because I don't want this to be visible outside this package
 */
abstract class AgentHandlerController {

    abstract DefaultListModel<String> getListModel();
    abstract File getDisplayedAgentsPath();
    abstract Agency getRaAgency();

    abstract void handleUserSelectsAgent(String agent);
    abstract void handleUserDispatchesAgent(String destinationAgency);
    abstract void handleUserDeletesAgent();
    abstract void handleUserSelectsAgent(int selectedIndex);
}
