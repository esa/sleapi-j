package ccsds.sle.api.isle.it;

public enum SLE_AbortOriginator
{
    /**
     * the peer system
     */
    sleAO_peer(0, "peer system"),
    /**
     * the local proxy
     */
    sleAO_proxy(1, "local proxy"),
    /**
     * the local service element
     */
    sleAO_serviceElement(2, "local service element"),
    /**
     * the local application
     */
    sleAO_application(3, "local application"),
    /**
     * 
     */
    sleAO_invalid(-1, "invalid");

    /**
     * 
     */
    private int code;

    /**
     * 
     */
    private String msg;


    /**
     * Constructor SLE_AbortOriginator.
     * 
     * @param code
     * @param msg
     */
    private SLE_AbortOriginator(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets the code
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
}
