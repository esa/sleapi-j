package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_ChannelType
{
    cltuCT_MasterChannel(0, "master channel"), cltuCT_VirtualChannel(1, "virtual channel"), cltuCT_invalid(-1,
                                                                                                           "invalid");

    private int code;

    private String msg;


    private CLTU_ChannelType(int code, String msg)
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

    public static CLTU_ChannelType getChannelTypeByCode(int code)
    {
        for (CLTU_ChannelType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
