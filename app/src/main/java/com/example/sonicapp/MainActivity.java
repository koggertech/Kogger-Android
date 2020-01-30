package com.example.sonicapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Set;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    TimeChartView surfaceView;
    KS_DriverExample_c DeviceDriver;
    LocationManager locationManager;

    TextView TextInfo;

    ToggleButton TB_ConnectionMenu;
    ScrollView MenuConnection;

    ToggleButton TB_ShowMenu;
    ScrollView Menu;

    ToggleButton TB_DisplayMenu;
    ScrollView MenuDisplay;

    ToggleButton TB_GeneralMenu;
    ScrollView MenuGeneral;

    CustomSeekBar SB_Baudrate;

    ToggleButton TB_WriteFlash;
    ToggleButton TB_Restore;
    ToggleButton TB_Update;

    CustomSeekBar SB_SamplesCount;
    CustomSeekBar SB_Range;

    CustomSeekBar SB_Freq;
    CustomSeekBar SB_Width;
    Switch SW_Boost;
    CustomSeekBar SB_SoundSpeed;

    CustomSeekBar SB_Period;
    Switch SW_DatasetChart;
    Switch SW_DatasetAttQuat;
    Switch SW_DatasetSimpleDist;
    Switch SW_DatasetDistNMEA;

    TextView TV_DispMinThr;
    SeekBar SB_DispMinThr;

    TextView TV_DispMaxThr;
    SeekBar SB_DispMaxThr;

    Switch SW_Log;
    Switch SW_KeepScreen;
    Switch SW_GNSS;

    RadioGroup RG_PlotColor;

    SerialUSB serial;



    public static final String APP_PREFERENCES = "settings_global";
    public static final String APP_PREFERENCES_BAUDRATE = "BAUDRATE";
    public static final String APP_PREFERENCES_LOGGING = "LOGGING";
    public static final String APP_PREFERENCES_KEEP_SCREEN = "KEEP_SCREEN";
    public static final String APP_PREFERENCES_USE_GNSS = "USE_GNSS";
    private SharedPreferences mSettings;

    // 0x[ID][VER]
    private static final int INTERFACE_RECEIVE_DATASET_V0 = 0x1000;
    private static final int INTERFACE_RECEIVE_DIST_SETUP_V0 = 0x1100;
    private static final int INTERFACE_RECEIVE_CHART_SETUP_V0 = 0x1200;
    private static final int INTERFACE_RECEIVE_DSP_V0 = 0x1300;
    private static final int INTERFACE_RECEIVE_TRANSC_V0 = 0x1400;
    private static final int INTERFACE_RECEIVE_SND_SPD_V0 = 0x1500;
    private static final int INTERFACE_RECEIVE_UART_V0 = 0x1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

       if(ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},  1);
       }

        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        LinearLayout linearLayout = findViewById(R.id.chart_layout);
        surfaceView = new TimeChartView(this, linearLayout);

        serial = new SerialUSB();

        DeviceDriver = new KS_DriverExample_c();
        DeviceDriver.SetPort(serial);

        InfoHandler_Box = new InfoHandler();

        DeviceDriver.SetListenerChart(new KoggerSonicBaseDriver_c.InterfaceListenerChart() {
            @Override
            public void ReceiveComplete(long offset_mm, int resolution_mm, int count_samples, short[] data) {

                int[] val_chart = new int[count_samples];
                for (int i = 0; i < count_samples; i++) {
                    val_chart[i] = data[i];
                }

                surfaceView.Waterfall.PushData(val_chart, resolution_mm, (int)offset_mm);
//                surfaceView.Waterfall2.PushData(val_chart, resolution_mm, (int)offset_mm);
            }

            @Override
            public void ReceiveNew() {
            }

            @Override
            public void ReceiveSetting(int count_samples, int resolution_mm, long offset_samples) {
                InfoHandler_Box.obtainMessage(INTERFACE_RECEIVE_CHART_SETUP_V0, null).sendToTarget();
            }

            @Override
            public void ReceiveResponse(int code) {
            }
        });


        DeviceDriver.SetListenerDataset(new KoggerSonicBaseDriver_c.InterfaceListenerDataset() {
            @Override
            public void ReceiveSetting() {
                InfoHandler_Box.obtainMessage(INTERFACE_RECEIVE_DATASET_V0, null).sendToTarget();
            }

            @Override
            public void ReceiveResponse(int code) {

            }
        });

        DeviceDriver.SetListenerTransc(new KoggerSonicBaseDriver_c.InterfaceListenerTransc() {
            @Override
            public void ReceiveSetting() {
                InfoHandler_Box.obtainMessage(INTERFACE_RECEIVE_TRANSC_V0, null).sendToTarget();
            }

            @Override
            public void ReceiveResponse(int code) {

            }
        });

        DeviceDriver.SetListenerSound(new KoggerSonicBaseDriver_c.InterfaceListenerSound() {
            @Override
            public void ReceiveSetting() {
                InfoHandler_Box.obtainMessage(INTERFACE_RECEIVE_SND_SPD_V0, null).sendToTarget();
            }

            @Override
            public void ReceiveResponse(int code) {

            }
        });

        DeviceDriver.SetListenerAttitude(new KoggerSonicBaseDriver_c.InterfaceListenerAttitude() {
            public void ReceiveYPR(float ypr[]){
                TextInfo = findViewById(R.id.info_text);
                String t = new String();
                t = "Y: " + String.format("%.2f", ypr[0]) + "; ";
                t += "P: " + String.format("%.2f", ypr[1]) + "; ";
                t += "R: " + String.format("%.2f", ypr[2]) + "; ";

                InfoHandler_Box.obtainMessage(1, t.getBytes()).sendToTarget();
            }

            public void ReceiveQuat(float[] q){

            }
            public void ReceiveResponse(int code){

            }
        });


        MenuConnection = findViewById(R.id.sv_connection);
        MenuConnection.setVisibility(View.VISIBLE);

        Menu = findViewById(R.id.sv_menu);
        Menu.setVisibility(View.VISIBLE);

        MenuDisplay = findViewById(R.id.sv_display);
        MenuDisplay.setVisibility(View.VISIBLE);

        MenuGeneral = findViewById(R.id.sv_general);
        MenuGeneral.setVisibility(View.VISIBLE);

        TB_ConnectionMenu = findViewById(R.id.tb_show_connection);
        TB_ConnectionMenu.setChecked(true);
        TB_ConnectionMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();

                if (isChecked) {
                    TB_ShowMenu.setChecked(false);
                    TB_DisplayMenu.setChecked(false);
                    TB_GeneralMenu.setChecked(false);

                    MenuConnection.setWillNotDraw(false);
                    MenuConnection.setVisibility(View.VISIBLE);

                    buttonView.setBackgroundColor(0xB3FFF8E1);
                    buttonView.setScaleX(1.0f);
                    buttonView.setScaleY(1.0f);
                } else {
                    MenuConnection.setVisibility(View.INVISIBLE);
                    MenuConnection.setWillNotDraw(true);

                    buttonView.setBackgroundColor(0x80FFF8E1);
                    buttonView.setScaleX(0.9f);
                    buttonView.setScaleY(0.9f);
                }
            }
        });
        TB_ConnectionMenu.setChecked(false);

        TB_ShowMenu = findViewById(R.id.tb_show_menu);
        TB_ShowMenu.setChecked(true);
        TB_ShowMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
                if (isChecked) {
                    TB_ConnectionMenu.setChecked(false);
                    TB_DisplayMenu.setChecked(false);
                    TB_GeneralMenu.setChecked(false);

                    Menu.setWillNotDraw(false);
                    Menu.setVisibility(View.VISIBLE);

                    buttonView.setBackgroundColor(0xB3FFF8E1);
                    buttonView.setScaleX(1.0f);
                    buttonView.setScaleY(1.0f);
                } else {
                    Menu.setVisibility(View.INVISIBLE);
                    Menu.setWillNotDraw(true);

                    buttonView.setBackgroundColor(0x80FFF8E1);
                    buttonView.setScaleX(0.9f);
                    buttonView.setScaleY(0.9f);
                }
            }
        });
        TB_ShowMenu.setChecked(false);

        TB_DisplayMenu = findViewById(R.id.tb_show_disp);
        TB_DisplayMenu.setChecked(true);
        TB_DisplayMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();

                if (isChecked) {
                    TB_ConnectionMenu.setChecked(false);
                    TB_ShowMenu.setChecked(false);
                    TB_GeneralMenu.setChecked(false);

                    MenuDisplay.setWillNotDraw(false);
                    MenuDisplay.setVisibility(View.VISIBLE);

                    buttonView.setBackgroundColor(0xB3FFF8E1);
                    buttonView.setScaleX(1.0f);
                    buttonView.setScaleY(1.0f);
                } else {
                    MenuDisplay.setVisibility(View.INVISIBLE);
                    MenuDisplay.setWillNotDraw(true);

                    buttonView.setBackgroundColor(0x80FFF8E1);
                    buttonView.setScaleX(0.9f);
                    buttonView.setScaleY(0.9f);
                }
            }
        });
        TB_DisplayMenu.setChecked(false);


        TB_GeneralMenu = findViewById(R.id.tb_show_general);
        TB_GeneralMenu.setChecked(true);
        TB_GeneralMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();

                if (isChecked) {
                    TB_ConnectionMenu.setChecked(false);
                    TB_ShowMenu.setChecked(false);
                    TB_DisplayMenu.setChecked(false);

                    MenuGeneral.setWillNotDraw(false);
                    MenuGeneral.setVisibility(View.VISIBLE);

                    buttonView.setBackgroundColor(0xB3FFF8E1);
                    buttonView.setScaleX(1.0f);
                    buttonView.setScaleY(1.0f);
                } else {
                    MenuGeneral.setVisibility(View.INVISIBLE);
                    MenuGeneral.setWillNotDraw(true);

                    buttonView.setBackgroundColor(0x80FFF8E1);
                    buttonView.setScaleX(0.9f);
                    buttonView.setScaleY(0.9f);
                }
            }
        });
        TB_GeneralMenu.setChecked(false);

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TB_ConnectionMenu.setChecked(false);
                TB_ShowMenu.setChecked(false);
                TB_DisplayMenu.setChecked(false);
                TB_GeneralMenu.setChecked(false);
            }
        });

        SB_Baudrate = findViewById(R.id.sb_baudrate);
        SB_Baudrate.TextV = findViewById(R.id.tv_baudrate);
        SB_Baudrate.StartText = "Baudrate, bps: ";
        SB_Baudrate.CaseValues = new int[]{9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600};
        SB_Baudrate.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.UART.Baudrate = value;
                DeviceDriver.SendUART();
                if(usbService != null) {
                    usbService.SetBaudrate((int)DeviceDriver.Data.UART.Baudrate);
                }
            }
        });
        SB_Baudrate.setValue(115200);

        TB_WriteFlash = findViewById(R.id.tb_flash);
        TB_WriteFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DeviceDriver.SaveAllSettings();
                    buttonView.setChecked(false);
                } else {

                }
            }
        });
        TB_WriteFlash.setChecked(false);

        TB_Restore = findViewById(R.id.tb_restore);
        TB_Restore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DeviceDriver.RestoreAllSettings();
                    buttonView.setChecked(false);
                } else {
                }
            }
        });
        TB_Restore.setChecked(false);

        TB_Update = findViewById(R.id.tb_update);
        TB_Update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DeviceDriver.FirmwareUpdate();
                    buttonView.setChecked(false);
                } else {
                }
            }
        });
        TB_Update.setChecked(false);


        SB_Freq =  findViewById(R.id.sb_freq);
        SB_Width =  findViewById(R.id.sb_width);
        SW_Boost = findViewById(R.id.sw_boost);
        SB_SoundSpeed =  findViewById(R.id.sb_spd_sound);
        SB_Period =  findViewById(R.id.sb_period);

        SB_SamplesCount = findViewById(R.id.sb_itemchart);
        SB_SamplesCount.TextV = findViewById(R.id.tv_itemchart);
        SB_SamplesCount.StartText = "Samples: ";
        SB_SamplesCount.SetCaseValues(new int[]{100, 150, 300, 500, 800, 1000, 1500, 3000, 4000, 5000});
        SB_SamplesCount.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.Chart.SetSize(value);
                SB_Range.setValue(DeviceDriver.Data.Chart.GetResol());
                DeviceDriver.SendChartSetup();
            }
        });
        SB_SamplesCount.setValue(100);

        SB_Range = findViewById(R.id.sb_range);
        SB_Range.TextV = findViewById(R.id.tv_range);
        SB_Range.StartText = "Resol., mm: ";
        SB_Range.SetCaseValues(new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        SB_Range.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.Chart.SetResol(value);
                SB_Range.setValue(DeviceDriver.Data.Chart.GetResol());
                DeviceDriver.SendChartSetup();
            }
        });
        SB_Range.setValue(10);


        SB_Freq = findViewById(R.id.sb_freq);
        SB_Freq.TextV = findViewById(R.id.tv_freq);
        SB_Freq.StartText = "Freq., kHz: ";
        SB_Freq.SetCaseValues(new int[]{200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300, 310, 320, 330, 340, 350, 360, 370, 380, 390, 400, 410, 420, 430, 440, 450, 460, 470, 480, 490, 500, 510, 520, 530, 540, 550, 560, 570, 580, 590, 600, 610, 620, 630, 640, 650, 660, 670, 680, 690, 700, 710, 720, 730, 740, 750, 760, 770, 780, 790, 800});
        SB_Freq.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.Transc.SetFreq(value);
                DeviceDriver.SendTransc();
            }
        });
        SB_Freq.setValue(700);

        SB_Width = findViewById(R.id.sb_width);
        SB_Width.TextV = findViewById(R.id.tv_width);
        SB_Width.StartText = "Pulse Width: ";
        SB_Width.SetCaseValues(new int[]{0, 4, 6, 8, 10, 15, 20, 25, 30, 40, 50});
        SB_Width.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.Transc.SetWidth(value);
                DeviceDriver.SendTransc();
            }
        });
        SB_Width.setValue(10);

        SW_Boost.setChecked(true);
        SW_Boost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    DeviceDriver.Data.Transc.SetBoost(1);
                } else {
                    DeviceDriver.Data.Transc.SetBoost(0);
                }
                DeviceDriver.SendTransc();
            }
        });
        SW_Boost.setChecked(false);

        SB_SoundSpeed = findViewById(R.id.sb_spd_sound);
        SB_SoundSpeed.TextV = findViewById(R.id.tv_spd_sound);
        SB_SoundSpeed.StartText = "Sound speed, m/s: ";
        SB_SoundSpeed.SetCaseValues(new int[]{1400,  1405,  1410,  1415,  1420,  1425,  1430,  1435,  1440,  1445,  1450,  1455,  1460,  1465,  1470,  1475,  1480,  1485,  1490,  1495,  1500,  1505,  1510,  1515,  1520,  1525,  1530,  1535,  1540,  1545,  1550,  1555,  1560,  1565,  1570,  1575,  1580,  1585,  1590,  1595});
        SB_SoundSpeed.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.SoundSpeed_m_s = value;
                DeviceDriver.SendSoundSpeed();
            }
        });
        SB_SoundSpeed.setValue(1500);

        SB_Period = findViewById(R.id.sb_period);
        SB_Period.TextV = findViewById(R.id.tv_period);
        SB_Period.StartText = "Period, ms: ";
        SB_Period.SetCaseValues(new int[]{0,  10,  20,  50,  100,  200,  500,  1000,  2000});
        SB_Period.setListenerUpdate(new ListenerControlUpdate() {
            @Override
            public void Update(int value) {
                DeviceDriver.Data.Channel.setPeriod(value);
                DeviceDriver.SendChannel();
            }
        });
        SB_Period.setValue(100);


        SW_DatasetChart = findViewById(R.id.sw_send_chart_v0);
        SW_DatasetChart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeviceDriver.Data.Channel.setChart(isChecked);
                DeviceDriver.SendChannel();
            }
        });

        SW_DatasetAttQuat = findViewById(R.id.sw_send_quat);
        SW_DatasetAttQuat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeviceDriver.Data.Channel.setQuat(isChecked);
                DeviceDriver.SendChannel();
            }
        });

        SW_DatasetSimpleDist = findViewById(R.id.sw_send_dist_v0);
        SW_DatasetSimpleDist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeviceDriver.Data.Channel.setDist(isChecked);
                DeviceDriver.SendChannel();
            }
        });

        SW_DatasetDistNMEA = findViewById(R.id.sw_send_dist_nmea);
        SW_DatasetDistNMEA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeviceDriver.Data.Channel.setDistNMEA(isChecked);
                DeviceDriver.SendChannel();
            }
        });


        SB_DispMinThr=  findViewById(R.id.sb_min_amp);
        TV_DispMinThr =  findViewById(R.id.tv_min_amp);

        SB_DispMaxThr=  findViewById(R.id.sb_max_amp);
        TV_DispMaxThr =  findViewById(R.id.tv_max_amp);

        SB_DispMinThr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //DeviceDriver.GUI_SetWidthByIndex(progress);
                //TimeChartView.WaterfallChart.
                String t = new String();
                t = "Min threshold: ";
                t += progress;
                t += " dB";
                TV_DispMinThr.setText(t);
                if(progress > SB_DispMaxThr.getProgress()) {
                    SB_DispMaxThr.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                surfaceView.Waterfall.SetMinThr(SB_DispMinThr.getProgress()*5/2);
                surfaceView.Waterfall.SetMaxThr(SB_DispMaxThr.getProgress()*5/2);
            }
        });
        SB_DispMinThr.setProgress(10);


        SB_DispMaxThr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //DeviceDriver.GUI_SetWidthByIndex(progress);
                //TimeChartView.WaterfallChart.
                String t = new String();
                t = "Max threshold: ";
                t += progress;
                t += " dB";
                TV_DispMaxThr.setText(t);

                if(progress < SB_DispMinThr.getProgress()) {
                    SB_DispMinThr.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                surfaceView.Waterfall.SetMinThr(SB_DispMinThr.getProgress()*5/2);
                surfaceView.Waterfall.SetMaxThr(SB_DispMaxThr.getProgress()*5/2);
            }
        });
        SB_DispMaxThr.setProgress(100);

        RG_PlotColor = findViewById(R.id.rg_plot_color);
        RG_PlotColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case -1:
                        break;

                    case R.id.rb_color_blue:
                        surfaceView.Waterfall.SetColorScheme(0);
                        break;

                    case R.id.rb_color_sepia:
                        surfaceView.Waterfall.SetColorScheme(1);
                        break;

                    case R.id.rb_color_rainbow:
                        surfaceView.Waterfall.SetColorScheme(2);
                        break;

                    default:
                        break;
                }
            }
        });


        SW_Log = findViewById(R.id.sw_log);
        SW_Log.setChecked(false);
        SW_Log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeviceDriver.SetLog(isChecked);
            }
        });

        SW_KeepScreen = findViewById(R.id.sw_screen_on);
        SW_KeepScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });

        SW_GNSS = findViewById(R.id.sw_gnss);
        SW_GNSS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.removeUpdates(locationListener);
                    }
                }
            }
        });
    }

    private class InfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String t = new String();
            switch (msg.what) {
                case 1:
                    byte data[] = (byte[]) msg.obj;
                    TextInfo.setText(new String(data));
                    break;

                case INTERFACE_RECEIVE_DATASET_V0:
                    SB_Period.setValue(DeviceDriver.Data.Channel.getPeriod());
                    SW_DatasetChart.setChecked(DeviceDriver.Data.Channel.getChart());
                    SW_DatasetAttQuat.setChecked(DeviceDriver.Data.Channel.getQuat());
                    SW_DatasetSimpleDist.setChecked(DeviceDriver.Data.Channel.getDist());
                    SW_DatasetDistNMEA.setChecked(DeviceDriver.Data.Channel.getNMEA());
                    break;

                case INTERFACE_RECEIVE_CHART_SETUP_V0:
                    SB_SamplesCount.setValue(DeviceDriver.Data.Chart.GetSize());
                    SB_Range.setValue(DeviceDriver.Data.Chart.GetResol());
                    break;

                case INTERFACE_RECEIVE_TRANSC_V0:
                    SB_Freq.setValue(DeviceDriver.Data.Transc.GetFreq());
                    SB_Width.setValue(DeviceDriver.Data.Transc.GetWidth());
                    SW_Boost.setChecked(DeviceDriver.Data.Transc.GetBoost() > 0);
                    break;

                case INTERFACE_RECEIVE_SND_SPD_V0:
                    SB_SoundSpeed.setValue(DeviceDriver.Data.SoundSpeed_m_s);
                    break;

                case INTERFACE_RECEIVE_UART_V0:
                    break;
            }
        }
    }

    InfoHandler InfoHandler_Box;

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private UsbService usbService;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(serial.Handler);
            usbService.SetBaudrate((int)DeviceDriver.Data.UART.Baudrate);
            serial.usbService = usbService;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
            serial.usbService = null;
            //DeviceDriver.Output.Port.usbService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

        if (mSettings.contains(APP_PREFERENCES_BAUDRATE) && SB_Baudrate != null) {
            int baudrate =  mSettings.getInt(APP_PREFERENCES_BAUDRATE, 0);
            SB_Baudrate.setProgress(baudrate);
        }

        if (mSettings.contains(APP_PREFERENCES_LOGGING) && SW_Log != null) {
            boolean logging = mSettings.getBoolean(APP_PREFERENCES_LOGGING,false);
            SW_Log.setChecked(!logging);
            SW_Log.setChecked(logging);
        }

        if (mSettings.contains(APP_PREFERENCES_KEEP_SCREEN) && SW_KeepScreen != null) {
            boolean keep_screen = mSettings.getBoolean(APP_PREFERENCES_KEEP_SCREEN,false);
            SW_KeepScreen.setChecked(!keep_screen);
            SW_KeepScreen.setChecked(keep_screen);
        }

        if (mSettings.contains(APP_PREFERENCES_USE_GNSS) && SW_GNSS != null) {
            boolean use_gnss = mSettings.getBoolean(APP_PREFERENCES_USE_GNSS,false);
            SW_GNSS.setChecked(!use_gnss);
            SW_GNSS.setChecked(use_gnss);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_BAUDRATE, SB_Baudrate.getProgress());
        editor.putBoolean(APP_PREFERENCES_LOGGING, SW_Log.isChecked());
        editor.putBoolean(APP_PREFERENCES_KEEP_SCREEN, SW_KeepScreen.isChecked());
        editor.putBoolean(APP_PREFERENCES_USE_GNSS, SW_GNSS.isChecked());
        editor.apply();

//        surfaceView.stopDrawThread();
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            DeviceDriver.LogLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  2);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Register the listener with the Location Manager to receive location updates
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
