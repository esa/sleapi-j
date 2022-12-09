package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_TransmissionMode
{

    /**
     * AD mode
     */
    fspTM_sequenceControlled(0, "sequence controlled"),
    /**
     * BD mode
     */
    fspTM_expedited(1, "expedited"),
    /**
     * unblock AD
     */
    fspTM_sequenceControlledUnblock(2, "sequence controlled unblock"), fspTM_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_TransmissionMode.
     * 
     * @param code
     * @param msg
     */
    private FSP_TransmissionMode(int code, String msg)
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
     * Gets the FSP transmission mode by code.
     * 
     * @param code
     * @return null if there is no FSP transmission mode at the given code.
     */
    public static FSP_TransmissionMode getTransmissionModeByCode(int code)
    {
        for (FSP_TransmissionMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
