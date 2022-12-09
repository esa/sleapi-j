package ccsds.sle.api.isrv.irocf.types;

import ccsds.sle.api.isle.it.SLE_DeliveryMode;

public enum ROCF_DeliveryMode
{
    rocfDM_timelyOnline(SLE_DeliveryMode.sleDM_rtnTimelyOnline.getCode(), "timely online")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnTimelyOnline;
        }
    },
    rocfDM_completeOnline(SLE_DeliveryMode.sleDM_rtnCompleteOnline.getCode(), "complete online")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnCompleteOnline;
        }
    },
    rocfDM_offline(SLE_DeliveryMode.sleDM_rtnOffline.getCode(), "offline")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnOffline;
        }
    },
    rocfDM_invalid(-1, "invalid")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_invalid;
        }
    };

    private int code;

    private String msg;


    /**
     * Constructor ROCF_DeliveryMode.
     * 
     * @param code
     * @param msg
     */
    private ROCF_DeliveryMode(int code, String msg)
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
     * Gets the ROCF delivery mode by code.
     * 
     * @param code
     * @return null if there is no ROCF delivery mode at the given code.
     */
    public static ROCF_DeliveryMode getROCFDeliveryMode(int code)
    {
        for (ROCF_DeliveryMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

    public abstract SLE_DeliveryMode asSLE_DeliveryMode();

}
