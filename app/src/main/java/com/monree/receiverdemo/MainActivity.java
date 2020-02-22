package com.monree.receiverdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button getLocationBtn;
    TextView locationTv;
    IntentFilter filter;
    SmsReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocationBtn = findViewById(R.id.GetLocationButton);
        locationTv = findViewById(R.id.LocationTextView);
        filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiver = new SmsReceiver();
        registerReceiver(receiver,filter);
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},1);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECEIVE_SMS},2);
                }
            }
        });
    }

    public class SmsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            StringBuilder content = new StringBuilder();
            String sender = null;
            Bundle bundle = intent.getExtras();
            String format = intent.getStringExtra("format");
            if(bundle!=null){
                Object[] pdus = (Object[])bundle.get("pdus");
                for(Object object : pdus){
                    SmsMessage message = SmsMessage.createFromPdu((byte[])object,format);
                    sender = message.getOriginatingAddress();
                    content.append(message.getMessageBody());
                }
            }
            if(content.toString().contains("1908")){
                try{
                    Toast.makeText(MainActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                    sendSMSS(sender);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,"权限不足",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void sendSMSS(String phoneNumber) {
        String content = locationTv.getText().toString().trim();
        String phone = phoneNumber;
        if (!content.isEmpty()&&!phone.isEmpty()) {
            SmsManager manager = SmsManager.getDefault();
            ArrayList<String> strings = manager.divideMessage(content);
            for (int i = 0; i < strings.size(); i++) {
                manager.sendTextMessage(phone, null, content, null, null);
            }
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "手机号或内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {
                    Toast.makeText(this,"你取消了授权",Toast.LENGTH_SHORT).show();
                }
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //这里写操作 如send（）； send函数中New SendMsg （号码，内容）；
                } else {
                    Toast.makeText(this, "你取消了授权", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
