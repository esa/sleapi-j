package ccsds.sle.api.isle.it;

public enum SLE_TimeFmt
{

    sleTF_dayOfMonth(0), sleTF_dayOfYear(1);

    private int code;


    /**
     * Constructor SLE_TimeFmt.
     * 
     * @param code
     */
    private SLE_TimeFmt(int code)
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
