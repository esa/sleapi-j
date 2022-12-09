package ccsds.sle.api.isrv.icltu.types;

import ccsds.sle.api.isle.it.SLE_ForwardDuStatus;

public enum CLTU_Status
{

    cltuST_expired(SLE_ForwardDuStatus.sleFDS_expired.getCode(), "expired"),
    cltuST_interrupted(SLE_ForwardDuStatus.sleFDS_interrupted.getCode(), "interrupted"),
    cltuST_radiationStarted(SLE_ForwardDuStatus.sleFDS_productionStarted.getCode(), "radiation started"),
    cltuST_radiated(SLE_ForwardDuStatus.sleFDS_radiated.getCode(), "radiated"),
    cltuST_radiationNotStarted(SLE_ForwardDuStatus.sleFDS_productionNotStarted.getCode(), "radiation not started"),
    cltuST_invalid(-1, "invalid");

    private int code;

    private String msg;


    /**
     * Constructor CLTU_Status.
     * 
     * @param code
     * @param msg
     */
    private CLTU_Status(int code, String msg)
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
     * Gets the CLTU status by code.
     * 
     * @param code
     * @return null if there is no CLTU status at the given code.
     */
    public static CLTU_Status getStatusByCode(int code)
    {
        for (CLTU_Status e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
