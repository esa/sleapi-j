package ccsds.sle.api.isrv.ifsp.types;

import ccsds.sle.api.isle.it.SLE_ForwardDuStatus;

public enum FSP_PacketStatus
{

    fspST_acknowledged(SLE_ForwardDuStatus.sleFDS_acknowledged.getCode(), "acknowledged"),
    fspST_radiated(SLE_ForwardDuStatus.sleFDS_radiated.getCode(), "radiated"),
    fspST_productionStarted(SLE_ForwardDuStatus.sleFDS_productionStarted.getCode(), "production started"),
    fspST_productionNotStarted(SLE_ForwardDuStatus.sleFDS_productionNotStarted.getCode(), "production not started"),
    fspST_expired(SLE_ForwardDuStatus.sleFDS_expired.getCode(), "expired"),
    fspST_unsupportedTransmissionMode(SLE_ForwardDuStatus.sleFDS_unsupportedTransmissionMode.getCode(),
                                      "unsupported transmission mode"),
    fspST_interrupted(SLE_ForwardDuStatus.sleFDS_interrupted.getCode(), "interrupted"),
    fspST_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor FSP_PacketStatus.
     * 
     * @param code
     * @param msg
     */
    private FSP_PacketStatus(int code, String msg)
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
     * Gets the FSP packet status by code.
     * 
     * @param code
     * @return null if there is no FSP packet status at the given code.
     */
    public static FSP_PacketStatus getPacketStatusByCode(int code)
    {
        for (FSP_PacketStatus e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
