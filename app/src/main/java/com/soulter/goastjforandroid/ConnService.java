package com.soulter.goastjforandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnService extends Service {

    public static final String COUNTER = "msg";
    public static final String CODE = "codeID";
    public static final String COUNTER_CLIENT_NAME = "count_client_name";
    public static final String COUNTER_CLIENT_NUM = "count_client_num";
    public static final String COUNTER_FOCUSING = "count_focusing";
    public static final String ACTION_NAME = " com.soulter.goastjforandroid.ConnService.COUNTER_ACTION";

    public static Timer msgCollectTimer = new Timer();
    public static StringBuilder msgCache = new StringBuilder();

    private static final ArrayList<String> clientsName = new ArrayList<>();
    private static final ArrayList<String> clientsNum = new ArrayList<>();
    private static String focusingClient = "";

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {

        setMsgGetter();
        setMsgCollectTimer();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    int loopagain = 0;
//                    while (loopagain == 0) {
//                        try{
//
//                        }catch (Exception e){
////                            Out.say("HandleConn","接受数据出错，连接正在重置");
//                            e.printStackTrace();
//                            bridgeForActivity("连接已断开",1);
//                            loopagain = 1;
//                            Thread.interrupted();
//                        }
//
//                        if (loopagain == 1){
//                            break;
//                        }
//                    }
//                }catch (Exception e){
//                    System.out.println("处理连接数据时出错 非致命错误");
//                    e.printStackTrace();
//                }
//            }
//        }).start();


        return super.onStartCommand(intent, flags, startId);

    }

    public static void setMsgGetter(){
        Thread msgGetter = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        BufferedReader reader = new BufferedReader(SocketManager.inputStreamReader);
                        String temp;
                        while ((temp = reader.readLine())!=null){
                            System.out.println(temp);
                            msgCache.append(temp).append("\n");
                        }
                    }catch (Exception e){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }

            }
        });
        msgGetter.start();
    }

    public void setMsgCollectTimer(){
        msgCollectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!msgCache.toString().equals("")){
                    try {
                        msgAnalyzer(msgCache.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    msgCache.delete(0, msgCache.length());
                }
            }
        }, 500, 1000);
    }

    public void msgAnalyzer(String result) throws IOException{
        result = msgFilter(result);
        Pattern patternUrl = Pattern.compile("http://[^\\s]*",Pattern.MULTILINE|Pattern.DOTALL);
        final Matcher macherUrl = patternUrl.matcher(result);
        while (macherUrl.find()){
            bridgeForActivity(macherUrl.group(),2);
        }

        if (result.contains("[FileReceiveEvent]成功")&&result.contains("audio.wMASKav")){
            bridgeForActivity("get",6);
        }

        String[] msgList = result.split("\n");
        System.out.println(Arrays.toString(msgList));
        StringBuilder r = new StringBuilder();

        for (String msgItem: msgList){
            if (msgItem.contains("[FileReceiveEvent]接收")){
                msgItem = "一个接收事件启动.";
            }
            if (msgItem.contains("[FileReceiveEvent]成功")){
                msgItem = "一个接收事件完成.";
            }
            if (msgItem.contains("[HandleConn]广播")){
                msgItem = msgItem.replace("[HandleConn]广播", "服务器下发信息: ");
            }
            r.append(msgItem).append("\n");
        }

        if (!result.equals("")){
            bridgeForActivity(r.toString(),0);
        }

    }

    public String msgFilter(String result) throws IOException {
        //Clients
        if (result.contains("!clients ")){
            try{
                String clientData = result.substring(result.indexOf("!clients ") + 1, result.lastIndexOf("!"));
                clientsName.clear();
                clientsNum.clear();

                String[] clientList = clientData.split(" "); //未经加工的ClientData
                for (int i = 0; i < clientList.length/6 ; i++){
                    clientsName.add(clientList[i*6+2]);
                    clientsNum.add(clientList[i*6+1]);
                    if (clientList[i*6+4].equals("true")){
                        focusingClient = clientList[i*6+2];
                    }
                }
                bridgeForActivity("",3); //3 是发送心跳给Mainactivity，告知它有了新的client变动
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        Pattern pattern = Pattern.compile("\\!.*\\!");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()){
            String matcherText = matcher.group();
            switch (matcherText){
                case "!alivem!":{
                    SocketManager.bufferedWriter.write("#alivem#");
                    SocketManager.bufferedWriter.newLine();
                    SocketManager.bufferedWriter.flush();
                    result = matcher.replaceAll("");  // REPLACE
                    break;
                }
                case "!passErr!":{
                    result = matcher.replaceAll("");  // REPLACE
                    bridgeForActivity("密码错误",1);
                }
                case "!relogin!":{
                    result = matcher.replaceAll("");  // REPLACE
                    bridgeForActivity("被强制下线",1);
                }

                case "!finish!":{
                    result = matcher.replaceAll("");  // REPLACE
                    bridgeForActivity("连接已断开",1);
                }
                default:{
                    result = matcher.replaceAll("");  // REPLACE
                    break;
                }
            }

        }
        if(result.contains("!reDir ")){
            bridgeForActivity(result,4);
            result = "";
        }
        return result;
    }

    public void bridgeForActivity(String msg,int code){
        Intent intent = new Intent();
        intent.setAction(ACTION_NAME);
        if (code == 3){ // clientsInfo
            intent.putExtra(CODE,code);
            intent.putExtra(COUNTER_CLIENT_NAME,clientsName);
            intent.putExtra(COUNTER_CLIENT_NUM,clientsNum);
            intent.putExtra(COUNTER_FOCUSING,focusingClient);
        }else{
            intent.putExtra(COUNTER,msg);
            intent.putExtra(CODE,code);
        }
        sendBroadcast(intent);

    }




}
