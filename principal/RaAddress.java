package raf.principal;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;


public final class RaAddress implements Serializable{


    public InetAddress getHost() {
        return host;
    }
    public InetAddress host;
    public final int port;
    public final String name;
   
    public RaAddress(InetAddress host, int port, String name){
        this.host = host;
        this.port = port;
        this.name = name;
    }

    
    public RaAddress(String hostname, int port, String name) throws UnknownHostException{
        host = InetAddress.getByName (hostname);
        this.port = port;
        this.name = name;
    }    

  
    public RaAddress (String name) {
        host = null;
        this.port = 0;
        this.name = name;
    }    
    
 
    public String toString() {
    	if(host!=null) return host + ":" + port + ":" + name;
        return "local:"+ name;
     }    
    
}
