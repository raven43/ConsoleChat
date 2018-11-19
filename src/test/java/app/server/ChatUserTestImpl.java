package app.server;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatUserTestImpl extends ChatUser {

    private boolean connected = true;

    private Queue<String> toUserQ = new ConcurrentLinkedQueue<String>();

    private Queue<String> fromUserQ = new ConcurrentLinkedQueue<String>();


    public ChatUserTestImpl(Queue<ChatUser> userQ, Queue<ChatUser> agentQ, Queue<ChatUser> clientQ) {
        super(userQ, agentQ, clientQ);
    }

    @Override
    public String getMessage() {
        return fromUserQ.poll();
    }

    @Override
    public boolean sendMessage(String message) {
        toUserQ.add(message);
        return isConnected();
    }

    public Queue<String> getToUserQ() {
        return toUserQ;
    }

    public Queue<String> getFromUserQ() {
        return fromUserQ;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
