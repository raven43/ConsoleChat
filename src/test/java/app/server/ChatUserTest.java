package app.server;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatUserTest {

    private ChatUser[] user;
    private Socket[] socket;

    private int count = 3;

    private Queue<ChatUser> userQueue;
    private Queue<ChatUser> agentQueue;
    private Queue<ChatUser> clientQueue;

    @Before
    public void setUp() {
        user = new ChatUser[2];
        userQueue = new ConcurrentLinkedQueue<ChatUser>();
        agentQueue = new ConcurrentLinkedQueue<ChatUser>();
        clientQueue = new ConcurrentLinkedQueue<ChatUser>();
    }

    @After
    public void clear(){
        user[0].off();
        user[1].off();
    }

    @Test
    public void registerTest() throws IOException{
        TestSocket agentSocket = new TestSocket("/register agent Smith");
        TestSocket clientSocket = new TestSocket("/register client Mary");
        user[0] = new ChatUser(agentSocket,userQueue,agentQueue,clientQueue);
        user[1] = new ChatUser(clientSocket,userQueue,agentQueue,clientQueue);

        user[0].register();
        user[1].register();

        Assert.assertEquals(ChatUser.ROLE.AGENT,user[0].getRole());
        Assert.assertEquals("Smith",user[0].getName());
        Assert.assertEquals(ChatUser.ROLE.CLIENT,user[1].getRole());
        Assert.assertEquals("Mary",user[1].getName());
    }

    @Test
    public void boundTest() throws IOException{

        registerTest();

        user[0].findCompanion();
        Assert.assertTrue(agentQueue.contains(user[0]));
        user[1].findCompanion();

        Assert.assertEquals(user[0],user[1].getCompanion());
        Assert.assertEquals(user[1],user[0].getCompanion());
    }

    @Test
    public void ClientLeaveTest() throws IOException{
        boundTest();
        user[1].leaveChat("leave");
        Assert.assertTrue(agentQueue.contains(user[0]));
    }
    @Test
    public void AgentLeaveTest() throws IOException{
        boundTest();
        user[0].leaveChat("exit");
        Assert.assertTrue(clientQueue.contains(user[1]));
    }


}
