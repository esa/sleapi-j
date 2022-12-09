package ccsds.sle.api.isrv.iraf.types;

import ccsds.sle.api.isle.it.SLE_DeliveryMode;

public enum RAF_DeliveryMode
{

    rafDM_timelyOnline(SLE_DeliveryMode.sleDM_rtnTimelyOnline.getCode(), "timely online")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnTimelyOnline;
        }
    },
    rafDM_completeOnline(SLE_DeliveryMode.sleDM_rtnCompleteOnline.getCode(), "complete online")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnCompleteOnline;
        }
    },
    rafDM_offline(SLE_DeliveryMode.sleDM_rtnOffline.getCode(), "offline")
    {
        @Override
        public SLE_DeliveryMode asSLE_DeliveryMode()
        {
            return SLE_DeliveryMode.sleDM_rtnOffline;
        }
    },
    rafDM_invalid(-1, "invalid")
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
     * Constructor RAF_DeliveryMode.
     * 
     * @param code
     * @param msg
     */
    private RAF_DeliveryMode(int code, String msg)
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
     * This method is used as a casting between enumerations. It is override on
     * every enumeration element.
     * 
     * @return
     */
    public abstract SLE_DeliveryMode asSLE_DeliveryMode();

    /**
     * Gets the RAF delivery mode by code.
     * 
     * @param code
     * @return null if there is no RAF delivery mode at the given code.
     */
    public static RAF_DeliveryMode getRAFDelModeByCode(int code)
    {
        for (RAF_DeliveryMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }
        return null;
    }
}
