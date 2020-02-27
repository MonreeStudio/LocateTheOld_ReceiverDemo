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
import android.graphics.RenderNode;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button getLocationBtn;
    TextView locationTv;
    IntentFilter filter;
    SmsReceiver receiver;
    String locationInfo;
    String senderPhoneNum;
    Object object;
    Context context;
    public LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        setContentView(R.layout.activity_main);
        object = new Object();
        context = getApplicationContext();
        getLocationBtn = findViewById(R.id.GetLocationButton);
        locationTv = findViewById(R.id.LocationTextView);
        locationInfo = "";
        filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiver = new SmsReceiver();
        registerReceiver(receiver, filter);
        //requestLocation();
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},1);
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECEIVE_SMS},2);
            permissionList.add(Manifest.permission.RECEIVE_SMS);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {

        }

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> list = new ArrayList<>();
                list = getLngAndLat(context);
                locationInfo = list.get(1)+list.get(2)+list.get(0);
                locationTv.setText(locationInfo);
                //requestLocation();
            }
        });
    }

    private void requestLocation() {
        initLocation();
        //if (mLocationClient.isStarted())
        //    mLocationClient.stop();
        if (!mLocationClient.isStarted())
            mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        unregisterReceiver(receiver);
    }

    public class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            StringBuilder content = new StringBuilder();
            String sender = null;
            Bundle bundle = intent.getExtras();
            String format = intent.getStringExtra("format");
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object object : pdus) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) object, format);
                    sender = message.getOriginatingAddress();
                    senderPhoneNum = sender;
                    content.append(message.getMessageBody());
                }
            }
            if (content.toString().contains("1908")) {
                try {
                    List<String> list;
                    list = getLngAndLat(context);
                    locationInfo = list.get(1)+list.get(2)+list.get(0);
                    locationTv.setText(locationInfo);
                    //requestLocation();
                    //mLocationClient.stop();
                    //while (locationTv.getText().equals("这里显示位置信息"));
                    //Toast.makeText(MainActivity.this,locationInfo,Toast.LENGTH_SHORT).show();
                    sendSMSS(sender);
                    //mLocationClient.stop();
                    //Toast.makeText(MainActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "回复失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private List<String> getLngAndLat(Context context) {
        List<String> list = new ArrayList<>();
        double latitude = 0;
        double longitude = 0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  //从gps获取经纬度
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return new ArrayList<>();
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                list.add("定位方式：GPS");
            } else {//当GPS信号弱没获取到位置的时候又从网络获取
                return getLngAndLatWithNetwork();
            }
        } else {    //从网络获取经纬度
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return new ArrayList<>();
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                list.add("定位方式：网络");
            }
        }
        list.add("纬度：" + latitude +'\n');
        list.add("经度：" + longitude+"\n");
        return list;
    }

    //从网络获取经纬度
    public List<String> getLngAndLatWithNetwork() {
        List<String> list = new ArrayList<>();
        list.add("定位方式：网络");
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new ArrayList<>();
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        list.add("纬度：" + latitude +'\n');
        list.add("经度：" + longitude+"\n");
        return list;
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub

        }

    };

    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
                    currentPosition.append("经度：").append(location.getLongitude()).append("\n");
                    currentPosition.append("国家：").append(location.getCountry()).append("\n");
                    currentPosition.append("省：").append(location.getProvince()).append("\n");
                    currentPosition.append("市：").append(location.getCity()).append("\n");
                    currentPosition.append("区：").append(location.getDistrict()).append("\n");
                    currentPosition.append("街道：").append(location.getStreet()).append("\n");
                    currentPosition.append("定位方式：");
                    if(location.getLocType()==BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else if(location.getLocType() == BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");
                    }
                    locationInfo = currentPosition.toString();
                    locationTv.setText(currentPosition);
                }
            });
        }
    }

    private void sendSMSS(String phoneNumber) {
        String content = locationTv.getText().toString().trim();
        String phone = phoneNumber.trim();
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
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //requestLocation();
                }
                else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
}
