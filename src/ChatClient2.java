import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
final class ChatClient2 {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    public String message;
    public int type;

    //public boolean finished;

    private ChatClient2(String username, int port, String server) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    // create a thread to listen and send username, initialize socket, sInput and sOutput
    private boolean start() {
        try {
            socket = new Socket(server, port);
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());

            // listenFromServer establish a status that continuously listen
            Runnable r = new ListenFromServer();
            Thread t = new Thread(r);
            t.start();
            sOutput.writeObject(username);
            return true;
        } catch (ConnectException e) {
            System.out.println("Sorry, the server you want to connect is not working now.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (SocketException e) {
            e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        if (args.length == 0) {
            ChatClient2 client = new ChatClient2("Anonymous", 1500, "localhost");

            process(scanner, client);

        } else if (args.length == 1) {
            ChatClient2 client = new ChatClient2(args[0], 1500, "localhost");

            // send username
            process(scanner, client);

        } else if (args.length == 2) {
            ChatClient2 client = new ChatClient2(args[0], Integer.parseInt(args[1]), "localhost");

            process(scanner, client);

        } else {
            ChatClient2 client = new ChatClient2(args[0], Integer.parseInt(args[1]), args[2]);

            process(scanner, client);
        }

    }

    private static void process(Scanner scanner, ChatClient2 client) throws IOException {
        if (!client.start())
            return;

        System.out.println("Please enter your message");
        while (true) {
            client.message = scanner.nextLine();
            if (client.message.length() == 7) {
                if (client.message.startsWith("/logout")) {
                    System.out.println("Thank you for using our chat program!");
                    client.type = 1;
                    client.sendMessage(new ChatMessage(client.message, client.type));
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.isConnected();
                    break;
                }
            }
            client.sendMessage(new ChatMessage(client.message, client.type));
        }
    }


    /**
     * This is a private class inside of the ChatClient2
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author your name and section
     * @version date
     */
    private final class ListenFromServer implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    //System.out.println("reading");
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                }
            } catch (SocketException e) {
                try {
                    socket.close();
                    sInput.close();
                    sOutput.close();
                } catch (SocketException e1) {
                    System.out.println("Connection closed");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (EOFException e1) {
                System.out.println();
                System.out.println("I guess last time you just directly close ChatClient2s lol, " +
                        "you should use \"/logout\" instead.");
                System.out.println("--- But don't worry! I can handle that ;)");
                System.out.println("Just run it again!");
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
