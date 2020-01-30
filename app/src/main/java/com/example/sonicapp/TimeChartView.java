package com.example.sonicapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import static java.lang.Math.sqrt;

class TimeChartView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder holder;
    private Thread drawThread;
    private boolean drawingActive = false;

    private static final int MAX_FPS = (int) (30.0);
    private static final int MAX_FRAME_TIME = (int) (1000.0 / MAX_FPS);


    WaterfallChart Waterfall, Waterfall2;

    class WaterfallChart {
        class WaterfallLine_c {
            private int Raw[];
            private int Resol;
            private int Offset;

            private Bitmap Cash;
            private int Width, Height;
            private int MinThr, MaxThr;
            private int CashRange, CashOffset;
            private boolean NeedCreateCash;
            private int ColorScheme;

            public WaterfallLine_c() {
            }

            void Init(int width, int height) {
                Width = width;
                Height = height;
                //ColorCash = new int[Height * Width];
                Cash = Bitmap.createBitmap(Width, Height, Bitmap.Config.RGB_565);
            }

            int GetRange() {
                int raw_lenhth = 0;
                if(Raw != null) {
                    raw_lenhth = Raw.length;
                }
                return raw_lenhth*Resol + Offset;
            }

            void AddRaw(int data[], int resol, int offset) {
                Raw = new int[data.length];
                for(int i = 0; i < data.length; i++) {
                    Raw[i] = data[i];
                }
                Resol = resol;
                Offset = offset;
                NeedCreateCash = true;
            }

            void Draw(Canvas c, float x, float y, int min_val, int max_val, int range, int offset, int color_scheme) {
                if(InitCash(min_val, max_val, range, offset, color_scheme)) {
                    c.drawBitmap(Cash, x, y, new Paint());
                }
            }

            int GetWidth() {
                return Width;
            }

            int GetRawValueFromRange(int start, int end) {
                int answer = 0;

                int start_index = (start - Offset)/Resol;
                int end_index = (end - Offset)/Resol;

                if(start_index >= Raw.length || start_index < 0) {
                    return 0;
                }

                if(end_index > Raw.length) {
                    end_index = Raw.length;
                }

                int val = Raw[start_index];
                for(int i = start_index + 1; i < end_index; i++) {
                    if(Raw[i] > val) {
                        val = Raw[i];
                    }
                }

                answer = val;

                return answer;
            }

            private boolean InitCash(int min_val, int max_val, int main_range, int main_offset, int color_scheme) {
                boolean need_update = NeedCreateCash;

                if(MinThr != min_val || MaxThr != max_val) {
                    MinThr = min_val;
                    MaxThr = max_val;
                    need_update = true;
                }

                if(CashRange != main_range || CashOffset != main_offset) {
                    CashRange = main_range;
                    CashOffset = main_offset;
                    need_update = true;
                }

                if(ColorScheme != color_scheme) {
                    ColorScheme = color_scheme;
                    need_update = true;
                }

                if(Raw == null){
                    Cash.eraseColor(Color.BLACK);
                } else if (need_update) {
                    float one_pixel_range = (float) (main_range) / (float) (Height);

                    int ColorCash[] = new int[Height * Width];

                    for (int i = 0; i < Height; i++) {
                        int pixel_dist = (int) ((float) i * one_pixel_range) + main_offset;
                        int next_pixel_dist = (int) ((float) (i + 1) * one_pixel_range) + main_offset;

                        float data = GetRawValueFromRange(pixel_dist, next_pixel_dist);

                        data -= (float) min_val;
                        data *= 255.0f / (float) (max_val - min_val);

                        data = Math.max(data, 0);
                        data = Math.min(data, 255);

                        int clr = 0;

                        switch(ColorScheme) {
                            case 0:
                                clr = DataToBlueColor(data);
                                break;
                            case 1:
                                clr = DataToSepiaColor(data);
                                break;
                            case 2:
                                clr = DataToRainbowColor(data);
                                break;
                        }


                        for (int img_x = 0; img_x < Width; img_x++) {
                            ColorCash[i * Width + img_x] = clr;
                        }
                    }

                    Cash.setPixels(ColorCash, 0, Width, 0, 0, Width, Height);
                }

                NeedCreateCash = false;

                return (Cash != null);
            }

//            void ClearCash() {
//                Cash = null;
//            }

            private int DataToBlueColor(float data) {
                //float color_line = (float)sqrt((float)(data)*2)*20 - 10;
                float color_line = (float)sqrt(data)*16;

                int r = (int)(color_line*0.5f - 20);
                int g = (int)(color_line*0.8f);
                int b = (int)(color_line*1.4f);

                if( r > 255) {
                    r = 255;
                } else if(r < 0) {
                    r = 0;
                }

                if( g > 255) {
                    g = 255;
                } else if(g < 0) {
                    g = 0;
                }

                if( b > 255) {
                    b = 255;
                } else if(b < 0) {
                    b = 0;
                }

                return Color.rgb(r, g, b);
            }

            private int DataToSepiaColor(float data) {
                //float color_line = (float)sqrt((float)(data)*2)*20 - 10;
                float color_line = (float)sqrt(data)*16;

                int r = (int)(color_line*1.1f);
                int g = (int)(color_line*0.9f);
                int b = (int)(color_line*0.4f - 20);

                if( r > 255) {
                    r = 255;
                } else if(r < 0) {
                    r = 0;
                }

                if( g > 255) {
                    g = 255;
                } else if(g < 0) {
                    g = 0;
                }

                if( b > 255) {
                    b = 255;
                } else if(b < 0) {
                    b = 0;
                }

                return Color.rgb(r, g, b);
            }

            private int DataToRainbowColor(float data) {
                float color_line = data;

                float h = (255.0f - color_line*1.5f);
                float s = (1.0f);
                float v = (float)sqrt(color_line)/16.0f;

                if( h > 255.0f) {
                    h = 255.0f;
                } else if(h < 0.0f) {
                    s = 1.0f + h/100;
                    h = 0.0f;
                }

                if( s > 1.0f) {
                    s = 1.0f;
                } else if(s < 0.0f) {
                    s = 0.0f;
                }

                if( v > 1.0f) {
                    v = 1.0f;
                } else if(v < 0.0f) {
                    v = 0.0f;
                }

                float[] hsv = new float[3];
                hsv[0] = h;
                hsv[1] = s;
                hsv[2] = v;

                //return Color.HSVToColor(hsv);

                float r = 0;
                float g = 0;
                float b = 0;

                float      hh, p, q, t, ff;
                int        i;
                //rgb         out;

                if(s <= 0.0f) {       // < is bogus, just shuts up warnings
                    r = v;
                    g = v;
                    b = v;
                    return Color.rgb((int)(r*255.0f), (int)(g*255.0f), (int)(b*255.0f));
                }

                hh = h;
                if(hh >= 360.0f) hh = 0.0f;
                hh /= 60.0f;
                i = (int)hh;
                ff = hh - i;
                p = v * (1.0f - s);
                q = v * (1.0f - (s * ff));
                t = v * (1.0f - (s * (1.0f - ff)));

                switch(i) {
                    case 0:
                        r = v;
                        g = t;
                        b = p;
                        break;
                    case 1:
                        r = q;
                        g = v;
                        b = p;
                        break;
                    case 2:
                        r = p;
                        g = v;
                        b = t;
                        break;

                    case 3:
                        r = p;
                        g = q;
                        b = v;
                        break;
                    case 4:
                        r = t;
                        g = p;
                        b = v;
                        break;
                    case 5:
                    default:
                        r = v;
                        g = p;
                        b = q;
                        break;
                }

                return Color.rgb((int)(r*255.0f), (int)(g*255.0f), (int)(b*255.0f));
            }
        }

        WaterfallLine_c Line[];

        private int SizeX = 0, SizeY = 0, PosX, PosY,  CntLineForCurr, WidthCurr, WidthPlot;
        private int TimeCnt = 0;
        private int PrintLineCnt = 0;
        private int data_pos = 0, last_push_index;
        private int MinThr = 25, MaxThr = 250;
        private int TouchX, TouchY;
        private int Range_mm, Offset_mm;
        private int ColorScheme = 0;

        public WaterfallChart(int size_x, int size_y, int width_line) {
            Resize(size_x, size_y, width_line);
        }

        public void Resize(int size_x, int size_y, int width_line) {
            SizeX = size_x;
            SizeY = size_y;

            TimeCnt = SizeX / width_line;
            CntLineForCurr = SizeX/10/width_line;
            WidthCurr = CntLineForCurr*width_line;
            WidthPlot = SizeX - WidthCurr;

            int line_cnt = size_x / width_line;
            if (line_cnt > TimeCnt) {
                width_line = size_x / TimeCnt;
                line_cnt = TimeCnt;
            }

            PrintLineCnt = line_cnt;

            Line = new WaterfallLine_c[TimeCnt];
            for (int i = 0; i < Line.length; i++) {
                Line[i] = new WaterfallLine_c();
                Line[i].Init(width_line, size_y);
            }
        }

        void SetTouchPosition(int x, int y) {
            if(x > SizeX - WidthCurr) {
                TouchX = SizeX - WidthCurr;
            } else {
                TouchX = x;
            }

            TouchY = y;
        }

        /*private void ClearCash() {
            if(Line != null) {
                for (int i = 0; i < Line.length; i++) {
                    if(Line[i] != null) {
                        Line[i].ClearCash();
                    }
                }
            }
        }*/

        void DrawLine(Canvas c) {
            for (int k = 1; k <= PrintLineCnt; k++) {
                int buf_pos = data_pos - k;
                if (buf_pos < 0) {
                    buf_pos += TimeCnt;
                }
                Line[buf_pos].Draw(c, PosX + SizeX - (k + CntLineForCurr) * Line[buf_pos].GetWidth(), PosY, MinThr, MaxThr, Range_mm, Offset_mm, ColorScheme);
            }
        }

        void DrawLastLine(Canvas c) {
            for (int k = 1; k <= CntLineForCurr; k++) {
                Line[last_push_index].Draw(c, PosX + SizeX - k * Line[last_push_index].GetWidth(), PosY, MinThr, MaxThr, Range_mm, Offset_mm, ColorScheme);
            }
        }

        void Draw(Canvas c) {
            DrawLine(c);
            DrawLastLine(c);

            Paint paint = new Paint();
            paint.setStrokeWidth(2);
            paint.setColor(Color.WHITE);
            paint.setAlpha(170);
            paint.setAntiAlias(true);
            paint.setTextSize(40);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.RIGHT);

            int nbr_div = 5;

            for (int i = 1; i < nbr_div; i++) {
                float offset_y = (float)SizeY*(float)i/(float)nbr_div;
                paint.setAlpha(100);
                c.drawLine(PosX, PosY + offset_y, PosX + SizeX, PosY + offset_y, paint);
                String range_text;
                range_text = "" + (float)(Range_mm*i/nbr_div + Offset_mm)/1000 + " m";
                paint.setAlpha(170);
                c.drawText(range_text, PosX + SizeX - WidthCurr - 10, PosY + offset_y - 10, paint);
            }

            paint.setAlpha(100);
            c.drawLine(PosX + SizeX - WidthCurr, PosY, SizeX - WidthCurr, SizeY, paint);

//            paint.setAlpha(170);
//            c.drawLine(TouchX, 0, TouchX, TouchY - 10, paint);
//            c.drawLine(TouchX, TouchY + 10, TouchX, SizeY, paint);
//
//            c.drawLine(TouchX - 10, TouchY + 10 - 1, TouchX + 10, TouchY + 10 - 1, paint);
//            c.drawLine(TouchX - 10, TouchY - 10 + 1, TouchX + 10, TouchY - 10 + 1, paint);
//
//            int dist_cursor_cm = (int)(((float)(Range_mm + Offset_mm)/1000.0f)*((float)TouchY/(float)SizeY)*100);
//            String cursor_dist_text;
//            cursor_dist_text = "" + ((float)(dist_cursor_cm)/100) + " m";
//            paint.setAlpha(170);
//            paint.setTextAlign(Paint.Align.LEFT);
//            c.drawText(cursor_dist_text, TouchX + 20, TouchY + 13, paint);

            paint.setColor(Color.WHITE);
            paint.setAlpha(200);
            paint.setTextSize(60);

            String range_text;
            range_text = "" + (float)(Range_mm + Offset_mm)/1000 + " m";
            paint.setTextAlign(Paint.Align.RIGHT);
            c.drawText(range_text, PosX + SizeX - WidthCurr - 10, PosY + SizeY - 10, paint);
        }

        public void PushData(int data[], int resol, int offset) {
            Offset_mm = offset;
            Range_mm = data.length*resol + Offset_mm;
            Line[data_pos].AddRaw(data, resol, Offset_mm);
            last_push_index = data_pos;

            data_pos++;
            if (data_pos == TimeCnt) {
                data_pos = 0;
            }
        }

        void SetMinThr(int thr) {
            MinThr = thr;
        }

        void SetMaxThr(int thr) {
            MaxThr = thr;
        }

        void SetColorScheme(int scheme) {
            ColorScheme = scheme;
        }
    }


    public void render(Canvas c) {
        if(c == null) {
            return;
        }
        c.drawColor(Color.BLACK);
        Waterfall.Draw(c);
//        Waterfall2.Draw(c);
    }

    public TimeChartView(Context context, LinearLayout linearLayout) {
        super(context);
        getHolder().addCallback(this);
        linearLayout.addView(this);
        Waterfall = new WaterfallChart(1200, 350, 4);
        Waterfall.PosY = 0;
        Waterfall2 = new WaterfallChart(1200, 350, 4);
        Waterfall2.PosY = Waterfall.SizeY;
        setWillNotDraw(false);
        holder = getHolder();

        super.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        Waterfall.SetTouchPosition((int)event.getX(), (int)event.getY());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return false;
            }
        });
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_MOVE:
//                Waterfall.SetTouchPosition((int)event.getX(), (int)event.getY());
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                break;
//        }
//        return true;
//    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Waterfall = new WaterfallChart(width, height, 4);
        Waterfall.PosY = 0;
//        Waterfall2 = new WaterfallChart(width, height/2, 4);
//        Waterfall2.PosY = Waterfall.SizeY;

        stopDrawThread();
        if(holder != null) {
            startDrawThread();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder new_holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
    }

    public void stopDrawThread() {
        if (drawThread == null) {
            return;
        }

        drawingActive = false;
        try {
            drawThread.join();
        } catch (Exception e) {
        }

        drawThread = null;
    }

    public void startDrawThread() {
        if(drawThread == null) {
            drawThread = new Thread(this, "Draw thread");
            drawingActive = true;
            drawThread.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas)  {
        super.onDraw(canvas);
        render(canvas);
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while (drawingActive) {
            canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    synchronized (holder) {
                        postInvalidate();
                    }
                }
            }
            finally {
                if (canvas != null) {
                    synchronized (holder) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}