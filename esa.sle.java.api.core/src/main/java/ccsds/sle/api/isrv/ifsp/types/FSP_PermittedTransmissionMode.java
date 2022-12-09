package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_PermittedTransmissionMode
{

    fspPTM_sequenceControlled(0, "sequence controlled"), // AD
    fspPTM_expedited(1, "expedited"), // BD
    fspPTM_any(2, "any"),
    fspPTM_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_PermittedTransmissionMode.
     * 
     * @param code
     * @param msg
     */
    private FSP_PermittedTransmissionMode(int code, String msg)
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
     * Gets the FSP permitted transmission mode by code.
     * 
     * @param code
     * @return null if there is no FSP permitted transmission mode at the given
     *         code.
     */
    public static FSP_PermittedTransmissionMode getFSPPermittedTransmissionModeByCode(int code)
    {
        for (FSP_PermittedTransmissionMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
