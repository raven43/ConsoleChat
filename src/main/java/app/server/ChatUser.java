package app.server;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatUser {

    private static Logger log = Logger.getLogger(ChatUser.class);
    private String name;
    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private ChatUser companion;
    private Queue messageCash;
    private Queue messageHistory;
    private ROLE role;

    private Queue<ChatUser> userQ;
    private Queue<ChatUser> agentQ;
    private Queue<ChatUser> clientQ;


    private static int counter = 1;

    ChatUser(Socket socket,Queue<ChatUser> userQ,Queue<ChatUser> agentQ,Queue<ChatUser> clientQ) {

        this.socket = socket;

        this.userQ = userQ;
        this.agentQ = agentQ;
        this.clientQ = clientQ;

        this.role = ROLE.USER;
        this.name = "user#"+(counter++);
        this.messageCash = new ConcurrentLinkedQueue();
        this.messageHistory = new ConcurrentLinkedQueue();
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Queue getMessageCash() {
        return messageCash;
    }

    public Queue getMessageHistory() {
        return messageHistory;
    }

    public ROLE getRole() {
        return role;
    }

    public void setRole(ROLE role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatUser getCompanion() {
        return companion;
    }

    public void setCompanion(ChatUser companion) {
        this.companion = companion;
    }

    public void freeCompanion() {
        setCompanion(null);
    }

    public String getMessage()throws IOException {
        return in.readLine();
    }

    public boolean sendMessage(String message) {
        out.println(message);
        return true;
    }

    public void sendToCompanion(String message) {
        if (role == ROLE.AGENT)
            this.getCompanion().getMessageHistory().add(role.str + " " + name + ": " + message);
        if (role == ROLE.CLIENT)
            this.getMessageHistory().add(role.str + " " + name + ": " + message);
        companion.sendMessage(role.str + " " + name + ": " + message);
    }

    public boolean isFree() {
        return getCompanion() == null;
    }

    public boolean register(){
            //get message contains "/register [agent|client] <name>"
            String registerLine;
            try {
                registerLine = getMessage();
            } catch (IOException e) {
                log.info("user " + getName() + " disconnect");
                off();
                return false;
            }
            if (registerLine.contains("/register agent")) {
                setRole(ROLE.AGENT);
                if (registerLine.length() > "/register agent ".length())
                    setName(registerLine.substring(16));

                log.info("agent " + getName() + " log in");
                return true;
            }
            if (registerLine.contains("/register client")) {
                setRole(ROLE.CLIENT);
                if (registerLine.length() > "/register client ".length())
                    setName(registerLine.substring(17));
                log.info("client " + getName() + " log in");

                return true;
            }
            sendMessage("Something wrong, try again");
            return true;
        }



    public synchronized void findCompanion() {
        this.freeCompanion();

        if (role == ROLE.AGENT) {
            sendMessage("waiting client..");
            if (!clientQ.isEmpty()) {
                //bound agent and client
                setCompanion(clientQ.poll());
                getCompanion().setCompanion(this);

                log.info("Start chat btw agent \'" + name + "\' and client \'" + companion.name + "\'");

                //send info to client
                getCompanion().sendMessage("u get agent " + name);
                //send info to agent
                sendMessage("u get client " + companion.name);
                //send to agent client's message history and cash(if it's exist)
                Iterator iterator = companion.getMessageHistory().iterator();
                while (iterator.hasNext())
                    sendMessage((String) iterator.next());

                while (!companion.getMessageCash().isEmpty())
                    companion.sendToCompanion((String) companion.getMessageCash().poll());


            } else {
                agentQ.add(this);
            }
        }
        if (role == ROLE.CLIENT) {
            sendMessage("waiting agent..");
            if (!agentQ.isEmpty()) {
                //bound agent and client
                setCompanion(agentQ.poll());
                getCompanion().setCompanion(this);
                log.info("Start chat btw agent \'" + companion.name + "\' and client \'" + name + "\'");

                //send info to client
                sendMessage("u get agent " + companion.name);

                //send info to agent
                getCompanion().sendMessage("u get client " + name);

                //send to agent client's message history and cash(if it's exist)
                Iterator iterator = getMessageHistory().iterator();
                while (iterator.hasNext())
                    companion.sendMessage((String) iterator.next());
                while (!getMessageCash().isEmpty())
                    sendToCompanion((String) getMessageCash().poll());

            } else
                clientQ.add(this);

        }
    }

    public void leaveChat(String reason){
        log.info(getRole().str+" "+getName()+" "+reason);

        getMessageHistory().clear();
        getMessageCash().clear();
        if (!isFree()) {
            log.info("Stop  chat btw "+getRole().str+" \'"+getName()+"\' " +
                    "and "+getCompanion().getRole().str+" \'"+getCompanion().getName()+"\': agent "+reason);
            getCompanion().sendMessage("Your "+getRole().str+" "+reason);
            getCompanion().findCompanion();
            freeCompanion();
        }
        else {
            if (getRole() == ROLE.AGENT)
                agentQ.remove(this);
            if (getRole() == ROLE.CLIENT)
                clientQ.remove(this);
        }
    }


    public void off(){
        try {
            userQ.remove(this);
            socket.close();
            in.close();
            out.close();
        }catch (Exception e){
            log.error("Error",e);
        }

    }
    @Override
    public String toString() {
        String result = role.str+" "+name+"\n";
        if(isFree())result+="    companion: null\n";
        else result+="    companion: "+companion.role.str+" "+companion.name+"\n";
        if (role==ROLE.CLIENT&&!messageHistory.isEmpty()){
            result = result.concat("    Message history:\n");
            Iterator iterator = messageHistory.iterator();
            while (iterator.hasNext())
                result = result.concat("        "+iterator.next()+"\n");
        }
        if (role==ROLE.CLIENT&&!messageCash.isEmpty()){
            result = result.concat("    Message cash:\n");
            Iterator iterator = messageCash.iterator();
            while (iterator.hasNext())
                result = result.concat("        "+iterator.next()+"\n");
        }
        return result;
    }

    enum ROLE {
        USER("User"), AGENT("Agent"), CLIENT("Client");
        String str;

        ROLE(String str) {
            this.str = str;
        }
    }
}

