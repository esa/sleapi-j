/**
 * @(#) EE_SLE_Unbind.java
 */

package esa.sle.impl.api.apiop.sleop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_Unbind;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_UnbindReason;

/**
 * The class implements the UNBIND operation.
 */
public class EE_SLE_Unbind extends IEE_SLE_ConfirmedOperation implements ISLE_Unbind
{
    /**
     * The UNBIND reason.
     */
    private SLE_UnbindReason unbindReason;


    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     * 
     * @param opSrvType
     * @param version
     * @param preporter
     */
    public EE_SLE_Unbind(SLE_ApplicationIdentifier opSrvType, int version, ISLE_Reporter preporter)
    {
        super(opSrvType, SLE_OpType.sleOT_unbind, version, preporter);
        this.unbindReason = SLE_UnbindReason.sleUBR_invalid;
    }

    /**
     * Copy constructor
     * 
     * @param right
     */
    protected EE_SLE_Unbind(EE_SLE_Unbind right)
    {
        super(right);
        this.unbindReason = right.unbindReason;
    }

    /**
	 * 
	 */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
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
        else if (iid == ISLE_Unbind.class)
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
    public synchronized SLE_UnbindReason getUnbindReason()
    {
        return this.unbindReason;
    }

    /**
	 */
    @Override
    public synchronized void setUnbindReason(SLE_UnbindReason reason)
    {
        this.unbindReason = reason;
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.unbindReason == SLE_UnbindReason.sleUBR_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG, "Invalid Unbind reason");
        }
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation copy()
    {
        return new EE_SLE_Unbind(this);
    }

    /**
	 * 
	 */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder(maxDumpLength);
        printOn(os, maxDumpLength);

        os.append("Unbind Reason          : " + this.unbindReason.toString() + "\n");
        return os.toString();
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.unbindReason == null) ? 0 : this.unbindReason.hashCode());
        return result;
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_SLE_Unbind other = (EE_SLE_Unbind) obj;
        if (this.unbindReason != other.unbindReason)
        {
            return false;
        }
        return true;
    }

    @Override
    public synchronized String toString()
    {
        return "EE_SLE_Unbind [reason=" + this.unbindReason + "]";
    }
}
