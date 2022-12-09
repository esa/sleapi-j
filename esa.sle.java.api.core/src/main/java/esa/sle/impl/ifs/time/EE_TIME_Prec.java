package esa.sle.impl.ifs.time;

public enum EE_TIME_Prec
{

    eeTIME_PrecSECONDS(0, "seconds"),
    eeTIME_PrecHUNDRMILLISEC(1, "hundr millisec"),
    eeTIME_PrecTENMILLISEC(2, "ten millisec"),
    eeTIME_PrecMILLISEC(3, "millisec"),
    eeTIME_PrecHUNDRMICROSEC(4, "hund microsec"),
    eeTIME_PrecTENMICROSEC(5, "ten microsec"),
    eeTIME_PrecMICROSEC(6, "microsec"),
    eeTIME_PrecHUNDRNANOSEC(7, "hundr nanosec"),
    eeTIME_PrecTENNANOSEC(8, "ten nanosec"),
    eeTIME_PrecNANOSEC(9, "nanosec"),
    eeTIME_PrecNUMTIMEPREC(10, "num time prec");

    private int code;

    private String msg;


    private EE_TIME_Prec(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    public int getCode()
    {
        return this.code;
    }

    @Override
    public String toString()
    {
        return this.msg;
    }

}
