package com.example.sonicapp;


class KoggerSonicBaseDriver_c extends ProtoSerial {
    static final short CMD_ID_Chart = 10;
    static final short CMD_ID_Array = 11;
    static final short CMD_ID_YPR = 12;
    static final short CMD_ID_QUAT = 13;
    static final short CMD_ID_TEMP = 14;
    static final short CMD_ID_AGC = 20;
    static final short CMD_ID_TRANSC = 21;
    static final short CMD_ID_SOUND_SPEED = 22;
    static final short CMD_ID_UART = 0x17;
    static final short CMD_ID_FLASH_SET = 0x1D;
    static final short CMD_ID_GNSS = 0x64;

    static final short Setting = 2;
    static final short Getting = 3;
    static final short Content = 4;
    static final short Action = 5;
    static final short Reaction = 6;
    static final short Response = 6;

    static final short RespNone = 0;
    static final short RespOk = 1;
    static final short RespErrorCheck = 2;
    static final short RespErrorPayload = 3;
    static final short RespErrorID = 4;
    static final short RespErrorVersion = 5;
    static final short RespErrorType = 6;
    static final short RespErrorKey = 7;
    static final short RespErrorRuntime = 8;

    static final long UART_KEY = 0xC96B5D4A;

    //ProtoSerial Proto;
    KoggerSonicData_c Data;
    KoggerSonicData_c DataInterface;

    KoggerSonicBaseDriver_c() {
        super(null);
        //Proto = new ProtoSerial(null);
        Data = new KoggerSonicData_c();
        DataInterface = new KoggerSonicData_c();
    }

