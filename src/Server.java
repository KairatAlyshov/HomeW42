import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private Server(int port) {
        this.port = port;
    }
    public static Server bindToPort(int port){
        return new Server(port);
    }

    public void run() {

        try(var server = new ServerSocket(port)){
            while (!server.isClosed()){
                Socket clientSocket = server.accept();
                pool.submit(() -> handle(clientSocket));
            }
        } catch (IOException e){
            System.out.printf("Port %s is busy.%n", port);
            e.printStackTrace();
        }
    }




}
