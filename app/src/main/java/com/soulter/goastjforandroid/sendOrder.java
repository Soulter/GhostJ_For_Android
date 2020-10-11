package com.soulter.goastjforandroid;

public class sendOrder {
    public void send(final String order){

        new Thread(){
            @Override
            public void run() {
                try {

                    SocketManager.bufferedWriter.write(order);
                    SocketManager.bufferedWriter.newLine();
                    SocketManager.bufferedWriter.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
