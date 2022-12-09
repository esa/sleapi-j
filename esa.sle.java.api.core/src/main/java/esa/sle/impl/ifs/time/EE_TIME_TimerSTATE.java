package esa.sle.impl.ifs.time;

public enum EE_TIME_TimerSTATE
{

    eeTIME_TimerCREATED(0),
    eeTIME_TimerIDLE(1),
    eeTIME_TimerSTARTING(2),
    eeTIME_TimerWAITING(3),
    eeTIME_TimerEXITING(4);

    private int code;


    private EE_TIME_TimerSTATE(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }
}
