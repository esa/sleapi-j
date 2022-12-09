package esa.sle.impl.eapi.dcw.type;

public enum DCW_Event_Type
{
    dcwEVT_noEvent(0, "No event"),
    dcwEVT_informOpInvoke(1, "Inform Op Invoke"),
    dcwEVT_informOpReturn(2, "Inform Op Return"),
    dcwEVT_resumeDataTransfer(3, "Resume Data Transfer"),
    dcwEVT_provisionPeriodEnds(4, "Provision period ends"),
    dcwEVT_protocolAbort(5, "Protocol Abort");

    private int code;

    private String msg;


    private DCW_Event_Type(int code, String msg)
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
