package raf.principal;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AgentClassInputStream extends ObjectInputStream{

    private static final Logger logger = Logger.getLogger(AgentClassInputStream.class.getName());
    
    public AgentClassInputStream(InputStream in) throws IOException{
        super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass v)
    throws ClassNotFoundException{
        logger.log(Level.INFO, "Resolving class {0}", v.getName());
        Class<?> result = CacheClassManager.getInstance().loadClass(v.getName());
        logger.log(Level.INFO, "Class {0} loaded", v.getName());
        return result;
    }
}
