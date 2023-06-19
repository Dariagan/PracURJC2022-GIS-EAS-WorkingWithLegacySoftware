package raf.principal;


public interface RaMessageListener extends java.util.EventListener{

    void raMessageArrived(RaMessageEvent e);
}