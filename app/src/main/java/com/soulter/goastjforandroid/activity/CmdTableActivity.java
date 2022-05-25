package com.soulter.goastjforandroid.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soulter.goastjforandroid.service.ConnService;
import com.soulter.goastjforandroid.pojo.MessageFilterField;
import com.soulter.goastjforandroid.R;
import com.soulter.goastjforandroid.util.SocketManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class CmdTableActivity extends AppCompatActivity {


    private MyReceiver myReceiver4;
    private IntentFilter intentFilter;
    private TextView okok;
    private EditText inputOrder;
    private Button sendOrder;
    private Button eIWow;
    private Button eIWowDb;
    private Button eIUp;
    private Button eIDown;
    private Button msgFilter;
    private Button chatBtn;
    private ScrollView mScrollView;

    private ArrayList<String> orderList = new ArrayList<>();
    private ArrayList<String> autoCompList = new ArrayList<>();


    private ArrayList<String> clientsName = new ArrayList<>();
    private ArrayList<String> clientsNum = new ArrayList<>();
    private String focusingClient = "";

    ArrayList<String> autoCompResult = new ArrayList<>();
    private int orderIndex = 0;
    private int downTag = 0;

    //    public static String ORDER_DIALOG_SHOW = "order_dialog_show";
    public static String CLIENT_DATA_NAME_SEC = "client_name_sec";
    public static String CLIENT_DATA_NUM_SEC = "client_num_sec";
    public static String ALL_MESSAGE_TAG = "all_of_the_message";

    public static final int REQUEST_FOCUS_CLIENT_CODE = 1;


    Intent mIntent;

    String msgAll = "";
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();


    int msgChatTag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd_table);




        myReceiver4 = new MyReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnService.ACTION_NAME);
        //注册广播
        registerReceiver(myReceiver4, intentFilter);

        okok = findViewById(R.id.okok);
        inputOrder = findViewById(R.id.input_order);
        sendOrder = findViewById(R.id.send_order);

        eIWow = findViewById(R.id.easy_input_wow);
        eIWowDb = findViewById(R.id.easy_input_wow_db);
        eIUp = findViewById(R.id.easy_input_up);
        eIDown = findViewById(R.id.easy_input_down);
        msgFilter = findViewById(R.id.msg_filter_entry);
        chatBtn = findViewById(R.id.chat_entry);
        mScrollView = findViewById(R.id.scroll_view);

        mIntent = new Intent(CmdTableActivity.this,ConnService.class);
        startService(mIntent);

        msgAll = getIntent().getStringExtra(MainActivity.TO_CMD_ACTIVITY_OKOK)+"\n---------以上是历史信息---------\n";
        spannableStringBuilder.append(msgAll);
        if (!msgAll.equals(""))
            okok.setText(msgAll);
        showAutoComplete();

        sendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String order = inputOrder.getText().toString();

                            if (!order.equals("")){
                                spannableStringBuilder.append("\n>>>"+order);
//                                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#3E8E8D")),msgAll.length(),msgAll.length()+order.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                msgAll += "\n>>>"+order;

                                orderList.add(order);
                                orderIndex = orderList.size();
                                if (msgChatTag == 1){
                                    sendOrder("!echo "+order);
                                }else {
                                    sendOrder(order);
                                }


                            }


                        }catch (Exception e){
                            e.getStackTrace();
                        }
                    }
                }).start();


            }
        });

        //快捷输入
        eIWow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOrder.setText("!");
                inputOrder.setSelection(1);
            }
        });

        eIWowDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOrder.setText("!!");
                inputOrder.setSelection(2);
            }
        });

        eIUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (orderList.size() != 0 && orderIndex > 0){
                    orderIndex -= 1;
                    inputOrder.setText(orderList.get(orderIndex));
                    inputOrder.setSelection(orderList.get(orderIndex).length());
                    downTag = 0;
                }else {
                    Toast.makeText(CmdTableActivity.this,"到顶啦",Toast.LENGTH_SHORT).show();
                }

            }
        });

        eIDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (orderList.size() != 0 && orderIndex < orderList.size()-1){
                    orderIndex += 1;
                    inputOrder.setText(orderList.get(orderIndex));
                    inputOrder.setSelection(orderList.get(orderIndex).length());

                }else {
                    if (orderIndex != orderList.size() && downTag == 0){
                        inputOrder.setText("");
                        if (orderList.size()!=0){
                            orderIndex += 1;
                        }
                        downTag = 1;

                    }

                }
            }
        });



        msgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CmdTableActivity.this, MessageFilterField.class);
                intent.putExtra(ALL_MESSAGE_TAG,msgAll);
                startActivity(intent);
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (msgChatTag == 0){
                    inputOrder.setHint("聊天...");
                    chatBtn.setText("还原");
                    msgChatTag = 1;
                    chatDisplay();
                }else {
                    inputOrder.setHint("输入操作...");
                    chatBtn.setText("聊天");
                    msgChatTag = 0;
                    okok.setText(msgAll);
                }

            }
        });


        okok.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                diyOrderDialog();
                return false;
            }
        });


        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mScrollView.post(new Runnable() {
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });

    }


    @SuppressWarnings("unchecked")
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //获取从Service中传来的data
                    int codeid = intent.getIntExtra(ConnService.CODE,0);
                    final String msg = intent.getStringExtra(ConnService.COUNTER);
                    //处理登录异常  codeid=1 时是登陆异常的情况。
                    if (codeid == 1){
                        try{
                            SocketManager.bufferedWriter.write("#close#");
                            SocketManager.bufferedWriter.newLine();
                            SocketManager.bufferedWriter.flush();

                            SocketManager.socket.close();
                            SocketManager.socket=null;
                            SocketManager.inputStreamReader=null;
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        Intent intent2 = new Intent(CmdTableActivity.this, LoginActivity.class);
                        intent2.putExtra(LoginActivity.INTENT_EXTRA,msg);
                        startActivity(intent2);
                        stopService(mIntent);
                        finish();
                    }else if (codeid == 2)
                    {
//                            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
//                            builder.setTitle("新截图")
//                                    .setMessage("是否预览？")
//                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            showScrDialog(msg);
//
//                                        }
//                                    })
//                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                                        }
//                                    }).create().show();
                    } else if(codeid == 3)// clientsInfo
                    {
//                        clientsFields = null;

                        clientsName.clear();
                        clientsNum.clear();
                        clientsName = intent.getStringArrayListExtra(ConnService.COUNTER_CLIENT_NAME);
                        clientsNum = intent.getStringArrayListExtra(ConnService.COUNTER_CLIENT_NUM);
                        focusingClient = intent.getStringExtra(ConnService.COUNTER_FOCUSING);
//                        clientFocus.setText("聚焦("+clientsNum.size()+")");
//                        clientsFields = Arrays.asList(intent.getStringExtra(ConnService.COUNTER).substring(1,intent.getStringExtra(ConnService.COUNTER).length()-1).split(", "));
                    }else if (codeid == 0){
                        Log.v("tag",codeid+"\n"+msg);
                        spannableStringBuilder.append("\n<<<"+msg);
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#3E8E8D")),msgAll.length(),msgAll.length()+msg.length()+4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        spannableStringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC),msgAll.length(),msgAll.length()+msg.length()+4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        msgAll += "\n<<<"+msg;
                        if (msgChatTag == 1){
                            chatDisplay();
                        }else{
                            okok.setText(spannableStringBuilder);
                        }


                    }





                }
            });
        }
    }

    public void chatDisplay(){

        String msgChatStr = "";
        String[] msgChats = msgAll.split("\n");
        for (String line : msgChats){
            Log.v("lwl_tag_easyInput_debug","msgChats_line:"+line);
            if (line.indexOf("[TransCmd-echo]") == 0){
                msgChatStr += "\n"+line;
                okok.setText(msgChatStr);
            }

        }
        if (msgChatStr.equals("")){
            okok.setText("           \n\n\n\n此地无银");
        }

    }

    public void showAutoComplete(){
        final ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAnchorView(inputOrder);
        autoCompList.add("!focus ");
        autoCompList.add("!exit ");
        autoCompList.add("!!rft upload ");
        autoCompList.add("dir ");
        autoCompList.add("!list");
        autoCompList.add("!echo ");


        listPopupWindow.setAdapter(new ArrayAdapter<String>(this,R.layout.auto_complete_layout,autoCompResult));
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inputOrder.setText(autoCompList.get(position));
                inputOrder.setSelection(autoCompList.get(position).length());
                listPopupWindow.dismiss();//如果已经选择，隐藏listpopwindow
            }
        });

        inputOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {

                autoCompResult.clear();
                String textt = inputOrder.getText().toString();
                if (textt.length() > 0) {

                    Log.v("popup","onTextChanged()");

                    for (int loop = 0; loop < autoCompList.size(); loop++){
                        if (autoCompList.get(loop).indexOf(textt) == 0){
                            autoCompResult.add(autoCompList.get(loop));
                        }
                    }
                    if(autoCompResult.size() != 0){
                        listPopupWindow.show();
                    }
                }else if (listPopupWindow.isShowing()){
                    listPopupWindow.dismiss();
                }
            }
        });
    }





    public void showScrDialog(String url){
//        android.app.AlertDialog.Builder webDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
//        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
//        View webview = inflater.inflate(R.layout.web_message,null);
//        WebView webMessage = (WebView)webview.findViewById(R.id.web_message);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            webMessage.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }
//        webMessage.getSettings().setBlockNetworkImage(false);
//        webMessage.loadUrl(url);
//        webDialogBuilder.setView(webview);
//        webDialogBuilder.show();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(myReceiver4);

        super.onDestroy();

    }


    public void sendOrder(String order){
        try {
            SocketManager.bufferedWriter.write(order);
            SocketManager.bufferedWriter.newLine();
            SocketManager.bufferedWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    @Override
    protected void onResume() {
        SharedPreferences spfs = this.getSharedPreferences("spfs",Context.MODE_PRIVATE);

        Log.v("tag",String.valueOf(spfs.getInt("showOrder",0)));
        if (spfs.getInt("showOrder",0) == 1){
            diyOrderDialog();
            SharedPreferences.Editor editor = this.getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();
            editor.putInt("showOrder",0);
            editor.apply();
        }
        super.onResume();
    }


    public void diyOrderDialog(){


        //spfs->String ,Sring to Json to Hashmap to String[]
        SharedPreferences spfs = this.getSharedPreferences("spfs",Context.MODE_PRIVATE);
        final String json = spfs.getString("orderData","");

        final String[] orderNameStrings;

        int defOrderCount = 2;

        if (json.equals("")){
            orderNameStrings = new String[defOrderCount];
            orderNameStrings[0] = "自定义命令";
            orderNameStrings[1] = "让Client说话";

            final AlertDialog.Builder builder = new AlertDialog.Builder(CmdTableActivity.this);
            builder.setTitle("自定义命令：")
                    .setItems(orderNameStrings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                Intent intent = new Intent(CmdTableActivity.this,DiyOrderActivity.class);
                                startActivity(intent);
                            }
                            if (i == 1){
                                inputOrder.setText("mshta vbscript:createobject(\"sapi.spvoice\").speak(\"Hello\")(window.close)");
                            }
                        }
                    }).create().show();
        }else{

            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            Gson gson = new Gson();
            final HashMap<String,String> map = gson.fromJson(json, type);
            orderNameStrings= new String[map.size() + defOrderCount];
            orderNameStrings[0] = "自定义命令";
            orderNameStrings[1] = "让Client说话";

            Log.v("mapSize",String.valueOf(map.size()));
//            Log.v("map", map.toString());
//            Log.v("keySet", map.keySet().toString());
            int index = defOrderCount;
            for(String entry : map.keySet()) {
                orderNameStrings[index]=entry;
                index++;
                Log.v("entry",entry);
            }
//            for(String entry1 : map.values()) {
//                Log.v("entry1",entry1);
//            }

            Log.v("najs",""+orderNameStrings[2]);
            final AlertDialog.Builder builder = new AlertDialog.Builder(CmdTableActivity.this);
            builder.setTitle("自定义命令：")
                    .setItems(orderNameStrings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                Intent intent = new Intent(CmdTableActivity.this,DiyOrderActivity.class);
                                startActivity(intent);
                            }else if (i == 1){
                                inputOrder.setText("mshta vbscript:createobject(\"sapi.spvoice\").speak(\"Hello\")(window.close)");
                            }else
                                inputOrder.setText(""+map.get(orderNameStrings[i]));
                        }
                    }).create().show();
        }


    }




}
