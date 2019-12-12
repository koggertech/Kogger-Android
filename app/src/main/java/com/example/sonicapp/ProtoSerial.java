package com.example.sonicapp;

import java.io.ByteArrayOutputStream;

class OutputStreamExt extends ByteArrayOutputStream {
    OutputStreamExt() {
        super();
    }

    void writeByte(char data[]) {
        byte data_b[] = new byte[data.length];
        for(int i = 0; i < data.length; i++) {
            data_b[i] = (byte)(data[i] & 0xFF);
        }
        write(data_b, 0, data_b.length);
    }

    void writeByte(byte data) {
        byte data_b[] = new byte[1];
        data_b[0] = data;
        write(data_b, 0, data_b.length);
    }

    void writeByte(byte[] data) {
        write(data, 0, data.length);
    }

    void writeByte(char data) {
        writeByte((byte)(data & 0xFF));
    }
}

class Serial{
    UsbService usbService;

    byte Data[];
    int pos_w = 0;
    int pos_r = 0;

    Serial() {
        usbService = null;
        Data = new byte[32768];
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
}

class LogStream {
    LogStream() {
    }
    synchronized OutputStreamExt GetStream() {
        return new OutputStreamExt();
    }
    void Write(OutputStreamExt data) {
    }
}

public class ProtoSerial {
    enum ProtoState {
        StateSync1,
        StateSync2,
        StateLength1,
        StateMode,
        StateID,
        StatePayload,
        StateCRCA,
        StateCRCB
    }

    Serial Port;
    ProtoIn In;
    ProtoOut Out;
    LogStream Log;

    ProtoSerial(Serial port) {
        In = new ProtoIn(null);
        Out = new ProtoOut(null);
        Log = new LogStream();
        SetPort(port);
    }

    void SetPort(Serial port) {
        Port = port;
        In.SetPort(Port);
        Out.SetPort(Port);
    }

    class ProtoInstance {
        final char LENGTH_ERROR = 0xFFFF;
        char Length;
        char ID;
        char Type;
        char Version;
        boolean Mark;
        boolean Response;

        ProtoInstance() {
            Length = 0;
            ID = 0;
            Type = 0;
            Version = 0;
            Mark = false;
            Response = false;
        }

        ProtoInstance(short id, short type, int version, boolean resp) {
            Set((char)id, (char)type, (char)version, resp);
        }

        void Set(char id, char type, char version, boolean resp) {
            ID = id;
            Type = type;
            Version = version;
            Response = resp;
            Length = ComputeLength();
        }

        char ComputeLength() {
            // default
            return LENGTH_ERROR;
        }

        boolean CheckLenhth() {
            if(Length != LENGTH_ERROR && Length == ComputeLength()) {
                return true;
            } else {
                return false;
            }
        }

        void SetID(char id) {
            ID = id;
        }

        void SetMode(char mode) {
            Type = (char)(mode & 0x7);
            Version = (char)((mode >> 3) & 0x3);
            int resp = (mode >> 7) & 0x01;
            if(resp == 1) {
                Response = true;
            } else {
                Response = false;
            }
        }

        char GetMode() {
            short resp_int = 0;
            if(Response) {
                resp_int = 1;
            }
            return  (char)(((Type & 0x7) | ((Version & 0x3) << 3) | ((resp_int & 0x1) << 7)));
        }
    }

    class ProtoBase {
        final char HEAD1 = 0xBB;
        final char HEAD2 = 0x55;

        char PayloadCheckSumA;
        char PayloadCheckSumB;

        Serial Port;

        void SetPort(Serial serial) {
            Port = serial;
        }

        void CheckSumUpdate(byte b) {
            PayloadCheckSumA += b;
            PayloadCheckSumA &= 0xFF;
            PayloadCheckSumB += PayloadCheckSumA;
            PayloadCheckSumB &= 0xFF;
        }

        void CheckSumUpdate(short b) {
            b &= 0xFF;
            PayloadCheckSumA += b;
            PayloadCheckSumA &= 0xFF;
            PayloadCheckSumB += PayloadCheckSumA;
            PayloadCheckSumB &= 0xFF;
        }

        void CheckSumUpdate(char b) {
            b &= 0xFF;
            PayloadCheckSumA += b;
            PayloadCheckSumA &= 0xFF;
            PayloadCheckSumB += PayloadCheckSumA;
            PayloadCheckSumB &= 0xFF;
        }

