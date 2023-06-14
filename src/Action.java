import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class Action implements Runnable{
    private String name;
    private final Scanner sc;
    private final Writer writer;
    private final Socket socket;
    private boolean isConnect;
    private static Set<Action> clients;

    public Action(Socket socket, Server server) throws IOException {
        this.name = giveName();
        this.sc = getReader(socket);
        this.writer = getWriter(socket);
        this.socket = socket;
        this.isConnect = true;
        this.clients = new HashSet<>();
    }

    @Override
    public void run() {
        System.out.printf("Connected new user: %s%n", name );
        try(socket; sc; writer){
            sendResponse("Hi " + name);
            mailingList("Client " + name + " has connected");
            while (isConnect && !socket.isClosed()){
                var message = sc.nextLine().strip();
                if(isQuitMsg(message)){
                    System.out.printf("Client %s is disconnected", name );
                    break;
                }else if(isEmptyMsg(message)){
                    System.out.printf("Empty message");
                }else  mailingList(name + " : " + message);
            }
        }catch (NoSuchElementException e){
            mailingList("Client " + name + " is disconnected\n");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void mailingList(String response){
      getClients().forEach(c -> {
            if (c != this) {
                try {
                    c.sendResponse(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static Action connectClient(Socket socket, Server server) {
        try {
            return new Action(socket, server);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private PrintWriter getWriter(Socket socket) throws IOException{
        OutputStream out = socket.getOutputStream();
        return new PrintWriter(out);
    }

    private Scanner getReader(Socket socket) throws IOException{
        InputStream input = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        return new Scanner(reader);
    }

    private boolean isQuitMsg(String msg){
        return "bye".equalsIgnoreCase(msg);
    }

    private boolean isEmptyMsg(String msg){
        return msg == null || msg.isBlank();
    }

    private void sendResponse (String response) throws IOException {
        writer.write(response);
        writer.write(System.lineSeparator());
        writer.flush();
    }

    private String giveName() {
        String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
        java.util.Random rand = new java.util.Random();
        Set<String> identifiers = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        while(builder.toString().length() == 0) {
            int length = rand.nextInt(5)+5;
            for(int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if(identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }

    public  Set<Action> getClients() {
        return clients;
    }
}
