package app.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientMain {



    private static final String address = "127.0.0.1";
    private static final int port = 6666;
    private static final int timeOut = 100;

    private static boolean isWorking;
    private static BufferedReader in;
    private static PrintWriter out;
    private static BufferedReader keyboard;

    public static void main(String[] args) {


        isWorking = true;
        Socket socket = connect();

        //create reader to read message fom console
        keyboard = new BufferedReader(new InputStreamReader(System.in));


        //create thread to sending messages
        Thread speakingThread = new Thread(new Runnable() {
            public void run() {
                while (isWorking){
                    try {
                        String line = keyboard.readLine();
                        if(line.contains("/off")){
                            isWorking = false;
                            break;
                        }
                        out.println(line);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }, "speak");
        speakingThread.start();
        try {
            //reading messages
            while (isWorking){
                try {
                    String line = in.readLine();
                    System.out.println(line);
                }catch (IOException e){
                    System.out.println("lost connection!");
                    connect();
                }
            }
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }




    }
    private static Socket connect(){
        System.out.println("Connecting...");
        while (true){
            try {
                //connect to app.server
                InetAddress serverAddress = InetAddress.getByName(ClientMain.address);
                Socket socket = new Socket(serverAddress, ClientMain.port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(),true);

                if (socket.isConnected()) {
                    System.out.println("Successful connect!");
                    return socket;
                }
                else
                    Thread.sleep(timeOut);
            }
            catch (Exception ignored){}
        }
    }

}
