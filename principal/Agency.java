package raf.principal;

import raf.principal.messageHandler.MessageHandler;
import raf.principal.messageHandler.MessageHandlerFactory;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Agency
    implements Serializable, AgentListener, RaMessageListener
{
    public static final Logger logger = Logger.getLogger(Agency.class.getName());
    private final Object parent;
    private Hashtable<String, RaAddress> agencies;
    private RaAddress raServer;
    private final Vector<AgencyListener> agencyListeners = new Vector<>();
    private int port;
    private RaAddress agencyAddress;
    private int generatedAgentsCount = 0;
    private volatile Thread listenThread = null;
    private final ClassManager classManager = CacheClassManager.getInstance();
    private final Hashtable<String, AgentBox> agentBoxes = new Hashtable<>();
    
    private ServerSocket serverSocket = null;

    public Agency(Object parent){
        this.parent = parent;
    }

    public Hashtable<String, AgentBox> getAgentBoxes() {
        return agentBoxes;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    class ReceiveMessageThread extends Thread implements Serializable{
        private final Socket socket;
        private final Agency agency;
        private InetAddress address;


        public ReceiveMessageThread(Agency agency, Socket socket) {
            this.socket = socket;
            this.agency = agency;
        }


        public void run() {
            try (ObjectInputStream inStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                 ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream())) {

                address = socket.getInetAddress();

                RaMessage message = (RaMessage) inStream.readObject();

                MessageHandlerFactory factory = new MessageHandlerFactory(agency, address, outStream);

                MessageHandler MessageHandler = factory.createMessageHandler(message.kind);
                logger.log(Level.INFO, "RaMessage received: {0}", message.kind.name());
                MessageHandler.handleMessage(message);

            } catch (IOException e) {
                System.err.println("ReceiveMessageThread: IOException on data transfer");
                e.printStackTrace();
                System.err.println(e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("ReceiveMessageThread: ClassNotFoundException when receiving object");
                e.printStackTrace();
                System.err.println(e.getMessage());
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    System.err.println("ReceiveMessageThread: IOException at cleanup!");
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    class ListenThread extends Thread implements Serializable
    {
        private final Agency parent;

        public ListenThread (Agency parent){
            this.parent = parent;
        }

        public void run(){
            Thread shouldLive = listenThread;
            try{
                while (shouldLive == listenThread){
                    Socket socket = serverSocket.accept();
                    if(shouldLive != listenThread) return;
                    logger.log(Level.INFO, "Socket is receiving a message");
                    new ReceiveMessageThread(parent, socket).start();
                    Thread.yield();
                }
            }
            catch (IOException e){
                System.err.println("! ListenThread.run: " + e);
                e.printStackTrace();
            }
        }
    }

    protected static class SendMessageThread extends Thread implements Serializable{
        RaMessage msg;

        
        public SendMessageThread(RaMessage msg){
            this.msg = msg;
        }

       
        public void run(){
            Socket socket = null;
            ObjectOutputStream outStream = null;

            try {
                socket = new Socket(msg.recipient.host, msg.recipient.port);
                logger.log(Level.INFO, String.format("Socket created at: %s %d", msg.recipient.host, msg.recipient.port));
                outStream = new ObjectOutputStream(
                                    socket.getOutputStream());
                outStream.writeObject (msg);
                outStream.flush();
                logger.log(Level.INFO, "Wrote message to socket");
            }
            catch (IOException e){
                logger.log(Level.WARNING, String.format(e + ": %s", msg.recipient));
            }

            try{
                try { sleep(10000); } catch(Exception ignored) {}
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){
                System.err.println("! SendMessageThread.run,2: ");
                e.printStackTrace();
            }
        }
    }

   
    public void logMessage(RaMessage msg){
        logger.log(Level.INFO, "Message version: {0}\nMessage kind: {1}\nMessage content: {2}",
                new Object[]{msg.version, msg.kind, msg.content});
    }

  
    protected void dispatch (Agent agent, RaAddress address){

        RaMessage msg;
        agent.onDispatch();
        AgentBox box = agentBoxes.get(agent.getName());
        if (box.thread.isAlive()) box.thread = null;

        try {
            RaAddress msgSender = new RaAddress(
                                         InetAddress.getLocalHost(),
                                         port, agent.getName());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            RaOutputStream mos = new RaOutputStream(bos);
            mos.writeObject (agent);

            msg = new RaMessage(msgSender, address, RaMessage.Kind.AGENT, "", bos.toByteArray());
            new SendMessageThread(msg).start();
        } catch (IOException e){
            System.err.println ("! Agency.dispatchRequest: " + e);
        }

        agent.onDestroy();
        fireAgentLeft(agent.getName());
        agentBoxes.remove (agent.getName());
        classManager.removeOne(agent.getName());
    }


    public void dispose(){
        if (listenThread != null) stopAgency(parent);
    }

    public synchronized void addAgentOnArrival(Agent agent, InetAddress sender){
        agent.setAgency(this);
        agent.addRaListener(this);
        agent.addRaMessageListener(this);
        Thread thread = new Thread (agent);
        java.util.Date time = new java.util.Date();
        AgentBox box = new AgentBox(agent, thread, time, sender);
        agentBoxes.put(agent.getName(), box);
        agent.onArrival();
        thread.start();
        fireAgentArrived(agent.getName());
    }

    
    public synchronized void addAgentOnCreation(Agent agent, InetAddress sender){
        agent.setAgency(this);
        agent.addRaListener(this);
        agent.addRaMessageListener(this);
        Thread thread = new Thread (agent);
        java.util.Date time = new java.util.Date();
        AgentBox box = new AgentBox(agent, thread, time, sender);
        agentBoxes.put(agent.getName(), box);
        agent.onCreate();
        thread.start();
        fireAgentCreated(agent.getName());
    }

    public void agentTriggersDispatchRequest(AgentEvent e){
        Agent agent = (Agent) e.getSource();
        RaAddress destination = agent.getDestination();
        logger.log(Level.INFO, "Destination: {0}", destination.host.toString());
        dispatch (agent, destination);
    }

    public void agentTriggersSleepRequest(AgentEvent e){
        Agent agent = (Agent) e.getSource();
        agent.onSleep();
    }
  
    public void agentTriggersSendsDestroyRequest(AgentEvent e){
        Agent agent = (Agent) e.getSource();
        destroyAgent(this, agent.getName());
    }

    public Enumeration<String> getRaNames(Object sender){
        return agentBoxes.keys();
    }

    public String generateAgentName(){
        String localHost;
        try{
            localHost = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e){
            logger.log(Level.WARNING, "Couldn't determine localhost");
            localHost = "Unknown host";
        }
        return ++generatedAgentsCount + " " + localHost + " " + new Date();
    }

    
    public RaAddress getAgencyAddress(){
        try{
            agencyAddress.host = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e){
            System.err.println ("! Agency.getAgencyAddress :" + e );
            e.printStackTrace();
            System.exit(1);
        }
        return agencyAddress;
    }

    

    public void startAgency(Object sender, int portNo, RaAddress raServer){
        if (sender != parent) return;

        port = portNo;
        this.raServer = raServer;
        try{
             agencyAddress = new RaAddress(InetAddress.getLocalHost(), port, null);

             serverSocket = new ServerSocket(port);
             logger.log(Level.INFO, "Listening to port: " + port);
        }
        catch (UnknownHostException e){
            System.err.println ("Couldn't determine local host address");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e){
            System.err.println ("Couldn't create server socket" + e);
            e.printStackTrace();
            System.exit(1);
        }
        listenThread = new ListenThread(this);
        listenThread.start();

        if (raServer != null){
            RaMessage msg = new RaMessage (agencyAddress,
                                                         raServer,
                                                         RaMessage.Kind.AGENCY_ONLINE,
                                                         null,
                                                         null);
            new SendMessageThread(msg).start();
        }
    }

    public void stopAgency (Object sender){
        if (sender != parent) return;
        if (raServer != null){
            RaMessage msg = new RaMessage (agencyAddress,
                    raServer, RaMessage.Kind.AGENCY_OFFLINE, null, null);
            try {
                Thread thread = new SendMessageThread(msg);
                thread.start();
                thread.join();
            }
            catch (InterruptedException e){
                System.err.println ("! Agency: La desconexión del raServer ha fallado!" + e);
            }
        }

        listenThread = null;
        try{
            serverSocket.close();
            serverSocket = null;
            logger.log(Level.INFO, "Closed server socket");
        }
        catch (IOException e){
            logger.log(Level.WARNING, "Couldn't close server socket");
	        System.exit(1);
        }
    }

    public void destroyAgent(Object sender, String name){
        AgentBox box = agentBoxes.get(name);
        if (box != null){
            box.agent.onDestroy();
            fireAgentDestroyed(box.agent.getName());
            agentBoxes.remove(name);
        }
    }

    public void dispatchAgent(Object sender, String name, RaAddress destination){
        AgentBox box = agentBoxes.get(name);
        if (box != null){
            logger.log(Level.INFO, "Destination: {0}", destination.host.toString());
            dispatch (box.agent, destination);
        }
    }

    public Hashtable<String, RaAddress> getServers (Object sender){
        Hashtable<String, RaAddress> result = null;
        synchronized (this){
            if (agencies != null) result = getAgencies();
        }
        return result;
    }


    public synchronized void addAgencyListener (AgencyListener l){
        if (agencyListeners.contains(l)) return;

        agencyListeners.addElement (l);
    }


    public synchronized void removeAgencyListener (AgencyListener l){
        agencyListeners.removeElement (l);
    }

   
    protected void fireAgentCreated(String name){
        Vector<AgencyListener> listeners;
        synchronized (this){
            listeners = new Vector<>(agencyListeners);
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            agencyListeners.elementAt(i).agencyRaCreated(e);
        }
    }

    protected void fireAgentArrived(String name){
        Vector<AgencyListener> listeners;
        synchronized (this){
            listeners = new Vector<>(agencyListeners);
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            agencyListeners.elementAt(i).agencyRaArrived(e);
        }
    }

    protected void fireAgentDestroyed(String name){
        Vector<AgencyListener> listeners;
        synchronized (this){
            listeners = new Vector<>(agencyListeners);
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            agencyListeners.elementAt(i).agencyRaDestroyed(e);
        }
    }

    protected void fireAgentLeft(String name){
        Vector<AgencyListener> listeners;
        synchronized (this){
            listeners = new Vector<>(agencyListeners);
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            agencyListeners.elementAt(i).agencyRaLeft(e);
        }
    }

    public void raMessageArrived(RaMessageEvent e){
        RaMessage message = e.getMessage();
        Agent receiver;

        if (message != null && message.recipient != null) {
            if (message.recipient.host == null) {
                AgentBox box = null;
                if (message.recipient.name != null)
                    box = agentBoxes.get (message.recipient.name);
                if (box != null) {
                    receiver = box.agent;
                    if (receiver != null)
                        receiver.handleMessage (message);
                }
                else System.err.println("Not a local agent: " + message.recipient.name);
            }
            else {
                new SendMessageThread(message).start();
            }
        }
    }

    public Hashtable<String, RaAddress> getAgencies() {
        return agencies;
    }

    public void setAgencies(Hashtable<String, RaAddress> agencies) {
        this.agencies = agencies;
    }

}
