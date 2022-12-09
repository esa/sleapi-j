package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_FopState
{
    fspFS_active(0, "active"),
    fspFS_retransmitWithoutWait(1, "retransmit without wait"),
    fspFS_retransmitWithWait(2, "retransmit with wait"),
    fspFS_initialisingWithoutBCFrame(3, "initialising without BC frame"),
    fspFS_initialisingWithBCFrame(4, "initialising with BC frame"),
    fspFS_initial(5, "initial"),
    fspFS_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_FopState.
     * 
     * @param code
     * @param msg
     */
    private FSP_FopState(int code, String msg)
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
     * Gets the FSP FopState by code.
     * 
     * @param code
     * @return null if there is no FSP FopState at the given code.
     */
    public static FSP_FopState getFSPFopStateByCode(int code)
    {
        for (FSP_FopState e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