        void CheckSumUpdate(byte b[]) {
            if(b == null) return;
            for (int i = 0; i < b.length; i++) {
                CheckSumUpdate(b[i]);
            }
        }

        void CheckSumReset() {
            PayloadCheckSumA = 0;
            PayloadCheckSumB = 0;
        }
    }

    class ProtoIn extends ProtoBase {
        char Payload[];
        ProtoState State;
        short PayloadLen;
        short PayloadOffset;
        short PayloadReadPos;
        char FieldCheckSumA;
        char FieldCheckSumB;
        int Mode;
        int Type;
        char Ver;
        boolean Response;
        int ErrorNbr;
        int ErrorHeader;
        int OkNbr = 0;
        int TotalByte;
        int ID;

        ProtoIn(Serial port) {
            SetPort(port);
            ResetState();
            ResetPayload();
        }

        Boolean Process() {
            while (Port != null && Port.available() != 0) {
                if (PushByte(Port.readByte())) {
                    return true;
                }
            }
            return false;
        }

        Boolean PushByte(char b) {
            TotalByte++;
            boolean payload_complete = false;
            switch (State) {
                case StateSync1:
                    if (b == HEAD1) {
                        State = ProtoState.StateSync2;
                    }
                    break;

                case StateSync2:
                    if (b == HEAD2) {
                        State = ProtoState.StateLength1;
                        CheckSumReset();
                    } else {
                        State = ProtoState.StateSync1;
                        ErrorHeader++;
                    }
                    break;

                case StateLength1:
                    PayloadLen = (short)b;
                    if (PayloadLen > 128) {
                        ResetStateAsError();
                    } else {
                        CheckSumUpdate(b);
                        Payload = new char[PayloadLen];
                        State = ProtoState.StateMode;
                    }
                    break;

                case StateMode:
                    Mode = b;
                    Type = (char)(Mode & 0x7);
                    Ver = (char)((Mode >> 3) & 0x3);
                    int resp = (Mode >> 7) & 0x01;
                    if(resp == 1) {
                        Response = true;
                    } else {
                        Response = false;
                    }
                    CheckSumUpdate(b);
                    State = ProtoState.StateID;
                    break;

                case StateID:
                    ID = b;
                    CheckSumUpdate(b);
                    if (PayloadLen > 0) {
                        State = ProtoState.StatePayload;
                    } else {
                        State = ProtoState.StateCRCA;
                    }

                    ResetPayload();
                    break;

                case StatePayload:
                    if (PayloadOffset < 128) {
                        Payload[PayloadOffset] = b;
                        CheckSumUpdate(b);
                        PayloadOffset++;
                        if (PayloadOffset >= PayloadLen) {
                            State = ProtoState.StateCRCA;
                        }
                    } else {
                        ResetStateAsError();
                    }
                    break;

                case StateCRCA:
                    State = ProtoState.StateCRCB;
                    FieldCheckSumA = b;
                    break;

                case StateCRCB:
                    FieldCheckSumB = b;
                    State = ProtoState.StateSync1;

                    if (CheckSummCheck()) {
                        payload_complete = true;
                        Logging();
                        OkNbr++;
                    } else {
                        ResetStateAsError();
                    }
                    break;
            }

            return payload_complete;
        }

        void Logging() {
            OutputStreamExt data_in = Log.GetStream();
            data_in.writeByte(HEAD1);
            data_in.writeByte(HEAD2);
            data_in.writeByte((char)PayloadLen);
            data_in.writeByte((char)Mode);
            data_in.writeByte((char)ID);
            data_in.writeByte(Payload);
            data_in.writeByte(PayloadCheckSumA);
            data_in.writeByte(PayloadCheckSumB);
            Log.Write(data_in);
        }

        void ResetState() {
            State = ProtoState.StateSync1;
        }

        void ResetPayload() {
            PayloadOffset = 0;
            PayloadReadPos = 0;
        }

        void ResetStateAsError() {
            ErrorNbr++;
            ResetState();
        }

        Boolean CheckSummCheck() {
            if (FieldCheckSumA == PayloadCheckSumA && FieldCheckSumB == PayloadCheckSumB) {
                return true;
            } else {
                return false;
            }
        }

        short ReadU1() {
            short u1 = (short)Payload[PayloadReadPos];
            PayloadReadPos += 1;
            u1 &=0xFF;
            return u1;
        }

