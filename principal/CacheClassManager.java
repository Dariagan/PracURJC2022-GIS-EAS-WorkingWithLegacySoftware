package raf.principal;

import raf.config.ConfigLoader;

import java.io.*;
import java.lang.String;
import java.lang.StringIndexOutOfBoundsException;
import java.lang.Thread;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;


/**
 * Almacena las clases y los bytecodes de las clases cargadas.
 * Es responsable de borrar las clases si no hay mas agentes
 * activos de esa clase en la agencia.
 */
public class CacheClassManager extends SecureClassLoader implements ClassManager
{
    private static final Logger logger = Logger.getLogger(CacheClassManager.class.getName());
    private static final ConfigLoader CONFIG_LOADER = ConfigLoader.getInstance();

    /**
     * Stores class data in ClassBoxes, indexed by class name.
     */
    private final Hashtable<String, ClassBox<?>> cache = new Hashtable<>();

    private static final CacheClassManager CACHE_CLASS_MANAGER = new CacheClassManager();
    private CacheClassManager(){}
    public static CacheClassManager getInstance() {return CACHE_CLASS_MANAGER;}

    /**
     * Auxiliar class for wrapping classes alongside their bytecode and the agent count.
     */
    private static class ClassBox<T>{

        private final Class<T> clazz;

        private final byte[] byteCode;
        /**
         * Number of agents currently loaded
         */
        private int count;
        /**
         * Crea un nuevo ClassBox.
         * El contador es puesto a 1.
         *
         * @param clazz Class to be stored
         * @param byteCode Bytecode of the compiled class (read from the .class file)
         */
        private ClassBox (Class<T> clazz, byte[] byteCode){
            this.clazz = clazz;
            this.byteCode = byteCode;
            count = 1;
        }
    }



    @Override
    public Class<?> findClass(String name){
    	ClassBox<?> box = cache.get(name);
	    if (box != null){
	        return box.clazz;
	    }
	    else {
            addOne(name);
            return cache.get(name).clazz;
        }
    }

    public void addOne(String className){
        ClassBox<?> box = cache.get(className);
        if (box != null){
            box.count++;
        }
        else {
            byte[] byteCode = getByteCodeFromFile(className);
            try {
                loadClassIntoCache(className, resolveClass(className, byteCode), byteCode);
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads a class into cache
     *
     * @param name Name of the class
     * @param clazz class
     * @param byteCode array of bytes of the read .class file
     */
    private void loadClassIntoCache(String name, Class<?> clazz, byte[] byteCode){

        cache.put(name, new ClassBox<>(clazz, byteCode));
    }

    private Class<?> resolveClass(String name, byte[] byteCode) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            logger.log(Level.INFO, String.format("Attempting to define class %s, size: %dB", name, byteCode.length));

            clazz = defineClass(null, byteCode, 0, byteCode.length, (CodeSource) null);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Secure class loader: malformed URL");
            e.printStackTrace();
        }
        if (clazz == null) throw new ClassNotFoundException(name);
        else return clazz;
    }

    private byte[] getByteCodeFromFile(String name){
        String fileName;
        try {
            fileName = CONFIG_LOADER.agency.displayedAgentsPath() + "\\" + name;
            logger.log(Level.INFO, "Loading file: {0}", fileName);
        }
        catch (StringIndexOutOfBoundsException e){
            logger.log(Level.WARNING, "Invalid file name");
            fileName = name;
        }

        try {
            FileInputStream fileStream = new FileInputStream(fileName);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int data;
            while ((data = fileStream.read()) != -1 ){
                bos.write(data);
            }
            byte[] byteCode = bos.toByteArray();
            bos.close();
            fileStream.close();
            return byteCode;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void removeOne(String className){
        ClassBox<?> box = cache.get(className);
        if (box != null){
            box.count--;
            if (box.count == 0){
                // borra la clase despues de cierto tiempo
                (new Remover(className)).start();
            }
        }
    }

    /**
     * Clase auxiliar del CacheClassManager que borra una clase despues del tiempo de retardo.
     */
    class Remover extends Thread{

        /**
         * Nombre de la clase que va a ser borrada.
         */
        private final String name;

        /**
         * Tiempo de retardo en milisegundos despues del cual la clase es borrada si su contador llega a cero.
         */
        public Remover (String name){
            this.name = name;
        }

        /**
         * Espera el tiempo de retardo especificado y entonces borra la clase de la cache si el contador llega cero;
         */
        public void run(){
            try {
                Thread.sleep(CONFIG_LOADER.classManager.byteCodeDelay());
                // mejor bloquear la cache durante esta operacion
                if ((cache.get(name)).count == 0)
                    cache.remove(name);
            }
            catch (InterruptedException e){
                System.err.println ("El thread Remover ha sido interrumpido!");
            }
        }
    }

    /**
     * Completely removes a class and its bytecode from the cache
     *
     * @param name Name of the deleted class
     */
    public void removeClass (String name){
        cache.remove(name);
    }

}
