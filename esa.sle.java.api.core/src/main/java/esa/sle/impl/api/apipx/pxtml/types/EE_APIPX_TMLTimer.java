package esa.sle.impl.api.apipx.pxtml.types;

public enum EE_APIPX_TMLTimer
{
    eeAPIPXtt_HBT(0, "heartbeat transmitting timer"),
    eeAPIPXtt_HBR(1, "heartbeat receiving timer"),
    eeAPIPXtt_TMS(2, "startup timer"),
    eeAPIPXtt_CPA(3, "closing after peer abort timer");

    private int code;

    private String msg;


    private EE_APIPX_TMLTimer(int code, String msg)
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
