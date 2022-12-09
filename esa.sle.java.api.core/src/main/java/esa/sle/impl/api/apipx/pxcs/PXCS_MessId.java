package esa.sle.impl.api.apipx.pxcs;

public enum PXCS_MessId
{
    mid_Connect(0),
    mid_Disconnect(1),
    mid_SlePdu(2),
    mid_BindPdu(3),
    mid_PeerAbort(4),
    mid_ProtocolAbort(5),
    mid_Reset(6),
    mid_ResumeReceive(7),
    mid_SuspendReceive(8),
    mid_ResumeXmit(9),
    mid_SuspendXmit(10),
    mid_ReportTrace(11),
    mid_RegisterPort(12),
    mid_Rsp_RegisterPort(13),
    mid_DeregisterPort(14),
    mid_Rsp_DeregisterPort(15),
    mid_StartTrace(16),
    mid_Rsp_StartTrace(17),
    mid_StopTrace(18),
    mid_Rsp_StopTrace(19),
    mid_TraceRecord(20),
    mid_LogRecord(21),
    mid_Notify(22),
    mid_NormalStop(23),
    mid_Rsp_NormalStop(24);

    private int code;


    private PXCS_MessId(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }

    public static PXCS_MessId getPXCSMessIdByCode(int code)
    {
        for (PXCS_MessId e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
