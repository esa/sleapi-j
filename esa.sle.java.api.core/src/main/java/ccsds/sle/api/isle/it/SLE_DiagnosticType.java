package ccsds.sle.api.isle.it;

public enum SLE_DiagnosticType
{
    sleDT_noDiagnostics(0, "no diagnostics"),
    sleDT_commonDiagnostics(1, "common diagnostics"),
    sleDT_specificDiagnostics(2, "specific diagnostics");

    private int code;

    private String msg;


    /**
     * Constructor SLE_DiagnosticType.
     * 
     * @param code the code as parameter
     * @param msg the message for the given code
     */
    private SLE_DiagnosticType(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets the code.
     * 
     * @return the code
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
