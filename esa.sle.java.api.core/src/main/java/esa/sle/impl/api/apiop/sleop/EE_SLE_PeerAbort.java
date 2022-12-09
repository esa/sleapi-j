/**
 * @(#) EE_SLE_PeerAbort.java
 */

package esa.sle.impl.api.apiop.sleop;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_PeerAbort;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;

/**
 * ////////////////////////////////////////////////// The class implements the
 * PEER-ABORT operation. //////////////////////////////////////////////////
 */
public class EE_SLE_PeerAbort extends IEE_SLE_Operation implements ISLE_PeerAbort
{
    /**
     * The originator of the PEER-ABORT operation.
     */
    private SLE_AbortOriginator originator;

    /**
     * The PEER-ABORT diagnostic.
     */
    private SLE_PeerAbortDiagnostic diagnostic;


    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     * 
     * @param srvType
     * @param version
     * @param preporter
     */
    public EE_SLE_PeerAbort(SLE_ApplicationIdentifier srvType, int version, ISLE_Reporter preporter)
    {
        super(srvType, SLE_OpType.sleOT_peerAbort, version, false, preporter);
        this.originator = SLE_AbortOriginator.sleAO_invalid;
        this.diagnostic = SLE_PeerAbortDiagnostic.slePAD_invalid;
    }

    /**
     * Copy constructor
     * 
     * @param right
     */
    protected EE_SLE_PeerAbort(EE_SLE_PeerAbort right)
    {
        super(right);
        this.originator = SLE_AbortOriginator.sleAO_invalid;
        this.diagnostic = SLE_PeerAbortDiagnostic.slePAD_invalid;
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
        else if (iid == ISLE_PeerAbort.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
	 * 
	 */
    @Override
    public synchronized SLE_PeerAbortDiagnostic getPeerAbortDiagnostic()
    {
        return this.diagnostic;
    }

    /**
	 */
    @Override
    public synchronized void setPeerAbortDiagnostic(SLE_PeerAbortDiagnostic diagnostic)
    {
        this.diagnostic = diagnostic;
    }

    /**
	 */
    @Override
    public synchronized SLE_AbortOriginator getAbortOriginator()
    {
        return this.originator;
    }

    /**
	 */
    @Override
    public synchronized void setAbortOriginator(SLE_AbortOriginator originator)
    {
        this.originator = originator;
    }

    /**
	 */
    @Override
    public synchronized ISLE_Operation copy()
    {
        return new EE_SLE_PeerAbort(this);

    }

    /**
	 * 
	 */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder();
        printOn(os, maxDumpLength);

        os.append("Abort Originator       : " + this.originator.toString() + "\n");
        os.append("Abort Diagnostic       : " + this.diagnostic.toString() + "\n");
        return os.toString();
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.diagnostic == null) ? 0 : this.diagnostic.hashCode());
        result = prime * result + ((this.originator == null) ? 0 : this.originator.hashCode());
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
        EE_SLE_PeerAbort other = (EE_SLE_PeerAbort) obj;
        if (this.diagnostic != other.diagnostic)
        {
            return false;
        }
        if (this.originator != other.originator)
        {
            return false;
        }
        return true;
    }

    @Override
    public synchronized String toString()
    {
        return "EE_SLE_PeerAbort [originator=" + this.originator + ", diagnostic=" + this.diagnostic + "]";
    }

}
