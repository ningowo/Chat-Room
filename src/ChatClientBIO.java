import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * This is a multi-thread client program, it starts two threads for sending and listening.
 *
 * @author Ning Ding
 * @version 2021.2.24
 */
final class ChatClientBIO {
    public static void main(String[] args) {
        System.out.println("======== Program Start ========");
        new ChatClientBIO().start(args[0], Integer.parseInt(args[1]), args[2]);
    }

    public void start(String server, int port, String username) {
        Socket socket = null;
        try {
            socket = new Socket(server, port);
            new Thread(new Send(socket, username)).start();
            new Thread(new Listen(socket)).start();
        } catch (IOException e) {
            System.out.println("Unable to connect");
            e.printStackTrace();
        }

    }

    private class Send implements Runnable {
        Socket socket;
        private ObjectOutputStream sOutput;

        public Send(Socket socket, String username) {
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sOutput.writeObject(username);
            } catch (IOException e) {
                System.out.println("Unable to connect.");
                try {
                    close(socket, null, sOutput);
                } catch (IOException ioException) {
                    ioException.printStackTrace();                    
                    System.exit(1);
                } 
            }
        }

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter whatever to chat with other\n" +
                    "Use '/msg someUsername yourMsg' to send to a specific user\n" +
                    "Use '/logout' to logout\n");
            try {
                while (true) {
                    ChatMessage chatMessage = new ChatMessage();
                    String msg = scanner.nextLine();
                    if (msg.startsWith("/logout")) {
                        System.out.println("Thank you for using our chatRoom!");
                        sOutput.writeObject(new ChatMessage(msg, 1));
                        break;
                    }
                    sOutput.writeObject(new ChatMessage(msg, 0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author Ning Ding
     * @version 2021/2/24
     */
    private final class Listen implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        
        public Listen(Socket socket) {
            try {
                this.socket = socket;
                this.sInput = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println("Fail to send to server " + socket.getInetAddress());
                System.exit(0);
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                }
            } catch (Exception e) {
                System.out.println("Connection closed.");
                try {
                    close(socket, sInput, null);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    private void close(Socket socket, InputStream is, OutputStream os) throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (is != null) {
            is.close();
        }
        if (os != null) {
            os.close();
        }
    }

}

