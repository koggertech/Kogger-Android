package com.example.sonicapp;

import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    TimeChartView VTimeChart;
    private Thread drawThread;
    Handler DataIn;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    byte data[] = (byte[]) msg.obj;
                    Port.addData(data);
                    break;
            }
        }
    }

    KS_DriverExample_c() {
        super();
        DataIn  = new MyHandler();

        drawThread = new Thread(this, "Driver thread");
        drawThread.start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(10);
                Process();
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
        Out.New(new ProtoInst(CMD_ID_GNSS, Content, 0, false), false, true);
        Out.writeDouble(location.getLatitude());
        Out.writeDouble(location.getLongitude());
        Out.writeFloat(location.getAccuracy());
        Out.End();
    }

    protected void CallbackGet_Chart() {
        if (Data.Chart.GetDataComlete()) {
            int[] val_chart = new int[Data.Chart.GetSize()];
            for (int i = 0; i < Data.Chart.GetSize(); i++) {
                val_chart[i] = Data.Chart.Data[i];
            }

            //VTimeChart.Waterfall.SetRangeParam(Data.Chart.GetResol()*Data.Chart.GetSize(), (int)Data.Chart.GetStartPos());
            VTimeChart.Waterfall.PushData(val_chart, Data.Chart.GetResol(), (int)Data.Chart.GetStartPos());
        }

        if (Data.Chart.GetSettingsUpdate()) {
//            SliderChartCount.setValue(Data.Chart.GetSize());
//            SliderChartResol.setValue(Data.Chart.GetResol()/10);
//            SliderChartStart.setValue((int)(Data.Chart.GetStartPos()/1000));
//            SliderChartPeriod.setValue((int)(Data.Chart.GetPeriod()));
        }
    }

    protected void CallbackGet_UART() {
    }
    protected void CallbackResp_UART(int code) {

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

    int GUI_ConvertIndexToItemChartCount(int index) {
        int item_cnt = (index + 1)*100;
        return item_cnt;
    }

    void GUI_SetItemChartCountByIndex(int index) {
        int item_cnt = GUI_ConvertIndexToItemChartCount(index);
        DataInterface.Chart.SetSize(item_cnt);
    }

    int GUI_GetItemChartCount() {
        return DataInterface.Chart.GetSize();
    }

    int GUI_ConvertIndexToResol(int index) {
        int resol = (index + 1) * 10;
        return resol;
    }

    void GUI_SetRangeByIndex(int index) {
        int resol = GUI_ConvertIndexToResol(index);
        DataInterface.Chart.SetResol(resol);
    }

    int GUI_GetRange() {
        return DataInterface.Chart.GetSize()*DataInterface.Chart.GetResol()+(int)DataInterface.Chart.GetStartPos();
    }

    int GUI_ConvertIndexToPeriod(int index) {
        int period = index*50;
        return period;
    }

    void GUI_SetPeriodByIndex(int index) {
        int period = GUI_ConvertIndexToPeriod(index);
        DataInterface.Chart.SetPeriod(period);
    }

    int GUI_GetPeriod() {
        return DataInterface.Chart.GetPeriod();
    }

    void GUI_DataChartSend() {
        Data.Chart.Set(DataInterface.Chart);
        SetChart();
    }

    int GUI_ConvertIndexToFreq(int index) {
        int freq = 180 + (index) * 10;
        return freq;
    }

    void GUI_SetFreqByIndex(int index) {
        int freq = GUI_ConvertIndexToFreq(index);
        DataInterface.Transc.SetFreq(freq);
    }

    int GUI_GetFreq() {
        return DataInterface.Transc.GetFreq();
    }

    int GUI_ConvertIndexToWidth(int index) {
        int width = index*2;
        return width;
    }

    void GUI_SetWidthByIndex(int index) {
        int width = GUI_ConvertIndexToWidth(index);
        DataInterface.Transc.SetWidth(width);
    }

    int GUI_GetWidth() {
        return DataInterface.Transc.GetWidth();
    }

    void GUI_SetBoostByIndex(int boost) {
        DataInterface.Transc.SetBoost(boost);
    }

    int GUI_GetBoost() {
        return DataInterface.Transc.GetBoost();
    }

    void GUI_DataTranscSend() {
        Data.Transc.Set(DataInterface.Transc);
        SetTransc();
    }

    int GUI_ConvertIndexToSoundSpeed(int index, int envir_index) {
        int speed = 0;
        if(envir_index == 0) { // water
            speed = 1380 + index*2;
        } else {
            speed = 800 + index*20;
        }

        return speed;
    }

    void GUI_SetSoundSpeedByIndex(int index, int envir_index) {
        int speed = GUI_ConvertIndexToSoundSpeed(index, envir_index);
        DataInterface.SoundSpeed_m_s = speed;
    }

    int GUI_GetSoundSpeed() {
        return DataInterface.SoundSpeed_m_s;
    }

    void GUI_DataSoundSpeedSend() {
        Data.SoundSpeed_m_s = DataInterface.SoundSpeed_m_s;
        SetSoundSpeed();
    }

    void GUI_SetUARTBaudrate(int baudrate) {
        DataInterface.UART.Baudrate = baudrate;
    }

    void GUI_SetBaudrateByIndex(int index) {
        int baudrate = 9600;
        switch (index) {
            case 0:
                baudrate = 9600;
                break;
            case 1:
                baudrate = 19200;
                break;
            case 2:
                baudrate = 38400;
                break;
            case 3:
                baudrate = 57600;
                break;
            case 4:
                baudrate = 115200;
                break;
            case 5:
                baudrate = 230400;
                break;
            case 6:
                baudrate = 460800;
                break;
            case 7:
                baudrate = 921600;
                break;
        }
        GUI_SetUARTBaudrate(baudrate);
    }

    void GUI_UARTSend() {
        Data.UART.Baudrate = DataInterface.UART.Baudrate;
        SetUART();
    }

    int GUI_GetBaudrate() {
        return (int)DataInterface.UART.Baudrate;
    }

}