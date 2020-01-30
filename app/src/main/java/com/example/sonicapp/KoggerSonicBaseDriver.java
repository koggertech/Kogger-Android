package com.example.sonicapp;


class KoggerSonicBaseDriver_c extends ProtoSerial {
    static final short ID_NONE = 0;
    static final short ID_TIMESTAMP = 0x01;
    static final short ID_DIST = 0x02;
    static final short ID_CHART = 0x03;
    static final short ID_ATTITUDE = 0x04;
    static final short ID_TEMP = 0x05;

    static final short ID_DATASET = 0x10;
    static final short ID_DIST_SETUP = 0x11;
    static final short ID_CHART_SETUP = 0x12;
    static final short ID_DSP = 0x13;
    static final short ID_TRANSC = 0x14;
    static final short ID_SOUND = 0x15;
    static final short ID_PIN = 0x16;
    static final short ID_BUS = 0x17;
    static final short ID_UART = 0x18;
    static final short ID_I2C = 0x19;
    static final short ID_CAN = 0x1A;
    static final short ID_GYR_SETUP = 0x1B;
    static final short ID_ACC_SETUP = 0x1C;

    static final short ID_VERSION = 0x20;
    static final short ID_MARK = 0x21;
    static final short ID_DIAG = 0x22;
    static final short ID_FLASH = 0x23;
    static final short ID_BOOT = 0x24;
    static final short ID_FW_UPDATE = 0x25;

    static final short ID_GNSS = 0x64;


    static final short Content = 1;
    static final short Setting = 2;
    static final short Getting = 3;

    static final short RespNone = 0;
    static final short RespOk = 1;
    static final short RespErrorCheck = 2;
    static final short RespErrorPayload = 3;
    static final short RespErrorID = 4;
    static final short RespErrorVersion = 5;
    static final short RespErrorType = 6;
    static final short RespErrorKey = 7;
    static final short RespErrorRuntime = 8;

    static final long CONFIRM_KEY = 0xC96B5D4A;

    static final long MarkTimeout = 100;
    long MarkLastTime;
    boolean AutoRequestSettings = true;
    boolean AutoRequest = AutoRequestSettings;

    KoggerSonicData_c Data;

    public interface InterfaceListenerChart {
        void ReceiveComplete(long offset_mm, int resolution_mm, int count_samples, short[] data);
        void ReceiveNew();
        void ReceiveSetting(int count_samples, int resolution_mm, long offset_samples);
        void ReceiveResponse(int code);
    }

    public interface InterfaceListenerTransc {
        void ReceiveSetting();
        void ReceiveResponse(int code);
    }

    public interface InterfaceListenerSound {
        void ReceiveSetting();
        void ReceiveResponse(int code);
    }

    public interface InterfaceListenerDataset {
        void ReceiveSetting();
        void ReceiveResponse(int code);
    }


    public interface InterfaceListenerAttitude {
        void ReceiveYPR(float ypr[]);
        void ReceiveQuat(float q[]);
        void ReceiveResponse(int code);
    }

    public interface InterfaceListenerLoadUpdate {
        void ReceiveResponse(int code);
    }

    InterfaceListenerDataset ListenerDataset;
    InterfaceListenerChart ListenerChart;
    InterfaceListenerTransc ListenerTrance;
    InterfaceListenerSound ListenerSound;
    InterfaceListenerAttitude ListenerAttitude;
    InterfaceListenerLoadUpdate ListenerLoadUpdate;


    void SetListenerDataset(InterfaceListenerDataset listener) {
        ListenerDataset = listener;
    }

    void SetListenerChart(InterfaceListenerChart listener) {
        ListenerChart = listener;
    }

    void SetListenerTransc(InterfaceListenerTransc listener) {
        ListenerTrance = listener;
    }

    void SetListenerSound(InterfaceListenerSound listener) {
        ListenerSound = listener;
    }

    void SetListenerLoadUpdate(InterfaceListenerLoadUpdate listener) {
        ListenerLoadUpdate = listener;
    }


    void SetListenerAttitude(InterfaceListenerAttitude listener) {
        ListenerAttitude = listener;
    }