    class ProtoInst extends ProtoInstance {
        ProtoInst(short id, short type, int version, boolean resp) {
            super(id, type, version, resp);
        }
        char ComputeLength() {
            char len = 0;
            switch(ID) {
                case CMD_ID_Chart:
                    if (Type == Action) {
                        len = 14;
                    } else if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_Array:
                    if (Type == Action) {
                        len = 20;
                    } else if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_YPR:
                    if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_TEMP:
                    if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_QUAT:
                    if (Type == Getting) {
                        len = 0;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_AGC:
                    if (Type == Getting) {
                        len = 0;
                    } else if (Type == Setting) {
                        len = 18;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_TRANSC:
                    if (Type == Getting) {
                        len = 0;
                    } else if (Type == Setting) {
                        len = 8;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_SOUND_SPEED:
                    if (Type == Getting) {
                        len = 0;
                    } else if (Type == Setting) {
                        len = 6;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;
                case CMD_ID_UART:
                    if (Type == Getting) {
                        len = 5;
                    } else if (Type == Setting) {
                        len = 13;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case CMD_ID_FLASH_SET:
                    if (Type == Action) {
                        len = 8;
                    } else {
                        return LENGTH_ERROR;
                    }
                    break;

                case CMD_ID_GNSS:
                    if (Type == Content) {
                        len = 20;
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
            long StartPos_mm = 0;
            int Size = 400;
            int Resol_mm = 10;
            int Offset;
            int Period_ms = 200;
            short Data[] = new short[5000];
            short DataPos;
            Boolean DataComplete = false;
            Boolean SettingsUpdate = false;

            void Set(ChartContent_c data) {
                StartPos_mm = data.StartPos_mm;
                Size = data.Size;
                Resol_mm = data.Resol_mm;
                Period_ms = data.Period_ms;
            }

            void SetStartPos(long start_pos_mm) {
                StartPos_mm = start_pos_mm;
            }

            long GetStartPos() {
                return StartPos_mm;
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

            void SetPeriod(int period_ms) {
                Period_ms = period_ms;
            }

            int GetPeriod() {
                return Period_ms;
            }

            void SetOffset(int offset) {
                if (offset == 0 && DataPos != 0) {
                    Size = DataPos + Offset;
                    DataComplete = true;
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

            void InitData(long start_pos_mm, int offset, int resol_mm) {
                SetStartPos(start_pos_mm);
                SetResol(resol_mm);
                SetOffset(offset);
                DataPos = 0;
            }

            void InitContent(long start_pos_mm, int size_chart, int resol_mm, int period_ms) {
                SetStartPos(start_pos_mm);
                SetResol(resol_mm);
                SetSize(size_chart);
                SetPeriod(period_ms);
                SettingsUpdate = true;
            }

            void AddData(short data) {
                Data[DataPos + Offset] = data;
                DataPos++;
            }
        }

        class YPRContent_c {
            float Yaw;
            float Pitch;
            float Roll;

            void Set(float yaw, float pitch, float roll) {
                Yaw = yaw;
                Pitch = pitch;
                Roll = roll;
            }
        }

        class AGCContent_c {
            long StartPos = 0;
            int Offset = 0;
            int Slope = 20;
            int Absorp = 0;

            void Set(long start_pos, int offset, int slope, int absorp) {
                StartPos = start_pos;
                Offset = offset;
                Slope = slope;
                Absorp = absorp;
            }

            int getStart() {
                return (int)StartPos;
            }

            int getOffset() {
                return Offset;
            }

            int getSlope() {
                return Slope;
            }

            int getAbsorp() {
                return Absorp;
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

            void Set(TransContent_c data) {
                SetFreq(data.Freq_khz);
                SetWidth(data.WidthPulse);
                SetBoost(data.Boost);
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

        ChartContent_c Chart;
        YPRContent_c YPR;
        float Temp;
        int SoundSpeed_m_s = 1500;
        AGCContent_c AGC;
        TransContent_c Transc;
        UARTContent_c UART;

        KoggerSonicData_c() {
            Chart = new ChartContent_c();
            YPR = new YPRContent_c();
            AGC = new AGCContent_c();
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
            case CMD_ID_Chart:
                parse_answer = ParsChart();
                break;

            case CMD_ID_YPR:
                parse_answer = ParceYPR();
                break;

            case CMD_ID_TEMP:
                parse_answer = ParceTemp();
                break;

            case CMD_ID_TRANSC:
                parse_answer = ParceTransc();
                break;

            case CMD_ID_SOUND_SPEED:
                parse_answer = ParceSoundSpeed();
                break;

            case CMD_ID_UART:
                parse_answer = ParceUART();
                break;

            default:
                break;
        }
    }

    protected void CallbackGet_Chart() {
    }

    protected void CallbackResp_Chart(int code) {
    }

    private int ParsChart() {
        if(In.Response) {
            CallbackResp_Chart(In.ReadU1());
        } else {
            if (In.Type == Reaction) {
                if (In.Ver == 0) {
                    long start_pos_mm = In.ReadU4();
                    int item_offset = In.ReadU2();
                    int item_resol_mm = In.ReadU2();
                    int data_len = In.GetReadAvailable();
                    Data.Chart.InitData(start_pos_mm, item_offset, item_resol_mm);
                    for (int i = 0; i < data_len; i++) {
                        short data = (short) (In.ReadU1());
                        Data.Chart.AddData(data);
                    }
                    CallbackGet_Chart();
                } else return RespErrorVersion;
            } else if (In.Type == Content) {
                if (In.Ver == 0) {
                    long start_pos_mm = In.ReadU4();
                    int item_cnt = In.ReadU2();
                    int item_resol_mm = In.ReadU2();
                    int item_repead_ms = In.ReadU2();
                    Data.Chart.InitContent(start_pos_mm, item_cnt, item_resol_mm, item_repead_ms);
                    CallbackGet_Chart();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackGet_YPR() {
    }

    protected void CallbackResp_YPR(int code) {
    }

    private int ParceYPR() {
        if(In.Response) {
            CallbackResp_YPR(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    short yaw = In.ReadS2();
                    short pitch = In.ReadS2();
                    short roll = In.ReadS2();
                    Data.YPR.Set((float) yaw * 0.01f, (float) pitch * 0.01f, (float) roll * 0.01f);
                    CallbackGet_YPR();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackGet_Temp() {
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
                    CallbackGet_Temp();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackGet_SoundSpeed() {
    }

    protected void CallbackResp_SoundSpeed(int code) {
    }

    private int ParceSoundSpeed() {
        if(In.Response) {
            CallbackResp_SoundSpeed(In.ReadU1());
        } else {
            if (In.Type == Content) {
                if (In.Ver == 0) {
                    Data.SoundSpeed_m_s = In.ReadU2();
                    CallbackGet_SoundSpeed();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackGet_UART() {
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
                    if (uart_key == UART_KEY) {
                        if (id == 1) Data.UART.Set((int) boudrate);
                        CallbackGet_UART();
                    } else return RespErrorKey;
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    protected void CallbackGet_Transc() {
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
                    CallbackGet_Transc();
                } else return RespErrorVersion;
            } else return RespErrorType;
        }
        return RespOk;
    }

    void SetChart() {
        Out.New(new ProtoInst(CMD_ID_Chart, Action, 0, true));
        Out.WriteU4(Data.Chart.GetStartPos());
        Out.WriteU2(Data.Chart.GetSize());
        Out.WriteU2(Data.Chart.GetResol());
        Out.WriteU2(Data.Chart.GetPeriod());
        Out.WriteU4(0);
        Out.End();
    }

    void GetChartSettings() {
        Out.New(new ProtoInst(CMD_ID_Chart, Getting, 0, true));
        Out.End();
    }

    void GetYPR() {
        Out.New(new ProtoInst(CMD_ID_YPR, Getting, 0, true));
        Out.End();
    }

    void GetTemp() {
        Out.New(new ProtoInst(CMD_ID_TEMP, Getting, 0, true));
        Out.End();
    }

    void SetTransc(int freq, short width_pulse, short boost) {
        Data.Transc.Set(freq, width_pulse, boost);
        SetTransc();
    }

    void SetTransc() {
        Out.New(new ProtoInst(CMD_ID_TRANSC, Setting, 0, true));
        Out.WriteU2(Data.Transc.Freq_khz);
        Out.WriteU1(Data.Transc.WidthPulse);
        Out.WriteU1(Data.Transc.Boost);
        Out.WriteU4(0);
        Out.End();
    }

    void GetTransc() {
        Out.New(new ProtoInst(CMD_ID_TRANSC, Getting, 0, true));
        Out.End();
    }

    void SetSoundSpeed(int sound_speed_m_s) {
        Data.SoundSpeed_m_s = sound_speed_m_s;
        SetSoundSpeed();
    }

    void SetSoundSpeed() {
        Out.New(new ProtoInst(CMD_ID_SOUND_SPEED, Setting, 0, true));
        Out.WriteU2(Data.SoundSpeed_m_s);
        Out.WriteU4(0); // Reseved
        Out.End();
    }

    void GetSoundSpeed() {
        Out.New(new ProtoInst(CMD_ID_SOUND_SPEED, Getting, 0, true));
        Out.End();
    }

    void SetUART() {
        Out.New(new ProtoInst(CMD_ID_UART, Setting, 0, true));
        Out.WriteU4(UART_KEY);
        Out.WriteU1((short)1);
        Out.WriteU4(Data.UART.Baudrate);
        Out.WriteU4(0); // Reseved
        Out.End();
    }

    void GetUART() {
        Out.New(new ProtoInst(CMD_ID_UART, Getting, 0, true));
        Out.WriteU4(UART_KEY);
        Out.WriteU1((short)1);
        Out.End();
    }

    void FlashAllSatting() {
        Out.New(new ProtoInst(CMD_ID_FLASH_SET, Action, 0, true));
        Out.WriteU4(UART_KEY);
        Out.WriteU4(0);
        Out.End();
    }

    void ReadAllSettings() {
        GetChartSettings();
        GetTransc();
        GetSoundSpeed();
        GetUART();
    }

    void WriteAllSettings() {
        CopyDataFromSource();
        SetChart();
        SetTransc();
        SetSoundSpeed();
        SetUART();
    }

    void CopyDataFromSource() {
    }
}