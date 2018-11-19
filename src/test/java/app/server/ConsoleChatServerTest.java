//package app.server;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.*;
//import java.net.Socket;
//
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class ConsoleChatServerTest {
//
//    private ChatUser[] user;
//    private Socket[] socket;
//    private ByteArrayInputStream[] in;
//    private ByteArrayOutputStream[] out;
//    private boolean[] isConnected;
//    private boolean[] isClosed;
//
//    private ExecutorService executor;
//    private Queue<ChatUser> userQueue;
//    private Queue<ChatUser> agentQueue;
//    private Queue<ChatUser> clientQueue;
//
//
//    @Before
//    public void setUp()throws IOException{
//
//        userQueue = new ConcurrentLinkedQueue<ChatUser>();
//        agentQueue = new ConcurrentLinkedQueue<ChatUser>();
//        clientQueue = new ConcurrentLinkedQueue<ChatUser>();
//        executor = Executors.newFixedThreadPool(10);
//
//        out = new ByteArrayOutputStream[]{
//                new ByteArrayOutputStream(),
//                new ByteArrayOutputStream()
//        };
//        in = new ByteArrayInputStream[]{
//                new ByteArrayInputStream("/register client C\nhelp request".getBytes("UTF-8")),
//                new ByteArrayInputStream("/register agent A\n".getBytes("UTF-8"))
//        };
//        isConnected = new boolean[]{true,true};
//        isClosed = new boolean[]{false,false};
//
//
//        socket = new Socket[]{
//                new Socket() {
//                    @Override
//                    public InputStream getInputStream() {
//                        return in[0];
//                    }
//
//                    @Override
//                    public OutputStream getOutputStream() {
//                        return out[0];
//                    }
//
//                    @Override
//                    public boolean isConnected() {
//                        return isConnected[0];
//                    }
//
//                    @Override
//                    public boolean isClosed() {
//                        return isClosed[0];
//                    }
//                },
//                new Socket() {
//                    @Override
//                    public InputStream getInputStream(){
//                        return in[1];
//                    }
//
//                    @Override
//                    public OutputStream getOutputStream(){
//                        return out[1];
//                    }
//
//                    @Override
//                    public boolean isConnected() {
//                        return isConnected[1];
//                    }
//
//                    @Override
//                    public boolean isClosed() {
//                        return isClosed[1];
//                    }
//                }
//        };
//        user = new ChatUser[]{
//                new ChatUser(socket[0],userQueue,agentQueue,clientQueue),
//                new ChatUser(socket[1],userQueue,agentQueue,clientQueue),
//        };
//
//    }
//    @After
//    public void clean(){
//        executor.shutdown();
//        user[0].off();
//        user[1].off();
//    }
//
//
//    @Test
//    public void chatUserTest(){
//
//        String message = null;
//        try {
//            message = user[0].getMessage();
//        }catch (EOFException e){
//            e.printStackTrace();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals("some message", message);
//
//    }
//
//    @Test
//    public void registerTest() throws IOException {
//
//
//        executor.submit(new UserThread(user[1],executor));
//        try {Thread.sleep(200);}catch (Exception e){}
//        out[1].reset();
//        try {Thread.sleep(100);}catch (Exception e){}
//
//        executor.submit(new UserThread(user[0],executor));
//        //in[0] = new ByteArrayInputStream("help request".getBytes("UTF-8"));
//
//        try {Thread.sleep(100);}catch (Exception e){}
//
//        Assert.assertEquals(ChatUser.ROLE.CLIENT,user[0].getRole());
//        Assert.assertEquals(ChatUser.ROLE.AGENT,user[1].getRole());
//
//        Assert.assertEquals("C",user[0].getName());
//        Assert.assertEquals("A",user[1].getName());
//
//        Assert.assertEquals(user[1],user[0].getCompanion());
//        Assert.assertEquals(user[0],user[1].getCompanion());
//
//        Assert.assertEquals("u get client C\r\n" +
//                "Client C: help request\r\n",new String(out[1].toByteArray()));
//    }
//
//    @Test
//    public void logOutTest()throws IOException {
//
//        executor.submit(new UserThread(user[0],executor));
//        executor.submit(new UserThread(user[1],executor));
//
//        try {Thread.sleep(200);}catch (Exception e){}
//
//        Assert.assertEquals(ChatUser.ROLE.CLIENT,user[0].getRole());
//        Assert.assertEquals(ChatUser.ROLE.AGENT,user[1].getRole());
//
//        in[0].reset();
//        in[1].reset();
//
//        in[0] = new ByteArrayInputStream("/exit\n".getBytes("UTF-8"));
//        in[1] = new ByteArrayInputStream("/exit\n".getBytes("UTF-8"));
//
//        try {Thread.sleep(200);}catch (Exception e){}
//
//        Assert.assertEquals(ChatUser.ROLE.USER , user[0].getRole());
//        Assert.assertEquals(ChatUser.ROLE.USER , user[1].getRole());
//    }
//
//    @Test
//    public void newMessgeTest()throws IOException{
//
//        ByteArrayInputStream tmpIn0 = in[0];
//        ByteArrayInputStream tmpIn1 = in[1];
//
//        registerTest();
//
//        Assert.assertEquals(in[0],tmpIn0);
//        Assert.assertEquals(in[1],tmpIn1);
//    }
//
//
//}