    public interface InterfaceListenerSerial {
        void ReceiveSetting(int baudrate, int id);
        void ReceiveResponse(int code);
    }
    InterfaceListenerChart InterfaceListenerSerial;

    KoggerSonicBaseDriver_c() {
        super(null);

        SetListenerChart(new InterfaceListenerChart() {
            public void ReceiveComplete(long offset_mm, int resolution_mm, int count_samples, short[] data) {
            }

            public void ReceiveNew() {
            }

            public void ReceiveSetting(int count_samples, int resolution_mm, long offset_samples) {
            }

            public void ReceiveResponse(int code) {
            }
        });

        SetListenerDataset(new InterfaceListenerDataset() {
            @Override
            public void ReceiveSetting() {

            }

            @Override
            public void ReceiveResponse(int code) {

            }
        });

        SetListenerTransc(new InterfaceListenerTransc() {
            @Override
            public void ReceiveSetting() {

            }

            @Override
            public void ReceiveResponse(int code) {

            }
        });

        SetListenerSound(new InterfaceListenerSound() {
            @Override
            public void ReceiveSetting() {

            }

            @Override
            public void ReceiveResponse(int code) {

            }
        });

        SetListenerLoadUpdate(new InterfaceListenerLoadUpdate() {
            @Override
            public void ReceiveResponse(int code) {

            }
        });

        Data = new KoggerSonicData_c();
    }

    void SetAutoRequestSettings(boolean request) {
        AutoRequestSettings = request;
        AutoRequest = AutoRequestSettings;
    }

    class ProtoInst extends ProtoInstance {
        ProtoInst(short id, short type, int version, boolean resp) {
            super(id, type, version, resp);
        }

        ProtoInst(short id, short type, int version, boolean resp, int len) {
            super(id, type, version, resp, len);
        }