        int ReadU2() {
            short b1 = ReadU1();
            short b2 = ReadU1();
            return (b1) | (b2 << 8);
        }

        short ReadS2() {
            short b1 = ReadU1();
            short b2 = (short)(ReadU1() << 8);
            return (short)(b1 | b2);
        }

        long ReadU4() {
            int b1 = ReadU1();
            int b2 = ReadU1();
            int b3 = ReadU1();
            int b4 = ReadU1();
            return (b1) | (b2 << 8) | (b3 << 16) | (b4 << 24);
        }

        int ReadS4() {
            int b1 = ReadU1();
            int b2 = ReadU1();
            int b3 = ReadU1();
            int b4 = ReadU1();
            return (b1) | (b2 << 8) | (b3 << 16) | (b4 << 24);
        }

        short GetPayloadLen() {
            return PayloadLen;
        }

        short GetPayloadReadPos() {
            return PayloadReadPos;
        }

        short GetReadAvailable() {
            return (short)(GetPayloadLen() - GetPayloadReadPos());
        }
    }

    class ProtoOut extends ProtoBase {
        int DataFilled = 0;
        OutputStreamExt LogStream;
        boolean ToLog, ToOutput;

        ProtoOut(Serial port) {
            SetPort(port);
        }

        void WriteS1(byte val) {
            if (ToLog && LogStream != null) LogStream.writeByte(val);
            if (ToOutput && Port != null) Port.writeByte(val);
            CheckSumUpdate(val);
            DataFilled += 1;
        }

        void WriteU1(short val) {
            WriteS1((byte)(val & 0xFF));
        }

        void WriteU2(int val) {
            WriteU1((byte)(val & 0xFF));
            WriteU1((byte)((val & 0xFF00) >> 8));
        }

        void WriteS2(short val) {
            WriteU1((byte)(val & 0xFF));
            WriteU1((byte)((val & 0xFF00) >> 8));
        }

        void WriteU4(long val) {
            WriteU1((byte)(val & 0xFF));
            WriteU1((byte)((val & 0xFF00) >> 8));
            WriteU1((byte)((val & 0xFF0000) >> 16));
            WriteU1((byte)((val & 0xFF000000) >> 24));
        }

        void WriteS4(int val) {
            WriteU1((byte)(val & 0xFF));
            WriteU1((byte)((val & 0xFF00) >> 8));
            WriteU1((byte)((val & 0xFF0000) >> 16));
            WriteU1((byte)((val & 0xFF000000) >> 24));
        }

        void writeFloat(float data) {
            int intBits =  Float.floatToIntBits(data);
            WriteU1((byte)((intBits) & 0xFF));
            WriteU1((byte)((intBits >> 8) & 0xFF));
            WriteU1((byte)((intBits >> 16) & 0xFF));
            WriteU1((byte)((intBits >> 24) & 0xFF));
        }

        void writeDouble(double data) {
            long intBits =  Double.doubleToLongBits(data);
            WriteU1((byte)((intBits) & 0xFF));
            WriteU1((byte)((intBits >> 8) & 0xFF));
            WriteU1((byte)((intBits >> 16) & 0xFF));
            WriteU1((byte)((intBits >> 24) & 0xFF));
            WriteU1((byte)((intBits >> 32) & 0xFF));
            WriteU1((byte)((intBits >> 40) & 0xFF));
            WriteU1((byte)((intBits >> 48) & 0xFF));
            WriteU1((byte)((intBits >> 56) & 0xFF));
        }

        private void Start() {
            WriteU1((short)HEAD1);
            WriteU1((short)HEAD2);
            DataFilled = 0;
            CheckSumReset();
        }

        private void WriteCheckSum() {
            char check1 = PayloadCheckSumA;
            char check2 = PayloadCheckSumB;
            WriteU1((short)check1);
            WriteU1((short)check2);
        }

        void New(ProtoInstance inst) {
            New(inst, true, true);
        }

        void New(ProtoInstance inst, boolean output, boolean logging) {
            ToOutput = output;
            ToLog = logging;
            if(Log != null) {
                LogStream = Log.GetStream();
            }
            Start();
            WriteU1((short)inst.Length);
            WriteU1((short)inst.GetMode());
            WriteU1((short)inst.ID);
        }

        boolean End() {
            WriteCheckSum();
            if(Log != null) {
                Log.Write(LogStream);
            }
            return true;
        }
    }
}
