package app.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ClientThread extends UserThread {

    ClientThread(@NotNull UserThread parent) {
        super(parent.user, parent.executor);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(user.getName());
        log.debug("Thread \'" + Thread.currentThread().getName() + "\' start");
        chat();
        log.debug("Thread \'" + Thread.currentThread().getName() + "\' stop");
    }

    private void chat() {
        boolean isRunning = true;
        while (isRunning) {
            String startMessage;
            try {
                startMessage = user.getMessage();
            } catch (IOException e) {
                user.leaveChat("disconnect");
                user.off();
                break;
            }
            if (startMessage.contains("/exit")) {
                log.info("client " + user.getName() + " log out");
                break;
            }
            user.getMessageCash().add(startMessage);

            user.findCompanion();


            while (true) {
                String message;
                try {
                    message = user.getMessage();
                } catch (IOException e) {
                    user.leaveChat("disconnect");
                    user.off();
                    isRunning = false;
                    break;
                }
                if (message.contains("/leave")) {
                    user.leaveChat("leave chat");
                    break;
                }
                if (message.contains("/exit")) {
                    user.leaveChat("log out");
                    isRunning = false;
                    break;
                }
                if (user.isFree())
                    user.getMessageCash().add(message);
                else
                    user.sendToCompanion(message);
            }
        }
        backToUserThread();
    }
}