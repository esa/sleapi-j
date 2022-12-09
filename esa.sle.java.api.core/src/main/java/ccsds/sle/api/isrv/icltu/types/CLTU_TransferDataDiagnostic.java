package ccsds.sle.api.isrv.icltu.types;

public enum CLTU_TransferDataDiagnostic
{
    cltuXFD_unableToProcess(0, "unable to process"),
    cltuXFD_unableToStore(1, "unable to store"),
    cltuXFD_outOfSequence(2, "out of sequence"),
    cltuXFD_inconsistenceTimeRange(3, "inconsistent time range"),
    cltuXFD_invalidTime(4, "invalid time"),
    cltuXFD_lateSldu(5, "late Sldu"),
    cltuXFD_invalidDelayTime(6, "invalid delay time"),
    cltuXFD_cltuError(7, "cltu error"),
    cltuXFD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_TransferDataDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private CLTU_TransferDataDiagnostic(int code, String msg)
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
     * Gets CLTU transfer data diagnostic by code.
     * 
     * @param code
     * @return null if there is no CLTU transfer data diagnostic at the given
     *         code.
     */
    public static CLTU_TransferDataDiagnostic getTransferDataDiagnosticByCode(int code)
    {
        for (CLTU_TransferDataDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
