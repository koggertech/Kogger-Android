package com.example.sonicapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

interface ListenerControlUpdate {
    void Update(int value);
}

class CustomSeekBar extends SeekBar {
    TextView TextV;
    String StartText = "";
    int CaseValues[] = {};
    ListenerControlUpdate ListenerUpdater;

    public CustomSeekBar(Context context) {
        super(context);
        setL();
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setL();
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setL();
    }

    public void setListenerUpdate(ListenerControlUpdate listener) {
        ListenerUpdater = listener;
    }

    public void SetCaseValues(int case_values[]) {
        setMax(case_values.length - 1);
        CaseValues = case_values;
    }

    private void setL() {
        setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    int value = getValue();
                    if (TextV != null) {
                        TextV.setText(ValueToText(value));
                    }

                    if (ListenerUpdater != null) {
                        ListenerUpdater.Update(value);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    protected String ValueToText(int value) {
        String t = StartText;
        t += value;
        return t;
    }

    public void setValue(int value) {
        setProgress(ValueToProgress(value));
        if(TextV != null) {
            TextV.setText(ValueToText(value));
        }
    }

    public int getValue() {
        int progress = getProgress();
        int samples_cnt = 0;

        if(progress < CaseValues.length) {
            samples_cnt = CaseValues[progress];
        } else {
            samples_cnt = CaseValues[CaseValues.length - 1];
        }

        return samples_cnt;
    }

    protected int ValueToProgress(int value) {
        int progress = 0;

        for(int i = 0; i < CaseValues.length; i++) {

            if(value < CaseValues[i]) {
                if(i != 0) {
                    int dif_prev_case = Math.abs(CaseValues[i - 1] - value);
                    int dif_case = Math.abs(CaseValues[i] - value);

                    if(dif_prev_case > dif_case) {
                        progress = i;
                    }
                }

                break;
            }

            progress = i;
        }

        return progress;
    }
}
//
//class SeekBarBaudrate extends CustomSeekBar{
//    public SeekBarBaudrate(Context context) {
//        super(context);
//    }
//    public SeekBarBaudrate(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//    public SeekBarBaudrate(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }
//
////    public int getValue() {
////        int progress = getProgress();
////
////        int baudrate = 9600;
////        switch (progress) {
////            case 0:
////                baudrate = 9600;
////                break;
////            case 1:
////                baudrate = 19200;
////                break;
////            case 2:
////                baudrate = 38400;
////                break;
////            case 3:
////                baudrate = 57600;
////                break;
////            case 4:
////                baudrate = 115200;
////                break;
////            case 5:
////                baudrate = 230400;
////                break;
////            case 6:
////                baudrate = 460800;
////                break;
////            case 7:
////                baudrate = 921600;
////                break;
////        }
////        return baudrate;
////    }
////
////    protected int ValueToProgress(int value) {
////        int progress = 0;
////        switch (value) {
////            case 9600:
////                progress = 0;
////                break;
////            case 19200:
////                progress = 1;
////                break;
////            case 38400:
////                progress = 2;
////                break;
////            case 57600:
////                progress = 3;
////                break;
////            case 115200:
////                progress = 4;
////                break;
////            case 230400:
////                progress = 5;
////                break;
////            case 460800:
////                progress = 6;
////                break;
////            case 921600:
////                progress = 7;
////                break;
////        }
////
////        return progress;
////    }
//}
//
//class SeekBarSampleCount extends CustomSeekBar{
//
//
//    public SeekBarSampleCount(Context context) {
//        super(context);
//    }
//    public SeekBarSampleCount(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//    public SeekBarSampleCount(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }
//
//    protected String ValueToText(int value) {
//        String t = "Samples: ";
//        t += value;
//        return t;
//    }
//
//    public int getValue() {
//        int progress = getProgress();
//        int samples_cnt = 0;
//
//        if(progress < CaseValues.length) {
//            samples_cnt = CaseValues[progress];
//        } else {
//            samples_cnt = CaseValues[CaseValues.length - 1];
//        }
//
//        return samples_cnt;
//    }
//
//    protected int ValueToProgress(int value) {
//        int progress = 0;
//
//        for(int i = 0; i < CaseValues.length; i++) {
//
//            if(value < CaseValues[i]) {
//                if(i != 0) {
//                    int dif_prev_case = Math.abs(CaseValues[i - 1] - value);
//                    int dif_case = Math.abs(CaseValues[i] - value);
//
//                    if(dif_prev_case > dif_case) {
//                        progress = i;
//                    }
//                }
//
//                break;
//            }
//
//            progress = i;
//        }
//
//        return progress;
//    }
//}

public class CustomViewBehavior {
}