        char ComputeLength() {
            char len = 0;
            switch(ID) {


                case ID_CHART:
                    if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_ATTITUDE:
                    if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_TEMP:
                    if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_DATASET:
                    if (Type == Getting) {
                        len = 1;
                    } else if (Type == Setting) {
                        len = 9;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_CHART_SETUP:
                    if (Type == Setting) {
                        len = 6;
                    } else if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_TRANSC:
                    if (Type == Getting) {
                        len = 0;
                    } else if (Type == Setting) {
                        len = 4;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_SOUND:
                    if (Type == Getting) {
                        len = 0;
                    } else if (Type == Setting) {
                        len = 4;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_UART:
                    if (Type == Getting) {
                        len = 5;
                    } else if (Type == Setting) {
                        len = 9;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_MARK:
                    if (Type == Setting) {
                        len = 4;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_FLASH:
                    if (Type == Setting) {
                        len = 4;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_GNSS:
                    if (Type == Content) {
                        len = 20;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case ID_BOOT:
                    if (Type == Setting) {
                        len = 4;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
            }
            return len;
        }
    }

    class KoggerSonicData_c {
        class ChartContent_c {
            final int MaxDist = 50000;
            int AbsOffset = 0;
            int Size = 400;
            int Resol_mm = 10;
            int Offset;
            short Data[] = new short[5000];
            short DataPos;
            Boolean DataComplete = false;
            Boolean SettingsUpdate = false;

            void Set(ChartContent_c data) {
                AbsOffset = data.AbsOffset;
                Size = data.Size;
                Resol_mm = data.Resol_mm;
            }

            void SetStartPos(int start_pos_mm) {
                AbsOffset = start_pos_mm;
            }

            int GetAbsOffset() {
                return AbsOffset;
            }

            void SetResol(int resol_mm) {
                if(resol_mm * Size <= MaxDist) {
                    Resol_mm = resol_mm;
                } else {
                    int resol = (MaxDist / Size) / 10;
                    Resol_mm = resol * 10;
                }
            }

            int GetResol() {
                return Resol_mm;
            }

            void SetSize(int size_chart) {
                Size = size_chart;
                if(Resol_mm * Size > MaxDist) {
                    int resol = (MaxDist / Size) / 10;
                    Resol_mm = resol * 10;
                }
            }

            int GetSize() {
                return Size;
            }

            int GetMaxDist_mm() {
                return Size*Resol_mm + AbsOffset;
            }

            void SetOffset(int offset) {
                if (offset == 0) {
                    Size = DataPos;
                    DataPos = 0;
                    if(Size > 0) {
                        DataComplete = true;
                    }
                }
                Offset = offset;
            }

            Boolean GetDataComlete() {
                if (DataComplete) {
                    DataComplete = false;
                    return true;
                }
                return false;
            }

            Boolean GetSettingsUpdate() {
                if (SettingsUpdate) {
                    SettingsUpdate = false;
                    return true;
                }
                return false;
            }

            void InitData(int start_pos_mm, int offset, int resol_mm) {
                SetStartPos(start_pos_mm);
                SetResol(resol_mm);
                SetOffset(offset);
//                DataPos = 0;
            }

            void InitContent(int start_pos_mm, int size_chart, int resol_mm) {
                SetStartPos(start_pos_mm);
                SetResol(resol_mm);
                SetSize(size_chart);
                SettingsUpdate = true;
            }

            void AddData(short data) {
                if(DataPos < Data.length) {
                    Data[DataPos] = data;
                    DataPos++;
                }
            }
        }

        class Attitude_c {
            float YPR[] = new float[3];
            float Quat[] = new float[4];

            void SetQuat(float q[]) {
                for(int i = 0; i < 4; i++) {
                    Quat[i] = q[i];
                }
            }

            void SetYPR(float ypr[]) {
                for(int i = 0; i < 3; i++) {
                    YPR[i] = ypr[i];
                }
            }
        }

        class TransContent_c {
            int Freq_khz = 670;
            short WidthPulse = 10;
            short Boost = 1;

            void Set(int freq_khz, short width_pulse, short boost) {
                SetFreq(freq_khz);
                SetWidth(width_pulse);
                SetBoost(boost);
            }

            int GetFreq() {
                return Freq_khz;
            }

            void SetFreq(int freq_khz) {
                Freq_khz= freq_khz;
            }

            int GetWidth() {
                return WidthPulse;
            }

            void SetWidth(int width) {
                WidthPulse = (short)width;
            }

            int GetBoost() {
                return Boost;
            }

            void SetBoost(int boost) {
                Boost = (short)boost;
            }
        }

        class UARTContent_c {
            long Baudrate = 115200;

            void Set(long baudrate) {
                Baudrate = baudrate;
            }

            long GetBaudrate() {
                return Baudrate;
            }
        }

        class Channel_c {
            int Period = 0;
            long Mask = 0x00;
            int ID = 1;

            void setPeriod(int period) {
                Period = period;
            }

            int getPeriod() {
                return Period;
            }

            void setChart(boolean en) {
                int bit_offset = 1;
                if(en) {
                    Mask |= (1<<bit_offset);
                } else {
                    Mask &= ~(1<<bit_offset);
                }
            }

            boolean getChart() {
                int bit_offset = 1;
                return (Mask & (1<<bit_offset)) == (1<<bit_offset);
            }

            void setTemp(boolean en) {
                int bit_offset = 4;
                if(en) {
                    Mask |= (1<<bit_offset);
                } else {
                    Mask &= ~(1<<bit_offset);
                }
            }

            boolean getTemp() {
                int bit_offset = 4;
                return (Mask & (1<<bit_offset)) == (1<<bit_offset);
            }

            void setQuat(boolean en) {
                int bit_offset = 3;
                if(en) {
                    Mask |= (1<<bit_offset);
                } else {
                    Mask &= ~(1<<bit_offset);
                }
            }

            boolean getQuat() {
                int bit_offset = 3;
                return (Mask & (1<<bit_offset)) == (1<<bit_offset);
            }

            void setDist(boolean en) {
                int bit_offset = 0;
                if(en) {
                    Mask |= (1<<bit_offset);
                } else {
                    Mask &= ~(1<<bit_offset);
                }
            }

            boolean getDist() {
                int bit_offset = 0;
                return (Mask & (1<<bit_offset)) == (1<<bit_offset);
            }

            void setDistNMEA(boolean en) {
                int bit_offset = 6;
                if(en) {
                    Mask |= (1<<bit_offset);
                } else {
                    Mask &= ~(1<<bit_offset);
                }
            }

            boolean getNMEA() {
                int bit_offset = 6;
                return (Mask & (1<<bit_offset)) == (1<<bit_offset);
            }
        }

        Channel_c Channel = new Channel_c();

        ChartContent_c Chart;
        Attitude_c Attitude;
        float Temp;
        int SoundSpeed_m_s = 1500;
        TransContent_c Transc;
        UARTContent_c UART;

        KoggerSonicData_c() {
            Chart = new ChartContent_c();
            Attitude = new Attitude_c();
            Transc = new TransContent_c();
            UART = new UARTContent_c();
        }
    }

    void Process() {
        while (In.Process()) {
            SwitchParsing(In.ID);
        }
    }

    private void SwitchParsing(int id) {
        int parse_answer = RespNone;
        switch(id) {
            case ID_DIST:
                parse_answer = ParsDist();
                break;

            case ID_CHART:
                parse_answer = ParsChart();
                break;

            case ID_ATTITUDE:
                parse_answer = ParceAttitude();
                break;

            case ID_TEMP:
                parse_answer = ParceTemp();
                break;

            case ID_DATASET:
                parse_answer = ParsDataset();
                break;

            case ID_DIST_SETUP:
                parse_answer = ParsDistSetup();
                break;

            case ID_CHART_SETUP:
                parse_answer = ParsChartSetup();
                break;

            case ID_TRANSC:
                parse_answer = ParceTransc();
                break;

            case ID_SOUND:
                parse_answer = ParceSoundSpeed();
                break;

            case ID_UART:
                parse_answer = ParceUART();
                break;

            case ID_FW_UPDATE:
                parse_answer = ParceLoadUpdate();
                break;
            default:
                break;
        }

        if(!In.Mark && (System.currentTimeMillis() - MarkLastTime > MarkTimeout)) {
            MarkLastTime = System.currentTimeMillis();
            SetMark();

            if(AutoRequest) {
                RequestAllSettings();
            }
        }
    }

    private int ParsDist() {
        if(In.Response) {
//            ListenerDist.ReceiveResponse(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    long dist = In.ReadU4();

                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    private int ParsChart() {
        if(In.Response) {
            ListenerChart.ReceiveResponse(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    int seq_offset = In.ReadU2();
                    int sample_resol_mm = In.ReadU2();
                    int abs_offset = In.ReadU2();

                    int data_len = In.GetReadAvailable();
                    Data.Chart.InitData(abs_offset, seq_offset, sample_resol_mm);

                    if(seq_offset == 0) {
                        ListenerChart.ReceiveNew();
                    }

                    for (int i = 0; i < data_len; i++) {
                        short data = (short) (In.ReadU1());
                        Data.Chart.AddData(data);
                    }

                    if (Data.Chart.GetDataComlete()) {
                        ListenerChart.ReceiveComplete(Data.Chart.AbsOffset, Data.Chart.Resol_mm,  Data.Chart.GetSize(), Data.Chart.Data);
                    }
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackReceive_Attitude(int ver) {
        if(ver == 0) {
            ListenerAttitude.ReceiveYPR(Data.Attitude.YPR);
        } else if(ver == 1) {
            ListenerAttitude.ReceiveQuat(Data.Attitude.Quat);
        }
    }

    protected void CallbackResp_Attitude(int code) {
    }

    private int ParceAttitude() {
        if(In.Response) {
            CallbackResp_Attitude(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    short yaw = In.ReadS2();
                    short pitch = In.ReadS2();
                    short roll = In.ReadS2();

                    float ypr[] = new float[3];
                    float presc = 0.01f;
                    ypr[0]= (float) yaw * presc;
                    ypr[1]= (float) pitch * presc;
                    ypr[2]= (float) roll * presc;

                    Data.Attitude.SetYPR(ypr);
                    CallbackReceive_Attitude(In.Ver);
                } else if (In.Ver == 1) {
                    short q0 = In.ReadS2();
                    short q1 = In.ReadS2();
                    short q2 = In.ReadS2();
                    short q3 = In.ReadS2();

                    float quat[] = new float[4];
                    float presc = 1/32767;
                    quat[0]= (float) q0 * presc;
                    quat[1]= (float) q1 * presc;
                    quat[2]= (float) q2 * presc;
                    quat[2]= (float) q3 * presc;

                    Data.Attitude.SetQuat(quat);
                    CallbackReceive_Attitude(In.Ver);
                }  else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackReceive_Temp(int ver) {
    }

    protected void CallbackResp_Temp(int code) {
    }

    private int ParceTemp() {
        if(In.Response) {
            CallbackResp_Temp(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    Data.Temp = (float)(In.ReadS2())*0.01f;
                    CallbackReceive_Temp(In.Ver);
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    private int ParsDataset() {
        if(In.Response) {
            ListenerDataset.ReceiveResponse(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    int channel_id =  In.ReadU1();
                    Data.Channel.Period  = (int)In.ReadU4();
                    Data.Channel.Mask  = (int)In.ReadU4();

                    ListenerDataset.ReceiveSetting();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    private int ParsDistSetup() {
        if(In.Response) {
            ListenerChart.ReceiveResponse(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {

                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    private int ParsChartSetup() {
        if(In.Response) {
            ListenerChart.ReceiveResponse(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    int sample_cnt = In.ReadU2();
                    int sample_resol_mm = In.ReadU2();
                    int abs_offset = In.ReadU2();
                    Data.Chart.InitContent(abs_offset, sample_cnt, sample_resol_mm);
                    ListenerChart.ReceiveSetting(sample_cnt, sample_resol_mm, abs_offset);
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackRequest_Transc() {
        ListenerTrance.ReceiveSetting();
    }

    protected void CallbackResp_Transc(int code) {
    }

    private int ParceTransc() {
        if(In.Response) {
            CallbackResp_Transc(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    int freq_khz = In.ReadU2();
                    short width_pulse = In.ReadU1();
                    short boost = In.ReadU1();
                    Data.Transc.Set(freq_khz, width_pulse, boost);
                    CallbackRequest_Transc();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackReceive_SoundSpeed(int ver) {
        ListenerSound.ReceiveSetting();
    }

    protected void CallbackResp_SoundSpeed(int code) {
    }

    private int ParceSoundSpeed() {
        if(In.Response) {
            CallbackResp_SoundSpeed(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    Data.SoundSpeed_m_s = (int)In.ReadU4()/1000;
                    CallbackReceive_SoundSpeed(In.Ver);
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackReceive_UART(int ver) {
    }

    protected void CallbackResp_UART(int code) {
    }

    private int ParceUART() {
        if(In.Response) {
            CallbackResp_UART(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    long uart_key = In.ReadU4();
                    int id = In.ReadU1();
                    long boudrate = In.ReadU4();
                    if (uart_key == CONFIRM_KEY) {
                        if (id == 1) Data.UART.Set((int) boudrate);
                        CallbackReceive_UART(In.Ver);
                    } else return RespErrorKey;
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    private int ParceLoadUpdate() {
        if(In.Response) {
            ListenerLoadUpdate.ReceiveResponse(In.ReadU1());
        } else {
            if (In.Type == Setting) {
                if (In.Ver == 0) {

                } else return RespErrorVersion;
            } else if (In.Type == Content) {
                if (In.Ver == 0) {

                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    void RequestChart() {
        Out.New(new ProtoInst(ID_CHART, Getting, 0, true));
        Out.End();
    }

    void RequstAttitudeInYPR() {
        Out.New(new ProtoInst(ID_ATTITUDE, Getting, 0, true));
        Out.End();
    }

    void RequestAttitudeInQuat() {
        Out.New(new ProtoInst(ID_ATTITUDE, Getting, 1, true));
        Out.End();
    }

    void RequestTemperature() {
        Out.New(new ProtoInst(ID_TEMP, Getting, 0, true));
        Out.End();
    }

    void RequestChannel() {
        Out.New(new ProtoInst(ID_DATASET, Getting, 0, true));
        Out.WriteU1((short)(1));
        Out.End();
    }

    void SendChannel() {
        Out.New(new ProtoInst(ID_DATASET, Setting, 0, true));
        Out.WriteU1((short)1);
        Out.WriteU4(Data.Channel.getPeriod());
        Out.WriteU4(Data.Channel.Mask);
        Out.End();
    }

    void RequestChartSetup() {
        Out.New(new ProtoInst(ID_CHART_SETUP, Getting, 0, true));
        Out.End();
    }

    void SendChartSetup() {
        Out.New(new ProtoInst(ID_CHART_SETUP, Setting, 0, true));
        Out.WriteU2(Data.Chart.GetSize());
        Out.WriteU2(Data.Chart.GetResol());
        Out.WriteU2(Data.Chart.GetAbsOffset());
        Out.End();
    }


    void RequestTransc() {
        Out.New(new ProtoInst(ID_TRANSC, Getting, 0, true));
        Out.End();
    }

    void SendTransc() {
        Out.New(new ProtoInst(ID_TRANSC, Setting, 0, true));
        Out.WriteU2(Data.Transc.Freq_khz);
        Out.WriteU1(Data.Transc.WidthPulse);
        Out.WriteU1(Data.Transc.Boost);
        Out.End();
    }

    void SetTransc(int freq, short width_pulse, short boost) {
        Data.Transc.Set(freq, width_pulse, boost);
        SendTransc();
    }


    void RequestSoundSpeed() {
        Out.New(new ProtoInst(ID_SOUND, Getting, 0, true));
        Out.End();
    }

    void SendSoundSpeed() {
        Out.New(new ProtoInst(ID_SOUND, Setting, 0, true));
        Out.WriteU4(Data.SoundSpeed_m_s*1000);
        Out.End();
    }

    void SetSoundSpeed(int sound_speed_m_s) {
        Data.SoundSpeed_m_s = sound_speed_m_s;
        SendSoundSpeed();
    }

    void SendUART() {
        Out.New(new ProtoInst(ID_UART, Setting, 0, true));
        Out.WriteU4(CONFIRM_KEY);
        Out.WriteU1((short)1);
        Out.WriteU4(Data.UART.Baudrate);
        Out.End();
    }

    void RequestUART() {
        Out.New(new ProtoInst(ID_UART, Getting, 0, true));
        Out.WriteU4(CONFIRM_KEY);
        Out.WriteU1((short)0);
        Out.End();
    }


    void SetMark() {
        Out.New(new ProtoInst(ID_MARK, Setting, 0, false));
        Out.WriteU4(CONFIRM_KEY);
        Out.End();
    }

    void SaveAllSettings() {
        Out.New(new ProtoInst(ID_FLASH, Setting, 0, true));
        Out.WriteU4(CONFIRM_KEY);
        Out.End();
    }

    void RestoreAllSettings() {
        Out.New(new ProtoInst(ID_FLASH, Setting, 2, true));
        Out.WriteU4(CONFIRM_KEY);
        Out.End();
    }

    void BootRun() {
        Out.New(new ProtoInst(ID_BOOT, Setting, 0, true));
        Out.WriteU4(CONFIRM_KEY);
        Out.End();
    }

    void FWRun() {
        Out.New(new ProtoInst(ID_BOOT, Setting, 1, true));
        Out.WriteU4(CONFIRM_KEY);
        Out.End();
        AutoRequest = AutoRequestSettings;
    }

    void LoadUpdatePart(int nbr_msg, byte[] data) {
        AutoRequest = false;
        Out.New(new ProtoInst(ID_FW_UPDATE, Setting, 0, true, 2 + data.length));
        Out.WriteU2(nbr_msg);
        for(int i = 0; i < data.length; i++) {
            Out.WriteS1(data[i]);
        }
        Out.End();
    }

    void RequestAllSettings() {
        RequestChannel();
        RequestChartSetup();
        RequestTransc();
        RequestSoundSpeed();
        RequestUART();
    }

    void SendAllSettings() {
        SendChannel();
        SendChartSetup();
        SendTransc();
        SendSoundSpeed();
        SendUART();
    }

    void CopyDataFromSource() {
    }
}