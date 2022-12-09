package ccsds.sle.api.isle.it;

public enum SLE_ForwardDuStatus
{
    sleFDS_radiated(0),
    sleFDS_expired(1),
    sleFDS_interrupted(2),
    sleFDS_acknowledged(3),
    sleFDS_productionStarted(4),
    sleFDS_productionNotStarted(5),
    sleFDS_unsupportedTransmissionMode(6),
    sleFDS_invalid(-1);

    private int code;


    /**
     * Constructor SLE_ForwardDuStatus
     *
     * @param code
     */
    private SLE_ForwardDuStatus(int code)
    {
        this.code = code;
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
}
