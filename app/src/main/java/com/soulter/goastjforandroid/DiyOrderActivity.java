package com.soulter.goastjforandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class DiyOrderActivity extends AppCompatActivity {


    private HashMap<String,String> map = new HashMap<String,String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_order);

        final EditText orderName = findViewById(R.id.order_name);
        final EditText orderContent = findViewById(R.id.order_content);
        Button orderApply = findViewById(R.id.apply_order);

        final SharedPreferences spfs = this.getSharedPreferences("spfs",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = this.getSharedPreferences("spfs", Context.MODE_PRIVATE).edit();

        orderApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                Gson gson1 = new Gson();
                final String order = spfs.getString("orderData","");
                if (!order.equals("")){
                    map = gson1.fromJson(order, type);
                }

                map.put(orderName.getText().toString(),orderContent.getText().toString());
                //map to json to String
                Gson gson = new Gson();
                String json = gson.toJson(map);

                //步骤3：将获取过来的值放入文件
                editor.putString("orderData",json);
                editor.apply();

//                Intent intent = new Intent(DiyOrderActivity.this,MainActivity.class);
//                intent.putExtra(MainActivity.ORDER_DIALOG_SHOW,1);
//                startActivity(intent);

                editor.putInt("showOrder",1);
                editor.apply();
                Toast.makeText(DiyOrderActivity.this,"编辑成功！",Toast.LENGTH_SHORT).show();
                finish();

                Log.v("json",json);
            }
        });

    }
}
