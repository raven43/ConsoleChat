package app.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


public class UserThread implements Runnable {


    Logger log = Logger.getLogger(UserThread.class);

    protected ChatUser user;
    ExecutorService executor;

    UserThread(ChatUser user,ExecutorService executor) {
        this.user = user;
        this.executor = executor;
    }

    UserThread(UserThread parent) {
        this.user = parent.user;
        this.executor = parent.executor;
    }

    public void run() {
        Thread.currentThread().setName(user.getName());
        log.debug("Thread \'" + Thread.currentThread().getName() + "\' start");

        user.sendMessage("Ok, now register as an agent or client");
        while (user.getRole()== ChatUser.ROLE.USER){
            if(!user.register()) break;
        }

        if(user.getRole()== ChatUser.ROLE.AGENT) {
            user.sendMessage("Welcome " + user.getName() + "! You register as an agent");
            executor.submit(new AgentThread(this));
        }
        if(user.getRole()== ChatUser.ROLE.CLIENT){
            user.sendMessage("Hello " + user.getName() + "! You register as an client");
            executor.submit(new ClientThread(this));
        }

        //register
//       while (true) {
//            //get message contains "/register [agent|client] <name>"
//            String registerLine;
//            try {
//                registerLine = user.getMessage();
//            } catch (IOException e) {
//                log.info("user " + user.getName() + " disconnect");
//                user.off();
//                break;
//            }
//            if (registerLine.contains("/register agent")) {
//                user.setRole(ChatUser.ROLE.AGENT);
//                if (registerLine.length() > "/register agent ".length())
//                    user.setName(registerLine.substring(16));
//
//                user.sendMessage("Welcome " + user.getName() + "! You register as an agent");
//                log.info("agent " + user.getName() + " log in");
//
//                //start agent thread
//                executor.submit(new AgentThread(this));
//                break;
//            }
//            if (registerLine.contains("/register client")) {
//                user.setRole(ChatUser.ROLE.CLIENT);
//                if (registerLine.length() > "/register client ".length())
//                    user.setName(registerLine.substring(17));
//                user.sendMessage("Hello " + user.getName() + "! You register as an client");
//                log.info("client " + user.getName() + " log in");
//
//                //start client thread
//                executor.submit(new ClientThread(this));
//                break;
//            }
//            if (registerLine.contains("/off")) {
//                break;
//            }
//            user.sendMessage("Something wrong, try again");
//        }
        log.debug("Thread \'" + Thread.currentThread().getName() + "\' stop");


    }

    protected void backToUserThread() {
        user.setRole(ChatUser.ROLE.USER);
        executor.submit(new UserThread(this));
    }
}
