package ccsds.sle.api.isle.it;

public enum SLE_ApplicationIdentifier
{
    sleAI_rtnAllFrames(0, "RAF"),
    sleAI_rtnInsert(1, "RI"),
    sleAI_rtnChFrames(2, "RCF"),
    sleAI_rtnChFsh(3, "RCFSH"),
    sleAI_rtnChOcf(4, "RCOCF"),
    sleAI_rtnBitstr(5, "RBS"),
    sleAI_rtnSpacePkt(6, "RSP"),
    sleAI_fwdAosSpacePkt(7, "FASP"),
    sleAI_fwdAosVca(8, "FAVCA"),
    sleAI_fwdBitstr(9, "FBS"),
    sleAI_fwdProtoVcdu(10, "FPVCDU"),
    sleAI_fwdInsert(11, "FI"),
    sleAI_fwdVcdu(12, "FTCVCU"),
    sleAI_fwdTcSpacePkt(13, "FSP"),
    sleAI_fwdTcVca(14, "FTCVCA"),
    sleAI_fwdTcFrame(15, "FTCF"),
    sleAI_fwdCltu(16, "FCLTU"),
    sleAI_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_ApplicationIdentifier.
     * 
     * @param code
     * @param msg
     */
    private SLE_ApplicationIdentifier(int code, String msg)
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

    /**
     * Gets application identifier by code.
     * 
     * @param code
     * @return
     */
    public static SLE_ApplicationIdentifier getApplIdByCode(int code)
    {
        for (SLE_ApplicationIdentifier e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

    @Override
    public String toString()
    {
        return this.msg;
    }
}
