package com.example.cheng.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    //真实距离到显示屏幕的距离
    /***
     * TODO：获取自定义view的长宽，计算合适的比例因子
     * TODO：最好在自定义view中描绘出这4个点
     */

    double scaling_factor = 1;

    //view 的 宽:w和高:h
    int view_w= 200;
    int view_h = 100;
    //提供的4个AP
    private static final String AccessPoint_A = "AC_A";

    private static final String AccessPoint_B = "AC_B";

    private static final String AccessPoint_C = "AC_C";

    private static final String AccessPoint_D = "AC_D";

    List<ScanResult> resultList;



    //检测权限列表

    private static final String[] NEEDED_PERMISSIONS = new String[]{

            Manifest.permission.ACCESS_COARSE_LOCATION,

            Manifest.permission.ACCESS_FINE_LOCATION

    };

    private static final int PERMISSION_REQUEST_CODE = 0;



    private WifiManager mWifiManager;

    private Button show_to;

    private TextView ap_x, ap_y, ap_z, ap_w;

    private DrawView drawView;

    @Override

    protected void onCreate(Bundle savedInstanceState)

    {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init_view_button();

        drawView = findViewById(R.id.drawView);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        final boolean mHasPermission = checkPermission();

        if (!mHasPermission) {

            requestPermission();

        }



        show_to.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {



                //打开WIFI

                open_wifi();



                //测试广播，对扫描结果进行监听

                registerBroadcast();



                //启动线程，1秒钟后开启，每隔1秒钟进行WIFI扫描

                Timer timer = new Timer();

                timer.schedule(new TimerTask() {

                    @Override

                    public void run() {

                        mWifiManager.startScan();

                    }

                },1000,100);

            }

        });



    }



    private void init_view_button()

    {

        show_to = findViewById(R.id.get);

        ap_x = findViewById(R.id.ap_x);

        ap_y = findViewById(R.id.ap_y);

        ap_z = findViewById(R.id.ap_z);

        ap_w = findViewById(R.id.ap_w);

    }



    private void open_wifi()

    {

        if (!mWifiManager.isWifiEnabled()) {

            mWifiManager.setWifiEnabled(true);

        }

    }



    private boolean checkPermission()

    {



        for (String permission : NEEDED_PERMISSIONS) {

            if (ActivityCompat.checkSelfPermission(this, permission)

                    != PackageManager.PERMISSION_GRANTED) {

                return false;

            }

        }



        return true;

    }



    private void requestPermission()

    {

        ActivityCompat.requestPermissions(this,

                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);

    }


    //注册广播，用于监听WIFI
    private void registerBroadcast()

    {

        IntentFilter filter = new IntentFilter();

        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mReceiver, filter);

    }


    private void show_info(String[] tem) {

        ap_x.setText(tem[0]);

        ap_y.setText(tem[1]);

        ap_z.setText(tem[2]);

        ap_w.setText(tem[3]);

    }

    private int[] sting2int(String[] string) {

        int tems[] = new int[4];

        tems[0] = Integer.valueOf(string[0]);

        tems[1] = Integer.valueOf(string[1]);

        tems[2] = Integer.valueOf(string[2]);

        tems[3] = Integer.valueOf(string[3]);

        return tems;

    }

    private double[] strength2view(int[] strength){
        double view_distance[] = new double[4];
        for(int i = 0; i <= 3; ++i){
            view_distance[i] = scaling_factor * strength2distance(strength[i]);
        }
        return  view_distance;
    }

    private int strength2distance(int s){
        int tem = 0;
        //TODO: switch the strength to distance eg, wifi 100 map 1 m
        if(s<100){

        }
        return tem;
    }



    /**
     * 预设定的布局：
     *              view_w
     *      (0, 0)A----------------------------B(view_w, 0)
     *            \                            \
     *            \                            \
     *            \             X(x, y)        \ view_h
     *            \                            \
     *            \                            \
     * (0, view_h)D----------------------------C(view_w, view_h)
     * @param SA:改点离AP点A的大致距离
     * @param SB:该点离AP点B的大致距离
     * @param SC:该点离AP点C的大致距离
     * @param SD:该点离AP点D的大致距离
     * @return int[2], 改点再view中的位置坐标
     * */
    private int[] get_xy(int SA, int SB, int SC, int SD) {
        int xy[] = new int[2];
        double temx1 = 0,temx2 = 0, temx3 = 0, temy1= 0, temy2 = 0, temy3 = 0;
        int count = 0; //有效交点的个数

        //利用A，B点画圆求交，D点进行选点
        if ( (SA + SB) > view_w && SA < view_w && SB < view_w){ //确保有交点且交点落在区域内
            //x=(SA^2-SB^2+view_w^2)/(2*view_w)
            temx1 = (Math.pow((double) SA, 2.0) - Math.pow((double) SB, 2.0) + Math.pow(view_w, 2.0)) / (2 * view_w);
            temy1 = Math.pow((double) SA, 2.0) - Math.pow(temx1, 2.0);

            //其实下面的代码没有必要，只需要取正数就行
            double one = Math.pow(temx1, 2.0) + Math.pow((temy1 - view_h), 2.0);
            double two = Math.pow(temx1, 2.0) + Math.pow((temy1 + view_h), 2.0);
            if (Math.abs(one - SD ) > Math.abs(two - SD)) {
                temy1 = -temy1;
            }
            ++count;
        }

        //利用A，C点画圆求交，B确定位置
        if ( (Math.pow((SA + SC),2) > Math.pow(view_w, 2) + Math.pow(view_h, 2))
                &&(Math.pow(SA, 2) < Math.pow(view_w, 2) + Math.pow(view_h, 2))
                &&(Math.pow(SC, 2) < Math.pow(view_w, 2) + Math.pow(view_h, 2))){//确保有交点,且交点在区域内
            double t = Math.pow(SA, 2) + Math.pow(view_w, 2) + Math.pow(view_h, 2) - Math.pow(SC, 2);
            double a = 4 * (Math.pow(view_w, 2) + Math.pow(view_h, 2));
            double b = -4 * view_h * t;
            double c = Math.pow(t, 2) - 4 * (Math.pow((2 * view_w * SA), 2));
            //a *  y^2 + b * y + c = 0
            double delte = Math.pow(b, 2) - 4 * a * c;
            if(delte >= 0 ){
                double y1 = (-b + Math.sqrt(delte))/(2 * b);
                double y2 = (-b - Math.sqrt(delte))/(2 * b);
                double x1 = 0,x2 = 0;
                if ( y1 > 0){
                    x1  = Math.sqrt(Math.pow(SA, 2) - Math.pow(y1, 2));
                }
                if (y2 > 0){
                    x2 = Math.sqrt(Math.pow(SA, 2) - Math.pow(y2, 2));
                }
                //有交点
                if (y1>0||y2>0){
                    ++count;
                }
                if(y1 > 0 & y2 >0){
                    double one = Math.pow(x1 - view_w, 2.0) + Math.pow(y1, 2.0);
                    double two = Math.pow(x2 - view_w, 2.0) + Math.pow(y2, 2.0);
                    if (Math.abs(one - SB ) > Math.abs(two - SB)){
                        temy2 = y2;
                        temx2 = x2;
                    }else {
                        temy2 = y1;
                        temx2 = x1;
                    }
                }else if(y1 < 0 ){
                    temx2 = x2;
                    temy2 = y2;
                }else if(y2 < 0){
                    temx2 = x1;
                    temy2 = y1;
                }else{
                    temx2 = 0;
                    temy2 = 0;
                }
            }
        }

        //用B，D画圆 ，C确定位置
        if((Math.pow((SB + SD), 2) > Math.pow(view_h, 2) + Math.pow(view_w, 2))
                &&(Math.pow(SB, 2) < Math.pow(view_h, 2) + Math.pow(view_w, 2))
                &&(Math.pow(SD, 2) < Math.pow(view_h, 2) + Math.pow(view_w, 2))){
            double t = Math.pow(view_w, 2) + Math.pow(SD, 2) - Math.pow(SB, 2) - Math.pow(view_h, 2);
            double a = Math.pow(2 * view_h, 2) + Math.pow(2 * view_w, 2);
            double b = 4 * view_h * t - 8 * view_h * Math.pow(view_w, 2);
            double c = Math.pow(t, 2) - Math.pow(2 * view_w * SD, 2) + Math.pow(2 * view_w * view_h, 2);
            double delte = Math.pow(b, 2) - 4 * a * c;
            if(delte > 0){
                double y1 = (-b + Math.sqrt(delte))/(2 * b);
                double y2 = (-b - Math.sqrt(delte))/(2 * b);
                double x1 = 0,x2 = 0;
                if ( y1 > 0){
                    x1 = Math.sqrt(Math.pow(SD, 2) - Math.pow(y1 - view_h, 2));
                }
                if (y2 > 0){
                    x2 = Math.sqrt(Math.pow(SD, 2) - Math.pow(y2 - view_h, 2));
                }
                //有交点
                if (y1>0||y2>0){
                    ++count;
                }
                if(y1 > 0 & y2 >0){
                    double one = Math.pow(x1 - view_w, 2.0) + Math.pow(y1 - view_h, 2.0);
                    double two = Math.pow(x2 - view_w, 2.0) + Math.pow(y2 - view_h, 2.0);
                    if (Math.abs(one - SD ) > Math.abs(two - SD)){
                        temy3 = y2;
                        temx3 = x2;
                    }else {
                        temy3 = y1;
                        temx3 = x1;
                    }
                }else if(y1 < 0 ){
                    temx3 = x2;
                    temy3 = y2;
                }else if(y2 < 0){
                    temx3 = x1;
                    temy3 = y1;
                }else{
                    temx3 = 0;
                    temy3 = 0;
                }
            }
        }
        if (count!=0) {
            xy[0] = (int) (temx1 + temx2 + temx3) / count;
            xy[1] = (int) (temy1 + temy2 + temy3) / count;
        }else {
            xy[0] = 0;
            xy[1] = 0;
        }
        return xy;
    }

    private String[] filt_info(List<ScanResult> resultList){

        /**
        * @param resultList:wifi 扫描的结果
        * @return tem:提取wifi扫描结果中的4个AP点的强度信息，并转换成字符串
        * */

        String tem[] = new String[4];

        for (int i=0;i <= 3; i++){

            tem[i]=String.valueOf(i);

        }

        for (ScanResult sc : resultList) {

            if (sc.SSID.equals(AccessPoint_A)) {

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[0] = String.valueOf(temp);

            }

            if (sc.SSID.equals(AccessPoint_B)) {

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[1] = String.valueOf(temp);

            }

            if (sc.SSID.equals(AccessPoint_C)) {

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[2] = String.valueOf(temp);

            }

            if(sc.SSID.equals(AccessPoint_D)){

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[3] = String.valueOf(temp);

            }

        }

        return tem;

    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver(){

        /**
         * 监听wifi广播，一旦扫描到wifi的结果就调用onReceive
         * */

        @Override

        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            // wifi已成功扫描到可用wifi

            if (Objects.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                resultList = mWifiManager.getScanResults();

                String tem[] = filt_info(resultList);

                show_info(tem);

                //更新view中的X，Y；

                int tems[] = sting2int(tem);

                //TODO:strength2view

                int tem_xy[] = get_xy(tems[0], tems[1], tems[2], tems[3]);

                Message message = new Message();

                message.what = 0x123;

                message.arg1= tem_xy[0] ;

                message.arg2 = tem_xy[1];


                drawView.handler.sendMessage(message);

            }

        }

    };
}
