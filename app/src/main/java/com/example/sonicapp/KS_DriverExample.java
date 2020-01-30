package com.example.sonicapp;

import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
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

//                        System.out.println("Total Avl:" + avlbl);
                        if(avlbl > 64) {
                            avlbl = 64;
                        }
//                        System.out.println("Msg:" + NbrMsg);
//                        System.out.println("Avl:" + avlbl);
//                        System.out.println("Off:" + NbrMsg*64);

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
    private Socket socket;

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(10);
                Process();
//                socket = new Socket("45.80.68.60", 8765);
                Updater.Process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        Out.New(new ProtoInst(ID_GNSS, Content, 0, false), false, true);
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

    void CopyDataFromSource() {
//        Data.Chart.SetSize((int)SliderChartCount.getValue());
//        Data.Chart.SetResol((int)SliderChartResol.getValue()*10);
//        Data.Chart.SetStartPos((int)SliderChartStart.getValue()*1000);
//        Data.Chart.SetPeriod((int)SliderChartPeriod.getValue());
//
//        Data.AGC.New((int)SliderAGCStart.getValue()*1000, (int)SliderAGCOffset.getValue()*100, (int)SliderAGCSlope.getValue()*10, (int)SliderAGCAbsorp.getValue()*1000);
//
//        Data.Transc.New((int)SliderTranscFreq.getValue(), (short)SliderTranscWidth.getValue(), (short)ToogleTranscBoost.getValue());
//
//        if (SerialBaudrate != -1) {
//            Data.UART.New(SerialBaudrate);
//        }
    }

//    int GUI_ConvertIndexToItemChartCount(int index) {
//        int item_cnt = 0;
//        switch(index) {
//            case 0:
//                item_cnt = 100;
//                break;
//            case 1:
//                item_cnt = 150;
//                break;
//            case 2:
//                item_cnt = 300;
//                break;
//            case 3:
//                item_cnt = 500;
//                break;
//            case 4:
//                item_cnt = 800;
//                break;
//            case 5:
//                item_cnt = 1000;
//                break;
//            case 6:
//                item_cnt = 1500;
//                break;
//            case 7:
//                item_cnt = 3000;
//                break;
//            case 8:
//                item_cnt = 5000;
//                break;
//
//        }
//        return item_cnt;
//    }
//
//    void GUI_SetItemChartCountByIndex(int index) {
//        int item_cnt = GUI_ConvertIndexToItemChartCount(index);
//        DataInterface.Chart.SetSize(item_cnt);
//    }
//
//    int GUI_GetItemChartCount() {
//        return DataInterface.Chart.GetSize();
//    }
//
//    int GUI_ConvertIndexToResol(int index) {
//        int resol = (index + 1) * 10;
//        return resol;
//    }
//
//    void GUI_SetRangeByIndex(int index) {
//        int resol = GUI_ConvertIndexToResol(index);
//        DataInterface.Chart.SetResol(resol);
//    }
//
//    int GUI_GetRange() {
//        return DataInterface.Chart.GetSize()*DataInterface.Chart.GetResol()+(int)DataInterface.Chart.GetAbsOffset();
//    }
//
//    int GUI_ConvertIndexToPeriod(int index) {
//        int period = 0;
//        switch(index) {
//            case 0:
//                period = 0;
//                break;
//            case 1:
//                period = 10;
//                break;
//            case 2:
//                period = 20;
//                break;
//            case 3:
//                period = 50;
//                break;
//            case 4:
//                period = 100;
//                break;
//            case 5:
//                period = 200;
//                break;
//            case 6:
//                period = 500;
//                break;
//            case 7:
//                period = 1000;
//                break;
//            case 8:
//                period = 2000;
//                break;
//
//        }
//
//        return period;
//    }
//
////    void GUI_SetPeriodByIndex(int index) {
////        int period = GUI_ConvertIndexToPeriod(index);
////        DataInterface.Chart.SetPeriod(period);
////    }
////
////    int GUI_GetPeriod() {
////        return DataInterface.Chart.GetPeriod();
////    }
//
//    void GUI_DataChartSend() {
//        Data.Chart.Set(DataInterface.Chart);
//        SendChartSetup();
//    }
//
//    int GUI_ConvertIndexToFreq(int index) {
//        int freq = 180 + (index) * 10;
//        return freq;
//    }
//
//    void GUI_SetFreqByIndex(int index) {
//        int freq = GUI_ConvertIndexToFreq(index);
//        DataInterface.Transc.SetFreq(freq);
//    }
//
//    int GUI_GetFreq() {
//        return DataInterface.Transc.GetFreq();
//    }
//
//    int GUI_ConvertIndexToWidth(int index) {
//        int width = index*2;
//        return width;
//    }
//
//    void GUI_SetWidthByIndex(int index) {
//        int width = GUI_ConvertIndexToWidth(index);
//        DataInterface.Transc.SetWidth(width);
//    }
//
//    int GUI_GetWidth() {
//        return DataInterface.Transc.GetWidth();
//    }
//
//    void GUI_SetBoostByIndex(int boost) {
//        DataInterface.Transc.SetBoost(boost);
//    }
//
//    int GUI_GetBoost() {
//        return DataInterface.Transc.GetBoost();
//    }
//
//    void GUI_DataTranscSend() {
//        Data.Transc.Set(DataInterface.Transc);
////        SetTransc();
//    }
//
//    int GUI_ConvertIndexToSoundSpeed(int index, int envir_index) {
//        int speed = 0;
//        if(envir_index == 0) { // water
//            speed = 1380 + index*2;
//        } else {
//            speed = 800 + index*20;
//        }
//
//        return speed;
//    }
//
//    void GUI_SetSoundSpeedByIndex(int index, int envir_index) {
//        int speed = GUI_ConvertIndexToSoundSpeed(index, envir_index);
//        DataInterface.SoundSpeed_m_s = speed;
//    }
//
//    int GUI_GetSoundSpeed() {
//        return DataInterface.SoundSpeed_m_s;
//    }
//
//    void GUI_DataSoundSpeedSend() {
//        Data.SoundSpeed_m_s = DataInterface.SoundSpeed_m_s;
////        SetSoundSpeed();
//    }

//    void GUI_SetUARTBaudrate(int baudrate) {
//        DataInterface.UART.Baudrate = baudrate;
//    }
//
//    void GUI_SetBaudrateByIndex(int index) {
//        int baudrate = 9600;
//        switch (index) {
//            case 0:
//                baudrate = 9600;
//                break;
//            case 1:
//                baudrate = 19200;
//                break;
//            case 2:
//                baudrate = 38400;
//                break;
//            case 3:
//                baudrate = 57600;
//                break;
//            case 4:
//                baudrate = 115200;
//                break;
//            case 5:
//                baudrate = 230400;
//                break;
//            case 6:
//                baudrate = 460800;
//                break;
//            case 7:
//                baudrate = 921600;
//                break;
//        }
//        GUI_SetUARTBaudrate(baudrate);
//    }

//    void GUI_UARTSend() {
//        Data.UART.Baudrate = DataInterface.UART.Baudrate;
//        SendUART();
//    }
//
//    int GUI_GetBaudrate() {
//        return (int)DataInterface.UART.Baudrate;
//    }

}