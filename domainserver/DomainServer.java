package raf.domainserver;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import raf.config.ConfigLoader;
import raf.principal.*;

/**
 * Una clase que construye dominios administrativos.
 * Cada agencia se debe registrar aqui con un mensaje AGENCY_ONLINE.
 * Si una agencia deja de estar en linea debe enviar un mensaje AGENCY_OFFLINE
 * Si ocurre algun cambio en el dominio se les notifica a las agencias con un mensaje AGENCYS.
 */
public class DomainServer
{
    private static final Logger logger = Logger.getLogger(DomainServer.class.getName());
    private static final ConfigLoader CONFIG_LOADER = ConfigLoader.getInstance();


    public static void main (String[] args){
        new DomainServer().startService(CONFIG_LOADER.domainServer.port());
    }

    /**
     * Gestiona los mensajes que vienen a trav�s de una conexi�n socket.
     * Los Mensajes pueden ser AGENCY_ONLINE o AGENCY_OFFLINE.
     */
    class ReceiveMessageThread extends Thread{
        private final Socket socket;
        private final DomainServer domainServer;
        /**
         * Crea un nuevo thread para recibir el mensaje.
         *
         * @param b El RaModel de este thread.
         * @param socket Socket de la conexion entrante.
         */
        public ReceiveMessageThread(DomainServer b, Socket socket){
            this.socket = socket;
            this.domainServer = b;
        }

        /**
         * Maneja el mensaje de entrada.
         */
        public void run(){
            ObjectOutputStream outStream = null;
            ObjectInputStream inStream = null;

            try{
                inStream = new ObjectInputStream(
                                new BufferedInputStream(
                                    socket.getInputStream()));
                outStream = new ObjectOutputStream(
                                    socket.getOutputStream());
            }
            catch (IOException e){
                System.err.println("ReceiveMessageThread: IOException en los  streams de conexion al socket!");
                e.printStackTrace();
            }

            try{
                assert inStream != null;
                RaMessage message = (RaMessage) inStream.readObject();
                if ( !message.recipient.host.equals(raAddress.host) ){
                    // se reenvia el mensaje
                    new SendMessageThread(message).start();
                }
                else
                    switch (message.kind){
                        case AGENCY_ONLINE -> {
                            agencies.put((message.sender.host.toString() + ":" + message.sender.port), message.sender);
                            domainServer.broadcast();
                        }
                        case AGENCY_OFFLINE -> {
                            logger.log(Level.INFO, "AGENCY_OFFLINE message arrived from: {0}", message.sender.host.toString());
                            agencies.remove (message.sender.host.toString());
                            domainServer.broadcast();
                        }
                        default -> throw new RuntimeException("not implemented");
                    }


            }
            catch (IOException e){
                System.err.println("ReceiveMessageThread: IOException on data transfer!");
                e.printStackTrace();
            }
            catch (ClassNotFoundException e){
                System.err.println ("ReceiveMessageThread: ClassNotFoundException when reading inStream!");
                e.printStackTrace();
            }

            try{
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){
                System.err.println("ReceiveMessageThread: IOException at cleanup!");
                e.printStackTrace();
            }
        }
    } // ReceiveMessageThread


    /**
     * Escucha en el puerto especificado y lanza nuevos threads para recibir
     * mensajes de entrada.
     */
    class ListenThread extends Thread
    {
        private final DomainServer parent;

        public ListenThread (DomainServer parent){
            this.parent = parent;
        }

        public void run(){
            Thread shouldLive = listenThread;
            try{
                while (shouldLive == listenThread){
                    Socket socket = serverSocket.accept();
                    logger.log(Level.INFO, "Receiving a message");
                    new ReceiveMessageThread (parent, socket).start();
                    Thread.yield();
                }
            }
            catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


    /**
     * Envia un RaMessage a otra agencia.
     */
    static class SendMessageThread extends Thread{
        RaMessage msg;

        /**
         * Crea un thread que envia un mensaje a otro host.
         * El mensaje debe contener la direcci�n destino!
         *
         * @param msg El mensaje a enviar.
         */
        public SendMessageThread(RaMessage msg){
            this.msg = msg;
        }

        /**
         * Envia el mensaje a trav�s de una conexi�n de socket.
         */
        public void run(){
            Socket socket = null;
            ObjectOutputStream outStream = null;
            ObjectInputStream inStream = null;

            try {
                socket = new Socket(msg.recipient.host, msg.recipient.port);
                logger.log(Level.INFO, String.format("Socket created at: %s %d", msg.recipient.host, msg.recipient.port));
                outStream = new ObjectOutputStream(
                                    socket.getOutputStream());
                inStream = new ObjectInputStream(
                                new BufferedInputStream(
                                    socket.getInputStream()));
                outStream.writeObject (msg);
                outStream.flush();
                logger.log(Level.INFO, "Message written in socket");
            }
            catch (IOException e){
                e.printStackTrace();
            }

            try{
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * Todas las agencias conectadas en el dominio
     */
    private final Hashtable<String, RaAddress> agencies;

    /**
     * Direccion de este servidor.
     */
    private RaAddress raAddress;

    /**
     * Socket en el puerto principal.
     */
    private ServerSocket serverSocket = null;

    /**
     * Thread que acepta conexiones de red.
     */
    private volatile Thread listenThread = null;

    /**
     * Crea un nuevo servidor que maneja el estado del dominio.
     */
    public DomainServer(){
        agencies = new Hashtable<>();
    }

    /**
     * Baja la conexion de red.
     */
    public void dispose(){
        try{
            listenThread = null;
            serverSocket.close();
            logger.log(Level.INFO, "Server socket closed");
        }
        catch (IOException e){
            logger.log(Level.WARNING, "Couldn't close server socket");
            System.exit(1);
        }
    }

    /**
     * Notifica a todas las agencias conectadas al dominio qu� otras agencias
     * estan en linea en el dominio
     */
     public void broadcast(){
        RaMessage message;
        Hashtable<String, RaAddress> servers;
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;

        try{
            synchronized (this){
                servers = new Hashtable<>(agencies);
            }
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream (bos);
            oos.writeObject (servers);

            // envia un mensaje AGENCYS a todos los servidores conectados.
            for (Enumeration<RaAddress> e = servers.elements(); e.hasMoreElements();){
                message = new RaMessage (raAddress,
                                            e.nextElement(),
                                            RaMessage.Kind.AGENCIES_LIST,
                                            "",
                                            bos.toByteArray());
                new SendMessageThread(message).start();
            }
        }
        catch (IOException e){
            System.err.println ("BoModel: Broadcast ha fallado!");
            e.printStackTrace();
        }
    }

    /**
     * Comienza ha escuchar esperando mensajes.
     */
    public void startService (int portNo){

        try{
            raAddress = new RaAddress(InetAddress.getLocalHost(), portNo, null);
            serverSocket = new ServerSocket(portNo);
            logger.log(Level.INFO, "Listening to port " + portNo);
        }
        catch (UnknownHostException e){
            System.err.println ("RaModel: Couldn't determine local host address");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e){
            System.err.println ("RaModel: Couldn't create ServerSocket!");
            e.printStackTrace();
            System.exit(1);
        }
        listenThread = new ListenThread(this);
        listenThread.start();
    }

    /**
     * Deja de escuchar en el dominio de red.
     */
    public void stopService(){
        dispose();
        serverSocket = null;
    }

}
