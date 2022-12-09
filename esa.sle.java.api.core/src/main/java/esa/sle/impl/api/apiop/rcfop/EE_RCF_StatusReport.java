/**
 * @(#) EE_RCF_StatusReport.java
 */

package esa.sle.impl.api.apiop.rcfop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isrv.ircf.IRCF_StatusReport;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;

/**
 * @NameRCF StatusReport Operation@EndName
 * @ResponsibilityThe class implements the RCF specific StatusReport operation.@EndResponsibility
 */
public class EE_RCF_StatusReport extends IEE_SLE_Operation implements IRCF_StatusReport
{
    /**
     * The total number of frames delivered.
     */
    private long numFrames = 0;

    /**
     * The lock status of the frame synchronisation process.
     */
    private RCF_LockStatus frameSyncLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the carrier demodulation process.
     */
    private RCF_LockStatus carrierDemodLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the sub-carrier demodulation process.
     */
    private RCF_LockStatus subCarrierDemodLock = RCF_LockStatus.rcfLS_invalid;

    /**
     * The lock status of the symbol synchronisation process.
     */
    private RCF_LockStatus symbolSyncLock = RCF_LockStatus.rcfLS_invalid;

    private RCF_ProductionStatus productionStatus = RCF_ProductionStatus.rcfPS_invalid;


    private EE_RCF_StatusReport(final EE_RCF_StatusReport right)
    {
        super(right);
        this.numFrames = right.numFrames;
        this.frameSyncLock = right.frameSyncLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.subCarrierDemodLock = right.subCarrierDemodLock;
        this.symbolSyncLock = right.symbolSyncLock;
        this.productionStatus = right.productionStatus;
    }

    public EE_RCF_StatusReport(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, SLE_OpType.sleOT_statusReport, version, false, preporter);
        this.numFrames = 0;
        this.frameSyncLock = RCF_LockStatus.rcfLS_invalid;
        this.carrierDemodLock = RCF_LockStatus.rcfLS_invalid;
        this.subCarrierDemodLock = RCF_LockStatus.rcfLS_invalid;
        this.symbolSyncLock = RCF_LockStatus.rcfLS_invalid;
        this.productionStatus = RCF_ProductionStatus.rcfPS_invalid;
    }

    @Override
    public synchronized long getNumFrames()
    {
        return this.numFrames;
    }

    @Override
    public synchronized RCF_LockStatus getFrameSyncLock()
    {
        return this.frameSyncLock;
    }

    @Override
    public synchronized RCF_LockStatus getCarrierDemodLock()
    {
        return this.carrierDemodLock;
    }

    @Override
    public synchronized RCF_LockStatus getSubCarrierDemodLock()
    {
        return this.subCarrierDemodLock;
    }

    @Override
    public synchronized RCF_LockStatus getSymbolSyncLock()
    {
        return this.symbolSyncLock;
    }

    @Override
    public synchronized RCF_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    @Override
    public synchronized void setNumFrames(long count)
    {
        this.numFrames = count;
    }

    @Override
    public synchronized void setFrameSyncLock(RCF_LockStatus status)
    {
        this.frameSyncLock = status;
    }

    @Override
    public synchronized void setCarrierDemodLock(RCF_LockStatus status)
    {
        this.carrierDemodLock = status;
    }

    @Override
    public synchronized void setSubCarrierDemodLock(RCF_LockStatus status)
    {
        this.subCarrierDemodLock = status;
    }

    @Override
    public synchronized void setSymbolSyncLock(RCF_LockStatus status)
    {
        this.symbolSyncLock = status;
    }

    @Override
    public synchronized void setProductionStatus(RCF_ProductionStatus status)
    {
        this.productionStatus = status;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();

        if (getOpVersionNumber() < 2)
        {
            if (this.frameSyncLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.subCarrierDemodLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.productionStatus == RCF_ProductionStatus.rcfPS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
        }
        else
        {
            if (this.frameSyncLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.frameSyncLock == RCF_LockStatus.rcfLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.symbolSyncLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RCF_LockStatus.rcfLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.carrierDemodLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RCF_LockStatus.rcfLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.subCarrierDemodLock == RCF_LockStatus.rcfLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.productionStatus == RCF_ProductionStatus.rcfPS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RCF_StatusReport pobj = new EE_RCF_StatusReport(this);
        return pobj;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("Number of frames       : " + this.numFrames + "\n");
        oss.append("Frame sync lock        : " + this.frameSyncLock + "\n");
        oss.append("Symbol sync lock       : " + this.symbolSyncLock + "\n");
        oss.append("Carrier demod lock     : " + this.carrierDemodLock + "\n");
        oss.append("Subcarrier demod lock  : " + this.subCarrierDemodLock + "\n");
        oss.append("Production status      : " + this.productionStatus + "\n");
        String ret = oss.toString();
        return ret;

    }

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
        else if (iid == IRCF_StatusReport.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized String toString()
    {
        return "EE_RCF_StatusReport [numFrames=" + this.numFrames + ", frameSyncLock=" + this.frameSyncLock
               + ", carrierDemodLock=" + this.carrierDemodLock + ", subCarrierDemodLock=" + this.subCarrierDemodLock
               + ", symbolSyncLock=" + this.symbolSyncLock + ", productionStatus=" + this.productionStatus + "]";
    }

}
