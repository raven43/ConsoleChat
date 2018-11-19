package app.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatUserTest2 {

    private Queue<ChatUser> userQueue;
    private Queue<ChatUser> agentQueue;
    private Queue<ChatUser> clientQueue;
    private ChatUser[] users;

    @Before
    public void setUp() {
        userQueue = new ConcurrentLinkedQueue<ChatUser>();
        agentQueue = new ConcurrentLinkedQueue<ChatUser>();
        clientQueue = new ConcurrentLinkedQueue<ChatUser>();

        users = new ChatUser[2];
    }

    @Test
    public void sendMessageTest() throws IOException {
        TestSocket socket = new TestSocket("sent message");
        users[0] = new ChatUser(socket,userQueue,agentQueue,clientQueue);

        Assert.assertEquals("sent message",users[0].getMessage());
    }
    @Test
    public void readMessageTest() throws IOException{
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
        users[0] = new ChatUser(new Socket(){
            @Override
            public OutputStream getOutputStream() throws IOException {
                return outputStream;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return inputStream;
            }
        },userQueue,agentQueue,clientQueue);

        users[0].sendMessage("message");
        Assert.assertEquals("message\r\n",new String(outputStream.toByteArray()));
    }
    @Test
    public void withOutSocketTest() throws IOException{
        final Queue<String> toUser = new ConcurrentLinkedQueue<String>();
        final Queue<String> fromUser = new ConcurrentLinkedQueue<String>();
        users[0]= new ChatUser(new TestSocket(""),userQueue,agentQueue,clientQueue){
            @Override
            public String getMessage() throws IOException {
                return fromUser.poll();
            }

            @Override
            public boolean sendMessage(String message) {
                toUser.add(message);
                return true;
            }
        };

        fromUser.add("/register client W");
        users[0].register();

        Assert.assertEquals(users[0].getRole(), ChatUser.ROLE.CLIENT);
    }

}
