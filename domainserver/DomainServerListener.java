package raf.domainserver;

/**
 * Interfaz para eventos que pueden ser lanzados por el ramodel.
 * Esos eventos son lanzados cuando un servidor se conecta o deconecta del dominio
 *
 * @author RMN
 */
public interface DomainServerListener extends java.util.EventListener{

    /**
     * Un servidor se ha conectado al dominio.
     */
    void raModelAgencyOnline (DomainServerEvent e);

    /**
     * Un servidor se ha desconectado del dominio.
     */
    void raModelAgencyOffline (DomainServerEvent e);

}
