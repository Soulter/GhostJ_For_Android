package com.soulter.goastjforandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.InputStream;

public class ConnService extends Service {

    public static final String COUNTER = "msg";
    public static final String CODE = "codeID";
    public static final String ACTION_NAME = " com.soulter.goastjforandroid.ConnService.COUNTER_ACTION";

    InputStream inputStream = null;


    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                int c;
                try {
                    int loopagain = 0;
                    while (loopagain == 0) {
                        try{
                            c=SocketManager.inputStreamReader.read();
//
//                            final byte[] buffer = new byte[2048];
//                            inputStream = SocketManager.socket.getInputStream();
//                            final int len = inputStream.read(buffer);
//                            if (len > 0){
//                                Log.v("testing",new String(buffer,0,len));
//                                bridgeForActivity(new String(buffer,0,len),0);
//                            }

                            //指令检测
                            if((char)c=='!'){
                                StringBuffer cmds=new StringBuffer("!");
                                while(true){
                                    int c0=SocketManager.inputStreamReader.read();
                                    if((char)c0=='!') {
                                        cmds.append("!");
                                        break;
                                    }
                                    if ((char)c0=='\n') {
                                        cmds.append("\n");
                                        break;
                                    }
                                    cmds.append((char)c0);
                                }
                                String cmd[]=cmds.toString().substring(0,cmds.length()-1).split(" ");
                                Log.v("TAG","cmds:  "+ cmds.toString());
                                Log.v("TAG",cmd[0]);
                                switch (cmd[0]){
                                    case "!relogin":{
                                        loopagain = 1;
                                        bridgeForActivity("被强制下线",1);
                                        break;
                                    }
                                    case "!alivem":{
                                        Log.v("TAG","case !alivem");
                                        SocketManager.bufferedWriter.write("#alivem#");
                                        SocketManager.bufferedWriter.newLine();
                                        SocketManager.bufferedWriter.flush();
                                        Log.v("TAG","send #alivem#");
                                        continue;
                                    }
//                                    case "!alivems":{
//                                        MasterMain.checkServerAlive.alive=true;
//                                        continue;
//                                    }
                                    case "!passErr":{
                                        loopagain = 1;
                                        bridgeForActivity("密码错误",1);
                                        break;
                                    }
                                    case "!clients":{//获取到最新列表
//                                        MasterMain.initGUI.clientTable.clients.clear();
//                                        for(int i=1;i<cmd.length;i+=6){
//                                            ClientTable.clientInfo clientInfo=new ClientTable.clientInfo();
//                                            clientInfo.id=Long.parseLong(cmd[i]);
//                                            clientInfo.name=cmd[i+1];
//                                            clientInfo.connTime=Long.parseLong(cmd[i+2]);
//                                            clientInfo.status=Boolean.parseBoolean(cmd[i+3]);
//                                            clientInfo.version= cmd[i + 4];
//                                            clientInfo.sysStartTime=Long.parseLong(cmd[i+5]);
//                                            MasterMain.initGUI.clientTable.clients.add(clientInfo);
//                                        }
//                                        MasterMain.initGUI.clientTable.tableStart=MasterMain.initGUI.clientTable.tableStart+5>MasterMain.initGUI.clientTable.clients.size()?0:MasterMain.initGUI.clientTable.tableStart;
//                                        MasterMain.initGUI.clientTable.updateCom();
//                                        bridgeForActivity(cmds.toString(),0);
                                        continue;
                                    }
                                    case "!taglog":{
//                                        try {
//                                            FileRW.write("tagLog.json", cmd[1]);
//                                            MasterMain.tagLog.load();
//                                            MasterMain.initGUI.onlineTimeChart.repaint();
//                                        }catch (Exception e){
//                                            Out.say("HandleConn","获取tagLog失败");
//                                            e.printStackTrace();
//                                        }
                                        continue;
                                    }
                                    case "!finish":{
//                                        BatProcess.masterProcess();

                                        continue;
                                    }
                                    default:{
//                                        Out.sayThisLine(cmds.toString());
                                        Log.v("TAG","default "+cmds.toString());
                                        bridgeForActivity(cmds.toString(),0);
                                        continue;
                                    }
                                }
                            }//指令检测完毕




                            Log.v("TAG",String.valueOf((char)c));
                            bridgeForActivity(String.valueOf((char)c),0);

                        }catch (Exception e){
//                            Out.say("HandleConn","接受数据出错，连接正在重置");
                            e.printStackTrace();
                            bridgeForActivity("连接已断开",1);
                            loopagain = 1;
                            Thread.interrupted();
//                            MasterMain.initGUI.bgp.setVisible(false);
                        }

                        if (loopagain == 1){
                            Log.v("TAG","nomoreagain");
                            break;
                        }
                    }
                }catch (Exception e){
//                    Out.say("HandleConn","处理连接数据时出错 非致命错误");
                    e.printStackTrace();
                }
            }
        }).start();


        return super.onStartCommand(intent, flags, startId);

    }

    public void bridgeForActivity(String msg,int code){



        Log.v("TAG","bfa:"+msg);
        Intent intent = new Intent();
        intent.setAction(ACTION_NAME);
        intent.putExtra(COUNTER,msg);
        intent.putExtra(CODE,code);
        sendBroadcast(intent);

    }


}
