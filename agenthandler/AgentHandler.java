
package raf.agenthandler;

import java.awt.event.*;
import java.io.File;
import java.lang.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import raf.config.ConfigLoader;
import raf.principal.*;


/**
 * RAF environment launcher with GUI
 */
public class AgentHandler extends AgentHandlerController implements AgencyListener{

    private static final Logger logger = Logger.getLogger(AgentHandler.class.getName());
    private final ConfigLoader config = ConfigLoader.getInstance();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    @Override
    DefaultListModel<String> getListModel() {
        return listModel;
    }
    @Override
    File getDisplayedAgentsPath(){
        return config.agency.displayedAgentsPath();
    }
    @Override
    Agency getRaAgency() {
        return agency;
    }

    /**
     * Name of the selected RA agent within listModel
     */
    private String selectedRaAgent = null;

	/**
	 * Local agency (handles agents)
	 */
	private final Agency agency;

    /**
     * RaAddress of the domain server the agency is connecting to
     */
    private RaAddress raDomainServer;

    /**
     * Instantiates AgentHandler
     */
    public static void main(String[] args){
        try {UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}
        catch (Exception e) {
            e.printStackTrace();
        }
        new AgentHandler();
    }

    /**
     * Sets up the desktop application and starts the agency
     */
    public AgentHandler(){

        JFrame view = new AgentHandlerView(this);
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                System.exit(0);
            }
        };
        view.addWindowListener(l);
        view.pack();
        view.setVisible(true);

        try{
            if (config.domainServer.ip() == null)
                raDomainServer = null;
            else{
                InetAddress server = InetAddress.getByName(config.domainServer.ip());
                raDomainServer = new RaAddress(server, config.domainServer.port(), null);
            }
        }
        catch (UnknownHostException e){
            logger.log(Level.WARNING, "Ra domain server address not valid");
            e.printStackTrace();
            raDomainServer = null;
        }

        agency = new Agency(this);
        agency.addAgencyListener(this);

        startAgency();
    }

    @Override
    void handleUserSelectsAgent(String agent) {
        if (!agent.isBlank())
            loadRaAgent(agent);
    }
    @Override
    void handleUserDispatchesAgent(String destinationAgency) {
        if(!destinationAgency.isBlank())
            sendRaAgentTo(destinationAgency);
    }

    @Override
    void handleUserDeletesAgent() {
        agency.destroyAgent(this, selectedRaAgent);
    }

    /**
     * loads a RaAgent
     */
    public void loadRaAgent(String agentName){

        try{
            ClassManager classManager = CacheClassManager.getInstance();
            Class<?> loadedAgentClass = classManager.findClass(agentName);


            Constructor<?>[] cons = loadedAgentClass.getConstructors();
            Object[] obs = {this.agency.generateAgentName()};
	        Agent agent = (Agent) cons[0].newInstance(obs);
            this.agency.addAgentOnCreation(agent, InetAddress.getLocalHost());
        }
        catch (InvocationTargetException | SecurityException | InstantiationException
               | IllegalAccessException e){
            logger.log(Level.WARNING, "Couldn't load class {0}", agentName);
            e.printStackTrace();
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    /**
     * Sends currently selected agent (selectedRaAgent) to another agency
     * @param agency destination agency
     */
    void sendRaAgentTo(String agency){
        InetAddress destination;

        String servername ;
        String strLoPort;
        int loPort = config.agency.port();

        try{
            // split server address into servername and port and determine host address
            int separator = agency.indexOf("/") + 1;
            int portDelimiter = agency.indexOf (':');
            if (portDelimiter != -1) {
                servername = agency.substring (separator, portDelimiter);
                strLoPort  = agency.substring (portDelimiter + 1);
                int secondDelimiter = strLoPort.indexOf (':');
                strLoPort = strLoPort.substring(0, secondDelimiter);
                loPort = Integer.parseInt (strLoPort);
            }   
            else{
                servername = agency;
            }

            destination = InetAddress.getByName(servername);
            this.agency.dispatchAgent(this, selectedRaAgent, new RaAddress (destination, loPort, null));
        }
        catch (IndexOutOfBoundsException | NumberFormatException e){
            logger.log(Level.WARNING, "Incorrect port format");
            e.printStackTrace();
        } catch (UnknownHostException e){
            logger.log(Level.WARNING, "Can't determine local host address");
            e.printStackTrace();
        }
    }

    /**
     * Stats agency thread
     */
    void startAgency(){
        logger.log(Level.INFO, "Initializing agency");
        agency.startAgency (this, config.agency.port(), raDomainServer);
    }
    /**
     * Stops agency thread
     */
    void stopAgency(){
        logger.log(Level.INFO, "Stopping agency");
        agency.stopAgency (this);
    }

    /**
     * Reaction to RA agent being created
     */
    public void agencyRaCreated (AgencyEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaction to RA agent arriving in this agency
     */
    public void agencyRaArrived (AgencyEvent e){
        logger.log(Level.INFO, "Agent {0} arrived", e.getName());
        listModel.addElement(e.getName());
    }

    /**
     * Reaction to RA agent leaving agency
     */
    public void agencyRaLeft (AgencyEvent e){
        logger.log(Level.INFO, "Agent {0} left, removing from agency", e.getName());
        try {
            listModel.removeElement(e.getName());
        }
        catch (Exception ignored){}
    }

    /**
     * Reaction to RA agent destroyed
     */
    public void agencyRaDestroyed (AgencyEvent e){
        logger.log(Level.INFO, "Agent {0} destroyed", e.getName());
        try {
            listModel.removeElement(e.getName());
        }
        catch (Exception ignored){}
    }

    /**
     * Reaction to user clicking on a listed (already loaded) agent
     */
    @Override
    void handleUserSelectsAgent(int selectedIndex) {
        if (selectedIndex >= listModel.size() || selectedIndex < 0) {
            selectedRaAgent = null;
            return;
        }
        selectedRaAgent = listModel.elementAt(selectedIndex);
    }

    /**
     *  Stops the agency and its connections
     */
    public void dispose() {
        stopAgency();
    }
}

