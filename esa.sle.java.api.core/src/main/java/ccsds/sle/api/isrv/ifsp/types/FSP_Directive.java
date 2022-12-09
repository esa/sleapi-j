package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_Directive
{
    fspDV_initiateADwithoutCLCW(0, "Initiate AD without CLCW"),
    fspDV_initiateADwithCLCW(1, "Initiate AD with CLCW"),
    fspDV_initiateADwithUnlock(2, "Initiate AD with unlock"),
    fspDV_initiateADwithSetVR(3, "Initiate AD with set V(R)"),
    fspDV_terminateAD(4, "Terminate AD"),
    fspDV_resumeAD(5, "Resume AD"),
    fspDV_setVS(6, "Set V(S)"),
    fspDV_setFopSlidingWindow(7, "Set FOP sliding window width"),
    fspDV_setT1Initial(8, "Set T1 initial"),
    fspDV_setTransmissionLimit(9, "Set transmission limit"),
    fspDV_setTimeoutType(10, "Set timeout type"),
    fspDV_abortVC(11, "Abort VC"),
    fspDV_modifyMapMuxControl(12, "Modify MAP multiplexing control"),
    fspDV_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_Directive.
     * 
     * @param code
     * @param msg
     */
    private FSP_Directive(int code, String msg)
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
     * Gets FSP directive by code.
     * 
     * @param code
     * @return null if there is no FSP directive at the given code.
     */
    public static FSP_Directive getDirectiveByCode(int code)
    {
        for (FSP_Directive e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
