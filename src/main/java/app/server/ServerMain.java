package app.server;

import org.apache.log4j.Logger;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class ServerMain {

    private static Logger log = Logger.getLogger(ServerMain.class);

    private static boolean isRunning = true;
    private static int port = 6666;
    public static ExecutorService executor = Executors.newFixedThreadPool(1000);


    public static void main(String[] args) {

        log.info("Start server");

        final Queue<ChatUser> userQueue = new ConcurrentLinkedQueue<ChatUser>();
        final Queue<ChatUser> agentQueue = new ConcurrentLinkedQueue<ChatUser>();
        final Queue<ChatUser> clientQueue = new ConcurrentLinkedQueue<ChatUser>();



        //console input thread
        Thread console = new Thread(new Runnable() {
            public void run() {

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            while (isRunning){
                try {
                    String command = keyboard.readLine();
                    if (command.contains("/info")){
                        System.out.println("/users\n"+
                                           "/agents\n"+
                                           "/clients\n");
                    }

                    if (command.contains("/users")){
                        System.out.println("USERS ("+userQueue.size()+"):");
                        Iterator iterator = userQueue.iterator();
                        while (iterator.hasNext())
                            System.out.print(iterator.next().toString());
                    }
                    if (command.contains("/agents")){
                        System.out.println("AGENTS:");
                        Iterator iterator = userQueue.iterator();
                        while (iterator.hasNext()){
                            ChatUser u = (ChatUser) iterator.next();
                            if(u.getRole()== ChatUser.ROLE.AGENT)
                                System.out.print(u);
                        }
                    }
                    if (command.contains("/clients")){
                        System.out.println("AGENTS:");
                        Iterator iterator = userQueue.iterator();
                        while (iterator.hasNext()){
                            ChatUser u = (ChatUser) iterator.next();
                            if(u.getRole()== ChatUser.ROLE.CLIENT)
                                System.out.print(u);
                        }
                    }
                    if (command.contains("/off")){
                        isRunning = false;
                        break;
                    }
                }
                catch (IOException e){
                    log.error("Console thread error",e);
                }
            }

            }
        },"console");
        console.start();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (isRunning){
                Socket clientSocket = serverSocket.accept();
                ChatUser user = new ChatUser(clientSocket,userQueue,agentQueue,clientQueue);

                userQueue.add(user);
                UserThread userThread = new UserThread(user,executor);
                executor.submit(userThread);
            }
        }
        catch (Exception e){
            log.error("Server mainThread ex",e);
        }
    }
}
