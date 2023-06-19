package raf.domainserver;

import raf.principal.RaAddress;

/**
 * Eventos that son lanzados por el ramodel para notifcar a los listeners si
 * los servidores se conectan o desconectan desde el dominio.
 */
public class DomainServerEvent extends java.util.EventObject{

    /**
     * Direccion de la agencia que se desconecta o desconecta del dominio.
     */
    private final RaAddress agency;

    /**
     * @param sender Object que lanza el evento.
     * @param agency El servidor de agentes que se ha conectado o desconectado del dominio.
     */
    public DomainServerEvent(Object sender, RaAddress agency){
        super (sender);
        this.agency = agency;
   }

    /**
     * Devuelve la direccion del servidor que se ha conectado o deconectado del dominio.
     */
    public RaAddress getAgency(){
        return agency;
    }

}