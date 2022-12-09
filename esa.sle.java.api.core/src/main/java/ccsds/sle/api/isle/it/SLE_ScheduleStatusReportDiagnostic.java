package ccsds.sle.api.isle.it;

public enum SLE_ScheduleStatusReportDiagnostic
{
    sleSSD_notSupportedInThisDeliveryMode(0, "not supported in this delivery mode"),
    sleSSD_alreadyStopped(1, "already stopped"),
    sleSSD_invalidReportingCycle(2, "invalid reporting cycle"),
    sleSSD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_ScheduleStatusReportDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private SLE_ScheduleStatusReportDiagnostic(int code, String msg)
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
     * Gets the schedule status report diagnostic by code.
     * 
     * @param code
     * @return null if there is no schedule status report diagnostic at the
     *         given code.
     */
    public static SLE_ScheduleStatusReportDiagnostic getSSRDiagnosticsByCode(int code)
    {
        for (SLE_ScheduleStatusReportDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
