package com.soulter.goastjforandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientsFieldActivity extends AppCompatActivity {

    private List<ClientsField> clientsFields = new ArrayList<>();
    private ArrayList<String> clientsName = new ArrayList<>();
    private ArrayList<String> clientsNum = new ArrayList<>();
    private TextView titleName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ciients_field);

//        clientsFields = Arrays.asList(getIntent().getStringExtra(MainActivity.CLIENT_DATA).substring(1,getIntent().getStringExtra(MainActivity.CLIENT_DATA).length()-1).split(", "));
//        ClientsListAdapter = new ClientsListAdapter(this, R.layout.clients_item, clientsFields);

        titleName = findViewById(R.id.title_clients_activity);

        clientsName = getIntent().getStringArrayListExtra(MainActivity.CLIENT_DATA_NAME_SEC);
        clientsNum = getIntent().getStringArrayListExtra(MainActivity.CLIENT_DATA_NUM_SEC);
        initClientData(clientsName, clientsNum);

        titleName.setText("建立的连接"+"("+clientsName.size()+"):");

        ClientsListAdapter clientsListAdapter = new ClientsListAdapter(this, R.layout.clients_item,clientsFields);

        ListView listView = findViewById(R.id.clients_list);
        listView.setAdapter(clientsListAdapter);

        // 为ListView注册一个监听器，当用户点击了ListView中的任何一个子项时，就会回调onItemClick()方法
        // 在这个方法中可以通过position参数判断出用户点击的是那一个子项
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 将先要传回的数据放到Intent里
                // 可以用putExtra()的方法，也可以用setXXX()的方法
                Intent intent = new Intent();
                intent.putExtra("client_name_focus", clientsName.get(position));
                // 设置返回码和返回携带的数据
                setResult(Activity.RESULT_OK, intent);
                // RESULT_OK就是一个默认值，=-1，它说OK就OK吧
                finish();
            }
        });
    }

    public void initClientData(ArrayList<String> clientsName, ArrayList<String> clientsNum){
        if (clientsName.size() == clientsNum.size()){
            for (int i = 0; i<clientsName.size(); i++){
                ClientsField clientsField = new ClientsField(clientsName.get(i), clientsNum.get(i));
                Log.v("initClientData",clientsName.get(i) + " " + clientsNum.get(i));
                clientsFields.add(clientsField);
            }
        }

    }
}
