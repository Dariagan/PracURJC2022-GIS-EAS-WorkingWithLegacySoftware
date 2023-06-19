package raf.principal.messageHandler;


import raf.principal.Agency;
import raf.principal.RaAddress;
import raf.principal.RaMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;

public class AgenciesListMessageHandler implements MessageHandler {

    private final Agency agency;

    public AgenciesListMessageHandler(Agency agency) {
        this.agency = agency;
    }

    @Override
    public void handleMessage(RaMessage message) {
        ByteArrayInputStream bis = new ByteArrayInputStream(message.binary);
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            synchronized (agency) {
                agency.setAgencies((Hashtable<String, RaAddress>) ois.readObject());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
