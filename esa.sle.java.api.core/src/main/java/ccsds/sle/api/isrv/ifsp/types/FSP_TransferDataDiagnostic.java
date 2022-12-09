package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_TransferDataDiagnostic
{
    fspXFD_unableToProcess(0, "unable to process"),
    fspXFD_unableToStore(1, "unable to store"),
    fspXFD_packetIdOutOfSequence(2, "packet ID out of sequence"),
    fspXFD_duplicatePacketIdentification(3, "duplicate packet ID"),
    fspXFD_inconsistentTimeRange(4, "inconsistent time range"),
    fspXFD_invalidTime(5, "invalid time"),
    fspXFD_conflictingProductionTimeIntervals(6, "conflicting production time intervals"),
    fspXFD_lateSldu(7, "late SLDU"),
    fspXFD_invalidDelayTime(8, "invalid delay time"),
    fspXFD_invalidTransmissionMode(9, "invalid transmission mode"),
    fspXFD_invalidMap(10, "invalid map"),
    fspXFD_invalidNotificationRequest(11, "invalid notification request"),
    fspXFD_packetTooLong(12, "packet too long"),
    fspXFD_unsupportedPacketVersion(13, "unsupported packet version"),
    fspXFD_incorrectPacketType(14, "incorrect packet type"),
    fspXFD_invalidPacketApid(15, "invalid packet APID"),
    fspXFD_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_TransferDataDiagnostic.
     * 
     * @param code
     * @param msg
     */
    private FSP_TransferDataDiagnostic(int code, String msg)
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
     * Gets the FSP transfer data diagnostic by code.
     * 
     * @param code
     * @return null if there is no FSP transfer data diagnostic at the given
     *         code.
     */
    public static FSP_TransferDataDiagnostic getTransferDataDiagnosticByCode(int code)
    {
        for (FSP_TransferDataDiagnostic e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
