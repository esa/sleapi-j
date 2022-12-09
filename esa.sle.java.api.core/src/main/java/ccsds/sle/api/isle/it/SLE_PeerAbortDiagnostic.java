package ccsds.sle.api.isle.it;

public enum SLE_PeerAbortDiagnostic
{

    slePAD_accessDenied(0, "access denied"),
    slePAD_unexpectedResponderId(1, "unexpected responder id"),
    slePAD_operationalRequirement(2, "operational requirement"),
    slePAD_protocolError(3, "protocol error"),
    slePAD_communicationsFailure(4, "communications failure"),
    slePAD_encodingError(5, "encoding-decoding error"),
    slePAD_returnTimeout(6, "return timeout"),
    slePAD_endOfServiceProvisionPeriod(7, "end of service provision period"),
    slePAD_unsolicitedInvokeId(8, "unsolicited invocation id"),
    slePAD_otherReason(127, "other reason"),
    slePAD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor SLE_PeerAbortDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private SLE_PeerAbortDiagnostic(int code, String msg)
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
     * Gets peer abort diagnostic by code.
     * 
     * @param code
     * @return null if there is no peer abort diagnostic for the given code.
     */
    public static SLE_PeerAbortDiagnostic getDiagByCode(int code)
    {
        for (SLE_PeerAbortDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
