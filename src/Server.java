import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private Set<ClientHandler> clients;
    private Server(int port) {
        this.port = port;
        clients = new HashSet<>();
    }
    public static Server bindToPort(int port){
        return new Server(port);
    }

    public void run() {

        try(var server = new ServerSocket(port)){
            while (!server.isClosed()){
                Socket clientSocket = server.accept();
                var client = ClientHandler.connectClient(clientSocket, this);
                if(client != null){
                    clients.add(client);
                    pool.submit(client);
                }
            }
        } catch (IOException e){
            System.out.printf("Port %s is busy.%n", port);
            e.printStackTrace();
        }
    }


    public Set<ClientHandler> getClients() {
        return clients;
    }

}
