import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * by googling, learned how to get current date and convert it to String
 *
 * @author your name and section
 * @version date
 */

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    public static String wordToFilter;

    private ChatServer(int port) {
        this.port = port;
    }

    // wait for client connection
    // create a new ClientThread object (run a method when created)
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                /*
                !!!! the time that t, and thus r, start, also ClientThread created is -- after t.start() is run
                 */
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) throws IOException {
        // filter
        ChatFilter chatFilter = new ChatFilter(wordToFilter);
        String filteredMessage = chatFilter.filter(message);

        //generate date
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String dateString = dateFormat.format(Calendar.getInstance().getTime());

        // print for clients
        for (int i = 0; i < clients.size(); i++) {
            synchronized (clients) {
                clients.get(i).writeMessage(String.format("%s  -%s", filteredMessage, dateString));
            }
        }
        // print for server
        System.out.printf("\"%s\" broadcast at %s\n", filteredMessage, dateString);
    }

    private void remove(int id1) {
        for (int i = 0; i < clients.size(); i++) {
            synchronized (clients) {
                if (clients.get(i).id == id1)
                    clients.remove(i);
            }
        }
    }

    private void directMessage(String message, String username) throws IOException {
        // filter
        ChatFilter chatFilter = new ChatFilter(wordToFilter);
        String filteredMessage = chatFilter.filter(message);

        //generate date
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String dateString = dateFormat.format(Calendar.getInstance().getTime());

        // print for clients
        for (int i = 0; i < clients.size(); i++) {
            synchronized (clients) {
                if (username.equals(clients.get(i).username)) {
                    clients.get(i).writeMessage(String.format("Direct message from %s: %s  -%s", username,
                            filteredMessage, dateString));
                }
            }
        }
        // print for server
        System.out.printf("message \"%s\" sent directly to %s at %s\n", filteredMessage, username, dateString);
    }

    // main-------------
    public static void main(String[] args) throws IOException {
        System.out.println("ChatServer Main method Started.");

        if (args.length == 2) {
            ChatServer.wordToFilter = args[1];
            ChatFilter chatFilter = new ChatFilter(wordToFilter);
        }

        if (args[0].isBlank()) {
            ChatServer server = new ChatServer(1500);
            server.start();
        } else {
            ChatServer server = new ChatServer(Integer.parseInt(args[0]));
            server.start();
        }

        System.out.println("ChatServer Main method finished.");

    }

    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     *
     * @author your name and section
     * @version date
     */
    public final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;
        String possibleUsername;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                possibleUsername = (String) sInput.readObject();

                sOutput.writeObject("Welcome to Our Chat Program!");

                for (int i = 0; i < clients.size(); i++) {
                    if (possibleUsername.equals(clients.get(i).username)) {
                        sOutput.writeObject("Sorry, you can not log in again with same username");
                        clients.remove(clients.size() - 1);
                        close();
                        return;
                    }
                }
                username = possibleUsername;

                System.out.println(username + ": connection established");
                //sOutput.writeObject("connection established");
                //sOutput.flush();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        // when ClientThread is created, run run() (readObject immediately)
        public void run() {
            try {
                while (true) {
                    cm = (ChatMessage) sInput.readObject();

                    // if user input /logout, close server parts
                    if (cm.getType() == 1) {
                        broadcast(String.format("%s has logged out", username));
                        clients.remove(this);
                        close();

                    } else {
                        if (cm.getMessage().length() == 0) {
                            broadcast(String.format("%s: %s", username, cm.getMessage()));
                        } else {
                            if (cm.getMessage().charAt(0) == '/') {
                                String[] array = cm.getMessage().split(" ");

                                // for direct message and printing all connected user
                                if (array[0].equals("/msg")) {
                                    if (array[1].equals(username)) {
                                        sOutput.writeObject("Sorry, you can not send message to yourself.");
                                        continue;
                                    }
                                    for (int i = 0; i < clients.size(); i++) {
                                        if (array[1].equals(clients.get(i).username)) {
                                            directMessage(cm.getMessage().substring(4), clients.get(i).username);
                                            sOutput.writeObject(String.format("Your message has been sent directly " +
                                                    "to %s", clients.get(i).username));
                                        }
                                    }
                                } else if (array[0].equals("/list")) {
                                    //ArrayList<String> arrayList = new ArrayList<>();
                                    sOutput.writeObject("Still connected users are: ");
                                    for (ClientThread client : clients) {
                                        if (!client.username.equals(username))
                                            sOutput.writeObject(String.format("%s ", client.username));
                                    }
                                    //sOutput.writeObject("\n");
                                } else {
                                    broadcast(String.format("%s: %s", username, cm.getMessage()));
                                }

                            } else {
                                //System.out.println("let's see the message: " + cm.getMessage());
                                broadcast(String.format("%s: %s", username, cm.getMessage()));
                            }
                        }
                    }
                    //System.out.println("Number of client: " + clients.size());
                }
            } catch (SocketException e) {
                if (username != null)
                    System.out.println("closed for " + username);
                try {
                    close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (NullPointerException e) {
            System.out.println(" (Last time, this user close chat program with the wrong method)");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void close() throws IOException {
            socket.close();
            sInput.close();
            sOutput.close();
        }

        private boolean writeMessage(String message) {
            try {
                sOutput.writeObject(message);
                sOutput.flush();
                return true;
            } catch (SocketException e) {
                System.out.println(" (Last time, this user close chat program with the wrong method)");
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
