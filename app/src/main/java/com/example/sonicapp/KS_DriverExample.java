package com.example.sonicapp;

import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class SerialUSB extends Serial {
    UsbService usbService;

    byte Data[];
    int pos_w = 0;
    int pos_r = 0;
    MyHandler Handler;

    SerialUSB() {
        usbService = null;
        Data = new byte[32768];
        Handler = new MyHandler();
    }

    void addData(byte data[]) {
        for(int i = 0; i < data.length; i++) {
            Data[pos_w] = data[i];
            pos_w++;
            if(pos_w == Data.length) {
                pos_w = 0;
            }
        }
    }

    void stop() {
    }

    char readByte() {
        char b = 0;

        if(Data != null) {
            b |= (char)(Data[pos_r] & 0xFF);

            pos_r++;
            if(pos_r == Data.length) {
                pos_r = 0;
            }
        }

        return b;
    }

    int available() {
        int avl = 0;
        if(pos_w > pos_r) {
            avl = pos_w - pos_r;
        } else {
            avl = pos_r - pos_w;
        }
        return avl;
    }

    void writeByte(byte data[]) {
        if(usbService != null) {
            usbService.write(data);
        }
    }

    void writeByte(byte data) {
        byte arr_data[] = new byte[1];
        arr_data[0]= data;
        if(usbService != null) {
            usbService.write(arr_data);
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    byte data[] = (byte[]) msg.obj;
                    addData(data);
                    break;
            }
        }
    }
}

class LogToFile extends LogStream {
    FileOutputStream File;
    LogToFile() {
        super();
    }

    void CreateFile() {
        File myDir = new File(Environment.getExternalStorageDirectory() + "/Kogger/");
        if(!myDir.exists()){
            myDir.mkdir();
        }
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
        String formattedDate = df.format(c.getTime());

        String fname = "KoggerLog" + formattedDate + ".bin";
        File file = new File(myDir, fname);

        try {
            File = new FileOutputStream(file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void Write(OutputStreamExt data) {
        if(File == null) {
            CreateFile();
        }
        if (File != null && data != null) {
            try {
                File.write(data.toByteArray());
                File.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class KS_DriverExample_c extends KoggerSonicBaseDriver_c implements  Runnable{
    private Thread drawThread;
    FileInputStream FileFirmware;

    KS_DriverExample_c() {
        super();
        SetListenerLoadUpdate(new InterfaceListenerLoadUpdate() {
            @Override
            public void ReceiveResponse(int code) {
                if(code == RespOk) {
                    Updater.SetConfirmNext();
                } else {
                    Updater.Stop();
                }

            }
        });

        drawThread = new Thread(this, "Driver thread");
        drawThread.start();
    }

    class UpdaterFromFile {
        FileInputStream FileFirmware;
        boolean ConfirmNext = false;
        int NbrMsg = 0;
        long LastActionTime = -1;
        void Process() {
            if(FileFirmware != null) {
                if(ConfirmNext) {
                    ConfirmNext = false;
                    try {
                        int avlbl = FileFirmware.available();
                        if(avlbl > 64) {
                            avlbl = 64;
                        }

                        byte[] data = new byte[avlbl];
                        FileFirmware.read(data, 0, avlbl);
                        NbrMsg++;
                        LoadUpdatePart(NbrMsg, data);
                        LastActionTime = System.currentTimeMillis();

                        if(avlbl < 64) {
                            FWRun();
                            FileFirmware = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if ((LastActionTime != -1) && ((System.currentTimeMillis() - LastActionTime) > 600)) {
                        SetConfirmNext();
                    }
                }
            }
        }

        void Start(FileInputStream file) {
            if(file == null) {
                return;
            }
            LastActionTime = System.currentTimeMillis();
            NbrMsg = 0;
            ConfirmNext = false;
            FileFirmware = file;
            BootRun();
        }

        void Stop() {
            LastActionTime = -1;
            ConfirmNext = false;
            FileFirmware = null;
        }

        void SetConfirmNext() {
            ConfirmNext = true;
        }
    }

    UpdaterFromFile Updater = new UpdaterFromFile();

    static long test_request_timestamp = 0;
    void TestRequest() {
        long current_time  = System.currentTimeMillis();
        if(current_time - test_request_timestamp >= 500) {
            test_request_timestamp = current_time;
//            RequestChart();
//            RequestTimestamp();
            RequestAttitudeInQuat();
        }
    }

    @Override
    public void run() {
        while(true) {
//            try {
//                Thread.sleep(5);
                Process();
                Updater.Process();
//                TestRequest();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    void SetLog(boolean enable) {
        if(enable) {
            Log = new LogToFile();
        } else {
            Log = new LogStream();
        }
    }

    void LogLocation(Location location) {
        Out.New(new ProtoInst(ID_NAV, Content, 0, false), false, true);
        Out.writeDouble(location.getLatitude());
        Out.writeDouble(location.getLongitude());
        Out.writeFloat(location.getAccuracy());
        Out.End();
    }

    void FirmwareUpdate() {
        File myDir = new File(Environment.getExternalStorageDirectory() + "/Kogger/");
        if(!myDir.exists()){
            return;
        }

        String fname = "KoggerSonarFW.ufw";
        File file = new File(myDir, fname);

        try {
            FileFirmware = new FileInputStream(file);
            Updater.Start(FileFirmware);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}