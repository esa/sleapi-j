package ccsds.sle.api.isle.it;

public enum SLE_YesNo
{
    sleYN_No(0, "no"), sleYN_Yes(1, "yes"), sleYN_invalid(-1, "invalid YN field");

    private int code;

    private String msg;


    /**
     * Constructor SLE_YesNo.
     * 
     * @param code
     * @param msg
     */
    private SLE_YesNo(int code, String msg)
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return this.msg;
    }

    /**
     * Gets the SLE_YesNo by boolean value.
     * 
     * @param readYn
     * @return
     */
    public static SLE_YesNo getYesNoByBool(boolean readYn)
    {
        int code = (readYn) ? 1 : 0;
        for (SLE_YesNo e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets the SLE_YesNo by code value.
     * 
     * @param code
     * @return null if there is no SLE_YesNo on the given code.
     */
    public static SLE_YesNo getYesNoByCode(int code)
    {
        for (SLE_YesNo e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
