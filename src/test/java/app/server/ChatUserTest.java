package app.server;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatUserTest {

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
    public void readWriteSocketTest() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("message1".getBytes());
        ChatUser user = new ChatUser(new Socket() {
            @Override
            public OutputStream getOutputStream() {
                return outputStream;
            }

            @Override
            public InputStream getInputStream() {
                return inputStream;
            }
        }, userQueue, agentQueue, clientQueue);
        user.sendMessage("message2");

        assertEquals("message1", user.getMessage());
        assertEquals("message2\r\n", new String(outputStream.toByteArray()));
    }

    @Test
    public void registerTest() {
        ChatUserTestImpl client = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);
        ChatUserTestImpl agent = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);

        client.getFromUserQ().add("/register client Mary");
        agent.getFromUserQ().add("/register agent Smith");

        client.register();
        assertEquals("Mary", client.getName());
        assertEquals(ChatUser.ROLE.CLIENT, client.getRole());

        agent.register();
        assertEquals("Smith", agent.getName());
        assertEquals(ChatUser.ROLE.AGENT, agent.getRole());
    }

    @Test
    public void boundTest() {
        ChatUserTestImpl client = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);
        ChatUserTestImpl agent = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);

        client.getFromUserQ().add("/register client Mary");
        agent.getFromUserQ().add("/register agent Smith");

        client.register();
        agent.register();

        client.findCompanion();
        agent.findCompanion();

        assertEquals(agent, client.getCompanion());
        assertEquals(client, agent.getCompanion());
    }

    @Test
    public void ClientLeaveTest() {
        ChatUserTestImpl client = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);
        ChatUserTestImpl agent = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);

        ChatUserTestImpl anotherClient = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);

        client.getFromUserQ().add("/register client Mary");
        agent.getFromUserQ().add("/register agent Smith");

        anotherClient.getFromUserQ().add("/register client Shone");

        client.register();
        agent.register();
        anotherClient.register();

        client.findCompanion();
        agent.findCompanion();

        anotherClient.findCompanion();

        client.leaveChat("leave");

        assertTrue(client.isFree());
        assertEquals(anotherClient, agent.getCompanion());
    }

    @Test
    public void agentLeaveTest() {
        ChatUserTestImpl client = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);
        ChatUserTestImpl agent = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);

        ChatUserTestImpl anotherAgent = new ChatUserTestImpl(userQueue, agentQueue, clientQueue);

        client.getFromUserQ().add("/register client Mary");
        agent.getFromUserQ().add("/register agent Smith");

        anotherAgent.getFromUserQ().add("/register agent Cooper");

        client.register();
        agent.register();
        anotherAgent.register();

        client.findCompanion();
        agent.findCompanion();
        anotherAgent.findCompanion();

        agent.leaveChat("exit");

        assertTrue(client.isFree());
        assertEquals(anotherAgent, client.getCompanion());
    }

}
