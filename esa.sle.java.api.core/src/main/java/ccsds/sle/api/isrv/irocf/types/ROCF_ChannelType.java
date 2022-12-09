package ccsds.sle.api.isrv.irocf.types;

public enum ROCF_ChannelType
{

    rocfCT_MasterChannel(0, "master channel"), rocfCT_VirtualChannel(1, "virtual channel"), rocfCT_invalid(-1,
                                                                                                           "invalid");

    private int code;

    private String msg;


    /**
     * Constructor ROCF_ChannelType.
     * 
     * @param code
     * @param msg
     */
    private ROCF_ChannelType(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets the code.
     * 
     * @return
     */
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
     * Gets the ROCF channel type by code.
     * 
     * @param code
     * @return null if there is no ROCF channel type at the given type.
     */
    public static ROCF_ChannelType getChannelTypeByCode(int code)
    {
        for (ROCF_ChannelType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
