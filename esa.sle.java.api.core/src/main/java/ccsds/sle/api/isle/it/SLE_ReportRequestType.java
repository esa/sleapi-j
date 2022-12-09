package ccsds.sle.api.isle.it;

public enum SLE_ReportRequestType
{

    sleRRT_immediately(0, "immediately"),
    sleRRT_periodically(1, "periodically"),
    sleRRT_stop(2, "stop"),
    sleRRT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_ReportRequestType.
     * 
     * @param code
     * @param msg
     */
    private SLE_ReportRequestType(int code, String msg)
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
     * Gets the report request type by code.
     * 
     * @param code
     * @return null if there is no report request type for the given code
     */
    public static SLE_ReportRequestType getReportRequestTypeByCode(int code)
    {
        for (SLE_ReportRequestType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
