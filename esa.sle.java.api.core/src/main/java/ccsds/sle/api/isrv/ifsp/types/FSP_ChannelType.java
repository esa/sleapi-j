package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_ChannelType
{
    fspCT_MasterChannel(0, "master channel"), fspCT_VirtualChannel(1, "virtual channel"), fspCT_invalid(-1,
                                                                                                           "invalid");

    private int code;

    private String msg;


    private FSP_ChannelType(int code, String msg)
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

    public static FSP_ChannelType getChannelTypeByCode(int code)
    {
        for (FSP_ChannelType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
