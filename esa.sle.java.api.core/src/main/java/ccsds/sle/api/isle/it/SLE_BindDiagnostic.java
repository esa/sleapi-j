package ccsds.sle.api.isle.it;

public enum SLE_BindDiagnostic
{
    sleBD_accessDenied(0, "access denied"),
    sleBD_serviceTypeNotSupported(1, "service type not supported"),
    sleBD_versionNotSupported(2, "version not supported"),
    sleBD_noSuchServiceInstance(3, "no such service instance"),
    sleBD_alreadyBound(4, "already bound"),
    sleBD_siNotAccessibleToThisInitiator(5, "service instance not accessible to this initiator"),
    sleBD_inconsistentServiceType(6, "inconsistent service type"),
    sleBD_invalidTime(7, "invalid time"),
    sleBD_outOfService(8, "out of service"),
    sleBD_otherReason(127, "other reason"),
    sleBD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_BindDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private SLE_BindDiagnostic(int code, String msg)
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
     * Gets bind diagnostic by code.
     * 
     * @param code
     * @return null if there is no bind diagnostic with the given code.
     */
    public static SLE_BindDiagnostic getBindDiagnosticByCode(int code)
    {
        for (SLE_BindDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
