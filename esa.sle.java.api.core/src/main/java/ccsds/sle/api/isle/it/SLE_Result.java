package ccsds.sle.api.isle.it;

public enum SLE_Result
{

    sleRES_positive(0, "positive"), sleRES_negative(1, "negative"), sleRES_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_Result.
     * 
     * @param code
     * @param msg
     */
    private SLE_Result(int code, String msg)
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
     * Gets the SLE result by code.
     * 
     * @param code
     * @return null if there is no SLE result on the given code.
     */
    public static SLE_Result getSLE_ResultByCode(int code)
    {
        for (SLE_Result e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
