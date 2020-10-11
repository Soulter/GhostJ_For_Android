package com.soulter.goastjforandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileExplorerActivity extends AppCompatActivity {
    private MyReceiver2 myReceiver;
    private IntentFilter intentFilter;
    private ArrayList<String> fileName = new ArrayList<>();
    private ArrayList<String> fileSize = new ArrayList<>();
    private ArrayList<FilesField> filesFields = new ArrayList<>();
    private String pathNow = "";
    private TextView pathTv;
    private Button filePreBtn;
    FilesListAdapter filesListAdapter;
    private ProgressBar progressBar;

    private ListView listView;


    String atomCanliu = "";

    sendOrder sendOrder = new sendOrder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);




        myReceiver = new MyReceiver2();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_NAME);
        listView = findViewById(R.id.files_list);
        pathTv = findViewById(R.id.file_path_tv);
        filePreBtn = findViewById(R.id.file_ope_previous);
        progressBar = findViewById(R.id.progressbar_file);
        //注册广播
        registerReceiver(myReceiver, intentFilter);

        sendOrder.send("!!rfe dir");
        progressBar.setVisibility(View.VISIBLE);



        filePreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOrder.send("!!rfe cd ..");
                sendOrder.send("!!rfe dir");
                progressBar.setVisibility(View.VISIBLE);
            }
        });



        filesFields.add(new FilesField("","",0));
        filesListAdapter = new FilesListAdapter(FileExplorerActivity.this, R.layout.file_item,filesFields);
        listView.setAdapter(filesListAdapter);
        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
//                                // 将先要传回的数据放到Intent里
//                                // 可以用putExtra()的方法，也可以用setXXX()的方法
//                                Intent intent = new Intent();
//                                intent.putExtra("client_name_focus", clientsName.get(position));
//                                // 设置返回码和返回携带的数据
//                                setResult(Activity.RESULT_OK, intent);
//                                // RESULT_OK就是一个默认值，=-1，它说OK就OK吧
//                                finish();
                if (filesFields.get(position).getIsDict() == 1){
                    sendOrder.send("!!rfe cd "+filesFields.get(position).getfileName());
                    sendOrder.send("!!rfe dir");
                    progressBar.setVisibility(View.VISIBLE);
                }else{
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FileExplorerActivity.this);
                    builder
                            .setMessage("是否上传？")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sendOrder.send("!!rfe upload "+filesFields.get(position).getfileName()+ " " +filesFields.get(position).getfileName());
                                    Toast.makeText(FileExplorerActivity.this,"开始上传...",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create().show();
                }
                Log.v("click!",filesFields.get(position).getfileName());
            }
        });
    }



    @SuppressWarnings("unchecked")
    class MyReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //获取从Service中传来的data
                    int codeid = intent.getIntExtra(ConnService.CODE,0);
                    final String msg = intent.getStringExtra(ConnService.COUNTER);

                    if (msg != null){
                        if (msg.contains("[FileReceiveEvent]")){
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FileExplorerActivity.this);
                            builder.setTitle("告知")
                                    .setMessage(msg)
                                    .setPositiveButton("了解", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }).show();
                        }
                    }

                    //处理登录异常  codeid=1 时是登陆异常的情况。
                    //4 is filesMsg
                    if (codeid == 4){
                        //检测到file数据

                        Log.v("mr2",msg+codeid);
                        fileName.clear();
                        fileSize.clear();




//                        String fileData = msg.replace("!reDir ","");
                        String fileData =  msg.substring(msg.indexOf("!reDir ")+7);
                        Log.v("fileData",fileData);
                        String[] fileDataTemp = fileData.split("\n");

                        String[] fileDataList = fileDataTemp[0].split("\\|"); //未经加工的FileData

                        pathNow = fileDataList[0];

                        pathTv.setText(pathNow);

                        String[] atomFile;
                        int atomIsDic;
                        filesFields.clear();
                        if (fileDataList.length>1){
                            for (int i = 1; i < fileDataList.length ; i++){
                                atomFile = fileDataList[i].split("\\:");
                                Log.v("fileAtom",atomFile.length+"");
                                if (atomFile.length == 3){
                                    if (atomFile[1].equals("true")){
                                        atomIsDic = 1; //is dic.
                                    }else{
                                        atomIsDic = 0;
                                    }

                                    FilesField filesField =  new FilesField(atomFile[0],String.valueOf(Long.valueOf(atomFile[2])/1024)+" KB",atomIsDic);
                                    filesFields.add(filesField);
                                    Log.v("testingFile",atomFile[0]+atomFile[2]+"   "+atomIsDic);
                                }
//                                else{
//                                    atomCanliu += fileDataList[i];
//                                    Log.v("atomcanliu",atomCanliu);
//                                    atomFile = fileDataList[i].split("\\:");
//                                    if (atomFile.length == 3){
//                                        atomCanliu = "";
//                                        if (atomFile[1].equals("true")){
//                                            atomIsDic = 1; //is dic.
//                                        }else{
//                                            atomIsDic = 0;
//                                        }
//
//                                        FilesField filesField =  new FilesField(atomFile[0],String.valueOf(Long.valueOf(atomFile[2])/1024)+" KB",atomIsDic);
//                                        filesFields.add(filesField);
//                                        Log.v("testingFile",atomFile[0]+atomFile[2]+"   "+atomIsDic);
//                                    }
//                                }

                            }
                            filesListAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
//                            listView.setAdapter(filesListAdapter);
                        }else{
                            FilesField filesField =  new FilesField("空文件夹","空文件夹",0);
                            filesFields.add(filesField);
                            filesListAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
                        }


                    }

                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }
}
