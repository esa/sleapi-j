/**
 * @(#) EE_SLE_Stop.java
 */

package esa.sle.impl.api.apiop.sleop;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_Stop;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;

/**
 * The class implements the STOP operation.
 */
public class EE_SLE_Stop extends IEE_SLE_ConfirmedOperation implements ISLE_Stop
{
    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     * 
     * @param opSrvType
     * @param version
     * @param preporter
     */
    public EE_SLE_Stop(SLE_ApplicationIdentifier opSrvType, int version, ISLE_Reporter preporter)
    {
        super(opSrvType, SLE_OpType.sleOT_stop, version, preporter);
    }

    /**
     * Copy constructor
     * 
     * @param right
     */
    protected EE_SLE_Stop(EE_SLE_Stop right)
    {
        super(right);
    }

    /**
	 * 
	 */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Stop.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
	 */
    @Override
    public synchronized ISLE_Operation copy()
    {
        return new EE_SLE_Stop(this);
    }

    /**
	 * 
	 */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder(maxDumpLength);
        printOn(os, maxDumpLength);
        return os.toString();
    }

}
