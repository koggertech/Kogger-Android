package com.example.sonicapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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


    TextView TV_Baudrate;
    SeekBar SB_Baudrate;

    ToggleButton TB_WriteFlash;

    TextView TV_ItemChart;
    SeekBar SB_ItemChart;

    TextView TV_Range;
    SeekBar SB_Range;

    TextView TV_Period;
    SeekBar SB_Period;

    TextView TV_Freq;
    SeekBar SB_Freq;

    TextView TV_Width;
    SeekBar SB_Width;

    Switch SW_Boost;

    TextView TV_SoundSpeed;
    SeekBar SB_SoundSpeed;

    TextView TV_DispMinThr;
    SeekBar SB_DispMinThr;

    TextView TV_DispMaxThr;
    SeekBar SB_DispMaxThr;

    Switch SW_Log;
    Switch SW_KeepScreen;
    Switch SW_GNSS;

    RadioGroup RG_PlotColor;

    Serial serial;

    public static final String APP_PREFERENCES = "settings_global";
    public static final String APP_PREFERENCES_BAUDRATE = "BAUDRATE";
    public static final String APP_PREFERENCES_LOGGING = "LOGGING";
    public static final String APP_PREFERENCES_KEEP_SCREEN = "KEEP_SCREEN";
    public static final String APP_PREFERENCES_USE_GNSS = "USE_GNSS";
    private SharedPreferences mSettings;

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

        serial = new Serial();

        DeviceDriver = new KS_DriverExample_c();
        DeviceDriver.SetPort(serial);
        DeviceDriver.VTimeChart = surfaceView;


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

        SB_Baudrate =  findViewById(R.id.sb_baudrate);
        TV_Baudrate =  findViewById(R.id.tv_baudrate);
        SB_Baudrate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String t = new String();
                t = "Baudrate, bps: ";

                DeviceDriver.GUI_SetBaudrateByIndex(progress);
                DeviceDriver.GUI_UARTSend();
                DeviceDriver.GUI_UARTSend();
                DeviceDriver.GUI_UARTSend();
                DeviceDriver.GUI_UARTSend();
                DeviceDriver.GUI_UARTSend();
                DeviceDriver.GUI_UARTSend();

                t += DeviceDriver.GUI_GetBaudrate();

                //long time = System.currentTimeMillis()%(1000000000);
                //t = String.valueOf(time);

                TV_Baudrate.setText(t);

                if(usbService != null) {
                    usbService.SetBaudrate(DeviceDriver.GUI_GetBaudrate());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        SB_Baudrate.setProgress(4);

        TB_WriteFlash = findViewById(R.id.tb_flash);
        TB_WriteFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();

                if (isChecked) {
                    DeviceDriver.FlashAllSatting();
                    buttonView.setChecked(false);
                } else {

                }
            }
        });
        TB_WriteFlash.setChecked(false);


        SB_ItemChart =  findViewById(R.id.sb_itemchart);
        TV_ItemChart =  findViewById(R.id.tv_itemchart);

        SB_Range =  findViewById(R.id.sb_range);
        TV_Range =  findViewById(R.id.tv_range);

        SB_Period =  findViewById(R.id.sb_period);
        TV_Period =  findViewById(R.id.tv_period);

        SB_Freq =  findViewById(R.id.sb_freq);
        TV_Freq =  findViewById(R.id.tv_freq);

        SB_Width =  findViewById(R.id.sb_width);
        TV_Width =  findViewById(R.id.tv_width);

        SW_Boost = findViewById(R.id.sw_boost);

        SB_SoundSpeed =  findViewById(R.id.sb_spd_sound);
        TV_SoundSpeed =  findViewById(R.id.tv_spd_sound);

        SB_ItemChart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceDriver.GUI_SetItemChartCountByIndex(progress);

                String t = new String();
                t = "Samples: ";
                t += DeviceDriver.GUI_GetItemChartCount();
                TV_ItemChart.setText(t);

                t = "Range, m: ";
                t += (float)DeviceDriver.GUI_GetRange()/1000.0f;
                TV_Range.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DeviceDriver.GUI_DataChartSend();
            }
        });
        SB_ItemChart.setProgress(4);

        SB_Range.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceDriver.GUI_SetRangeByIndex(progress);

                String t = new String();
                t = "Range, m: ";
                t += (float)DeviceDriver.GUI_GetRange()/1000.0f;
                TV_Range.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //DeviceDriver.SetChart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DeviceDriver.GUI_DataChartSend();
            }
        });
        SB_Range.setProgress(4);

        SB_Period.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceDriver.GUI_SetPeriodByIndex(progress);

                String t = new String();
                t = "Period, ms: ";
                t += (float)DeviceDriver.GUI_GetPeriod();
                TV_Period.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DeviceDriver.GUI_DataChartSend();
            }
        });
        SB_Period.setProgress(4);

        SB_Freq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceDriver.GUI_SetFreqByIndex(progress);

                String t = new String();
                t = "Freq., kHz: ";
                t += (float)DeviceDriver.GUI_GetFreq();
                TV_Freq.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DeviceDriver.GUI_DataTranscSend();
            }
        });
        SB_Freq.setProgress(52);

        SB_Width.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceDriver.GUI_SetWidthByIndex(progress);

                String t = new String();
                t = "Pulse Width: ";
                t += (float)DeviceDriver.GUI_GetWidth();
                TV_Width.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DeviceDriver.GUI_DataTranscSend();
            }
        });
        SB_Width.setProgress(5);

        SW_Boost.setChecked(true);
        SW_Boost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    DeviceDriver.GUI_SetBoostByIndex(1);
                } else {
                    DeviceDriver.GUI_SetBoostByIndex(0);
                }
                DeviceDriver.GUI_DataTranscSend();
            }
        });
        SW_Boost.setChecked(false);

        SB_SoundSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceDriver.GUI_SetSoundSpeedByIndex(progress, 0);

                String t = new String();
                t = "Sound speed: ";
                t += (float)DeviceDriver.GUI_GetSoundSpeed();
                TV_SoundSpeed.setText(t);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DeviceDriver.GUI_DataSoundSpeedSend();
            }
        });
        SB_SoundSpeed.setProgress(60);


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
                DeviceDriver.VTimeChart.Waterfall.SetMinThr(SB_DispMinThr.getProgress()*5/2);
                DeviceDriver.VTimeChart.Waterfall.SetMaxThr(SB_DispMaxThr.getProgress()*5/2);
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
                DeviceDriver.VTimeChart.Waterfall.SetMinThr(SB_DispMinThr.getProgress()*5/2);
                DeviceDriver.VTimeChart.Waterfall.SetMaxThr(SB_DispMaxThr.getProgress()*5/2);
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
                        DeviceDriver.VTimeChart.Waterfall.SetColorScheme(0);
                        break;

                    case R.id.rb_color_sepia:
                        DeviceDriver.VTimeChart.Waterfall.SetColorScheme(1);
                        break;

                    case R.id.rb_color_rainbow:
                        DeviceDriver.VTimeChart.Waterfall.SetColorScheme(2);
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




        /*byte data[] = new byte[7];
        data[0] = (byte)0xbb;
        data[1] = (byte)0x55;
        data[2] = (byte)0x0;
        data[3] = (byte)0x0;
        data[4] = (byte)0x0;
        data[5] = (byte)0x0;
        data[6] = (byte)0x0;
        //Driver.Input.Port.buf.put(data);
        DeviceDriver.Input.Port.addData(data);
        DeviceDriver.Process();*/

        TextInfo = findViewById(R.id.info_text);

        mHandler = DeviceDriver.DataIn;
    }

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
    private Handler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            usbService.SetBaudrate(DeviceDriver.GUI_GetBaudrate());
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
