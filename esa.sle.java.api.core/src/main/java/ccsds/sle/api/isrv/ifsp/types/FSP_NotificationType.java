package ccsds.sle.api.isrv.ifsp.types;

public enum FSP_NotificationType
{

    fspNT_packetProcessingStarted(0, "packet processing started"),
    fspNT_packetRadiated(1, "packet radiated"),
    fspNT_packetAcknowledged(2, "packet acknowledged"),
    fspNT_slduExpired(3, "SLDU expired"),
    fspNT_packetTransmissionModeMismatch(4, "packet transmission mode mismatch"),
    fspNT_transmissionModeCapabilityChange(5, "transmission mode capability change"),
    fspNT_bufferEmpty(6, "buffer empty"),
    fspNT_noInvokeDirectiveCapabilityOnThisVc(7, "no invoke directive capability on this VC"),
    fspNT_positiveConfirmResponseToDirective(8, "positive confirm response to directive"),
    fspNT_negativeConfirmResponseToDirective(9, "negative confirm response to directive"),
    fspNT_vcAborted(10, "VC aborted"),
    fspNT_productionInterrupted(11, "production interrupted"),
    fspNT_productionHalted(12, "production halted"),
    fspNT_productionOperational(13, "production operational"),
    fspNT_actionListCompleted(14, "action list completed"),
    fspNT_actionListNotCompleted(15, "action list not completed"),
    fspNT_eventConditionEvFalse(16, "event condition evaluated to false"),
    fspNT_invokeDirectiveCapabilityOnThisVC(17, "invoke directive capability on this VC"),
    fspNT_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_NotificationType.
     * 
     * @param code
     * @param msg
     */
    private FSP_NotificationType(int code, String msg)
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
     * Gets the FSP notification type by code.
     * 
     * @param code
     * @return null if there is no FSP notification type at the given code.
     */
    public static FSP_NotificationType getNotificationTypeByCode(int code)
    {
        for (FSP_NotificationType e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
