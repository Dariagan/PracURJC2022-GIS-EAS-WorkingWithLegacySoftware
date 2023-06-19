package raf.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Class in charge of loading up the configuration used by other classes
 */
public class ConfigLoader {
    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    private static final String CONFIG_FILE_PATH = "config/agenthandler.config";

    public final AgencyConfig agency;
    public final DomainServerConfig domainServer;
    public final ClassManagerConfig classManager;


    public record AgencyConfig(int port, File displayedAgentsPath){}
    public record DomainServerConfig(String ip, int port){}
    public record ClassManagerConfig(long byteCodeDelay){}

    private static final ConfigLoader CONFIG_LOADER = new ConfigLoader();
    public static ConfigLoader getInstance(){
        return CONFIG_LOADER;
    }

    private ConfigLoader(){
        Properties props = new Properties();
        loadProperties(props);
        handleOptionalByteCodeCopying(props);

        agency = new AgencyConfig(
                extractAgencyListenPort(props),
                new File(props.getProperty("displayedAgentsPath")));

        domainServer = new DomainServerConfig(
                props.getProperty("raDomainServerIp"),
                extractRaDomainServerPort(props));

        classManager = new ClassManagerConfig(extractByteCodeDelay(props));
    }

    private static void loadProperties(Properties props){
        try {
            FileInputStream in = new FileInputStream(CONFIG_FILE_PATH);
            props.load(in);
            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void handleOptionalByteCodeCopying(Properties props){
        if (Boolean.parseBoolean(props.getProperty("copyClassFiles"))) {
            logger.log(Level.INFO, "Copying .class files is enabled in {0}, starting copy operation", CONFIG_FILE_PATH);
            try {
                File sourceDirectory = new File(props.getProperty("compiledAgentsPath"));
                File destinationDirectory = new File(props.getProperty("displayedAgentsPath"));
                copyFiles(sourceDirectory, destinationDirectory);
                logger.log(Level.INFO, "Copy operation successful");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Copy operation failed");
                e.printStackTrace();
            }
        }
        else logger.log(Level.INFO, "Copying .class files is disabled in {0}", CONFIG_FILE_PATH);
    }

    private static void copyFiles(File sourceDirectory, File destinationDirectory) throws IOException {

        File[] javaFiles = sourceDirectory.listFiles();

        if (javaFiles != null) {
            for (File file : javaFiles) {
                Path destination = Path.of(destinationDirectory + File.separator + file.getName());

                String fileName = file.getName();
                if (!fileName.equals("displayed")) {
                    if (fileName.contains("$")) {

                        String newFileName = "raf.agentes." + fileName.substring(0, fileName.length() - 6);
                        destination = Path.of(destinationDirectory + File.separator + newFileName);
                    }
                    Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static int extractAgencyListenPort(Properties props){
        final int DEFAULT_VALUE = 10101;
        try {
            return Integer.parseInt(props.getProperty("port", String.valueOf(DEFAULT_VALUE)));
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return DEFAULT_VALUE;
        }
    }

    private static long extractByteCodeDelay(Properties props){
        final long DEFAULT_VALUE = 100_000;
        try {
            return Long.parseLong(props.getProperty("byteCodeDelay",  String.valueOf(DEFAULT_VALUE)));
        }
        catch (NumberFormatException e){
            return DEFAULT_VALUE;
        }
    }

    private static int extractRaDomainServerPort(Properties props){
        final int DEFAULT_VALUE = 10102;
        try {
            return Integer.parseInt(props.getProperty("raDomainServerPort", String.valueOf(DEFAULT_VALUE)));
        }
        catch (NumberFormatException e){
            return DEFAULT_VALUE;
        }
    }

}
