package ccsds.sle.api.isle.it;

import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;

public enum SLE_DeliveryMode
{

    sleDM_rtnTimelyOnline(0, "timely online")
    {
        @Override
        public RAF_DeliveryMode asRAF_DeliveryMode()
        {
            return RAF_DeliveryMode.rafDM_timelyOnline;
        }

        @Override
        public RCF_DeliveryMode asRCF_DeliveryMode()
        {
            return RCF_DeliveryMode.rcfDM_timelyOnline;
        }

        @Override
        public ROCF_DeliveryMode asROCF_DeliveryMode()
        {
            return ROCF_DeliveryMode.rocfDM_timelyOnline;
        }
    },

    sleDM_rtnCompleteOnline(1, "complete online")
    {
        @Override
        public RAF_DeliveryMode asRAF_DeliveryMode()
        {
            return RAF_DeliveryMode.rafDM_completeOnline;
        }

        @Override
        public RCF_DeliveryMode asRCF_DeliveryMode()
        {
            return RCF_DeliveryMode.rcfDM_completeOnline;
        }

        @Override
        public ROCF_DeliveryMode asROCF_DeliveryMode()
        {
            return ROCF_DeliveryMode.rocfDM_completeOnline;
        }
    },

    sleDM_rtnOffline(2, "offline")
    {
        @Override
        public RAF_DeliveryMode asRAF_DeliveryMode()
        {
            return RAF_DeliveryMode.rafDM_offline;
        }

        @Override
        public RCF_DeliveryMode asRCF_DeliveryMode()
        {
            return RCF_DeliveryMode.rcfDM_offline;
        }

        @Override
        public ROCF_DeliveryMode asROCF_DeliveryMode()
        {
            return ROCF_DeliveryMode.rocfDM_offline;
        }
    },

    sleDM_fwdOnline(3, "online")
    {

        // these here are correct null. Not used as the others
        @Override
        public RAF_DeliveryMode asRAF_DeliveryMode()
        {
            return null;
        }

        @Override
        public RCF_DeliveryMode asRCF_DeliveryMode()
        {
            return null;
        }

        @Override
        public ROCF_DeliveryMode asROCF_DeliveryMode()
        {
            return null;
        }

    },

    sleDM_fwdOffline(4, "offline")
    {

        @Override
        public RAF_DeliveryMode asRAF_DeliveryMode()
        {
            return null;
        }

        @Override
        public RCF_DeliveryMode asRCF_DeliveryMode()
        {
            return null;
        }

        @Override
        public ROCF_DeliveryMode asROCF_DeliveryMode()
        {
            return null;
        }

    },

    sleDM_invalid(-1, "invalid")
    {
        @Override
        public RAF_DeliveryMode asRAF_DeliveryMode()
        {
            return RAF_DeliveryMode.rafDM_invalid;
        }

        @Override
        public RCF_DeliveryMode asRCF_DeliveryMode()
        {
            return RCF_DeliveryMode.rcfDM_invalid;
        }

        @Override
        public ROCF_DeliveryMode asROCF_DeliveryMode()
        {
            return ROCF_DeliveryMode.rocfDM_invalid;
        }
    };

    private int code;

    private String msg;


    /**
     * Constructor SLE_DeliveryMode.
     * 
     * @param code
     * @param msg
     */
    private SLE_DeliveryMode(int code, String msg)
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

    /**
     * Used for casting between enumerations.
     * 
     * @return
     */
    public abstract RAF_DeliveryMode asRAF_DeliveryMode();

    /**
     * Used for casting between enumerations.
     * 
     * @return
     */
    public abstract RCF_DeliveryMode asRCF_DeliveryMode();

    /**
     * Used for casting between enumerations.
     * 
     * @return
     */
    public abstract ROCF_DeliveryMode asROCF_DeliveryMode();

    @Override
    public String toString()
    {
        return this.msg;
    }

    /**
     * Gets the delivery mode by code.
     * 
     * @param code
     * @return null if there is no delivery mode at the give parameter code.
     */
    public static SLE_DeliveryMode getDelModeByCode(int code)
    {
        for (SLE_DeliveryMode e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }

}
