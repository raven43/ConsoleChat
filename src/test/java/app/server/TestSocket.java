package app.server;

import java.io.*;
import java.net.Socket;

public class TestSocket extends Socket {

    private boolean close = false;
    private boolean connected = true;


    private ByteArrayInputStream inputStream;
    private ByteArrayOutputStream outputStream;

    public TestSocket(String message) throws IOException{
        inputStream = new ByteArrayInputStream(message.getBytes("UTF-8"));
        outputStream = new ByteArrayOutputStream();


        outputStream.toByteArray();
    }

    public void reWriteMessage(String message) throws IOException{
        inputStream = new ByteArrayInputStream(message.getBytes("UTF-8"));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream;
    }


    @Override
    public boolean isClosed() {
        return close;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public synchronized void close() throws IOException {
        close = false;
    }
}
