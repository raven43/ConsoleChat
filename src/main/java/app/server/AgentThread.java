package app.server;

import java.io.IOException;

public class AgentThread extends UserThread {

    AgentThread(UserThread parent){
        super(parent.user,parent.executor);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(user.getName());
        log.debug("Thread \'"+Thread.currentThread().getName()+"\' start");
        chat();
        log.debug("Thread \'"+Thread.currentThread().getName()+"\' stop");
    }

    private void chat(){

        boolean isAvailable = true;

        while (isAvailable) {
            user.findCompanion();
            while (true) {

                String message;
                try {
                    message = user.getMessage();
                }catch (IOException e){
                    user.leaveChat("disconnect");
                    user.off();
                    isAvailable = false;
                    break;
                }
                if(message.contains("/exit")){
                    user.leaveChat("log out");
                    isAvailable = false;
                    break;
                }
                if (!user.isFree()){
                    user.sendToCompanion(message);
                }
            }
        }

        backToUserThread();
    }
}
