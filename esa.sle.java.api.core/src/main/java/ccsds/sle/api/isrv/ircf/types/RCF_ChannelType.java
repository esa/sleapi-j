package ccsds.sle.api.isrv.ircf.types;

public enum RCF_ChannelType
{
    rcfCT_MasterChannel(0, "master channel"), rcfCT_VirtualChannel(1, "virtual channel"), rcfCT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor rcf channel type.
     * 
     * @param code
     * @param msg
     */
    private RCF_ChannelType(int code, String msg)
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

    /**
     * Gets the channel type by code.
     * 
     * @param code
     * @return
     */
    public static RCF_ChannelType getChannelTypeByCode(int code)
    {
        for (RCF_ChannelType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
