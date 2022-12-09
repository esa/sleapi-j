package ccsds.sle.api.isrv.ircf.types;

import ccsds.sle.api.isle.it.SLE_DeliveryMode;

public enum RCF_DeliveryMode
{
    rcfDM_timelyOnline(SLE_DeliveryMode.sleDM_rtnTimelyOnline.getCode(), "timely online")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnTimelyOnline;
        }
    },
    rcfDM_completeOnline(SLE_DeliveryMode.sleDM_rtnCompleteOnline.getCode(), "complete online")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnCompleteOnline;
        }
    },
    rcfDM_offline(SLE_DeliveryMode.sleDM_rtnOffline.getCode(), "offline")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnOffline;
        }
    },
    rcfDM_invalid(-1, "invalid")
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
     * Constructor RCF_DeliveryMode.
     * 
     * @param code
     * @param msg
     */
    private RCF_DeliveryMode(int code, String msg)
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

    public abstract SLE_DeliveryMode asSLE_DeliveryMode();

    /**
     * Gets the RCF delivery mode by code.
     * 
     * @param code
     * @return null if there is no RCF delivery mode at the given code.
     */
    public static RCF_DeliveryMode getRCFDelModeByCode(int code)
    {
        for (RCF_DeliveryMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
