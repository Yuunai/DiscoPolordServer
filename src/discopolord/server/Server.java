package discopolord.server;

import discopolord.client.Client;
import discopolord.client.ClientStatusServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

public class Server extends Thread{

    private static Logger logger = Logger.getLogger(Server.class.getSimpleName());
    private static int port;

    private static Properties props = new Properties();

    private static final String serverPortProperty = "server.port";

    private static ClientStatusServer clientStatusServer = new ClientStatusServer();

    static {
        initializeProperties();
        port = Integer.valueOf(props.getProperty(serverPortProperty));
    }

    public Server() {

    }

    @Override
    public void run() {
        logger.info("Server started...");

        try(ServerSocket serverSocket = new ServerSocket(port)) {

            while(true) {
                new Client(serverSocket.accept(), clientStatusServer).start();
            }

        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }

    public static void initializeProperties() {
        logger.info("Loading config file...");
        try {

            InputStream propsInput = new FileInputStream("config.properties");
            props.load(propsInput);
            logger.info("Config file loaded...");
        } catch (FileNotFoundException e) {
            logger.warning("Properties file not found: " + e.getMessage());
        } catch (IOException e) {
            logger.warning("Properties file loading failed: " + e.getMessage());
        }
    }

}
