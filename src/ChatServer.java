import jdk.management.resource.internal.inst.SocketChannelImplRMHooks;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 *
 * This is a chatroom server using Java NIO, it is designed to handle the chat 
 * between large amount of clients
 *
 * @author ding.ning
 * @date 2021.2.25
 */

public class ChatServer {
    private final int port;
    private ChatFilter filter;
    private Selector selector;
    private final ConcurrentHashMap<String, SocketChannel> clients = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public ChatServer(int port, String fileToFilter) {
        this.port = port;
        this.filter = new ChatFilter(fileToFilter);
    }

    public void start() {
        System.out.println("Server start");

        try {
            // 注册并设置一下参数
            selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.socket().setReuseAddress(true);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 2,
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(20));

        try {
            while (true) {
                // 如果selector发现新连接。不用if因为这个是阻塞的，一直阻塞直到选择到channel
                selector.select();
                // 用key找到对应连接，并挨个处理
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    try {
                        if (key.isAcceptable()) {
                            // 如果是新连接就注册到selector里，监听新连接的读事件
                            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                            SocketChannel sc = ssc.accept();
                            sc.configureBlocking(false);
                            // 绑定两个buffer
                            sc.register(selector, SelectionKey.OP_READ);
                            //System.out.println("==Connection established: " + key);
                        } else if (key.isReadable()){ // 客户端有写入
                            //System.out.println("==Write request: " + key);
                            // 通过key找到channel
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            // 有需求的时候再建buffer（不是在accept的时候建）
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            // 把数据读到byteBuffer（内存）里
                            clientChannel.read(byteBuffer);
                            // 翻转一下，准备写给别的client
                            //byteBuffer.flip();
                            // 把buffer转换成数组
                            ObjectInputStream is = new ObjectInputStream(
                                    new ByteArrayInputStream(byteBuffer.array()));
                            // 读完之后归零
                            byteBuffer.clear();
                            // 处理得到的信息
                            ChatMessage msg = (ChatMessage) is.readObject();
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    process(msg, clientChannel);
                                }
                            });
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        key.channel().close();
                        for (Map.Entry<String, SocketChannel> entry: clients.entrySet()) {
                            if (key.channel() == entry.getValue()) {
                                clients.remove(entry.getKey());
                                break;
                            }
                        }
                        System.out.println(key.channel() + " closed");
                    } finally {
                        //System.out.println("key removed");
                        keys.remove();
                    }
                } // end while
            }
        } catch (IOException e) {
            System.out.println("Server close.");
            e.printStackTrace();
        }

    }


    private void process(ChatMessage msg, SocketChannel toChannel)  {
        String from = msg.getFrom();
        String to = msg.getTo();
        int type = msg.getType();
        String content = msg.getContent();

        if (type == 2) {
            for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                writeText(from, entry.getValue(), content);
            }
            System.out.printf("--> %s: %s\n", from, content);
        } else if (type == 1) {
            boolean exist = false;
            for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                if (entry.getKey().equals(to)) {
                    exist = true;
                    writeText(from, entry.getValue(), content);
                    System.out.printf("--> %s => %s: %s\n", from, to, content);
                }
            }
            if (!exist) {
                writeText("Server", toChannel, "No such user exist.");
            }
        } else if (type == 0) {
            if (clients.containsKey(from)) {
                writeText("Server", toChannel, "Sorry, username has been occupied.");
                System.out.println("Connection failed - duplicate username: " + from);
            } else {
                clients.put(from, toChannel);
                writeText("Server", toChannel, "Connected to server");
                System.out.println("Connected: " + from);
            }
        } else if (type == -1) {
            System.out.println("... " + from + " logout");
            clients.remove(from);
            try {
                toChannel.close();
                System.out.println("Remove " + from);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeText(String from, SocketChannel channel, String text) {
        String sb = filter.filter(from + ": " + text);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(sb.getBytes());
        buffer.flip();

        synchronized (buffer) {
            try {
                channel.finishConnect();
                channel.write(buffer);
            } catch (IOException e) {
                try {
                    channel.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                System.out.println("Unable to write the message from " + from);

            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(Integer.parseInt(args[0]), args[1]);
        server.start();
    }
}
