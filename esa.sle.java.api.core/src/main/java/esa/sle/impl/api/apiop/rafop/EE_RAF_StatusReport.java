/**
 * @(#) EE_RAF_StatusReport.java
 */

package esa.sle.impl.api.apiop.rafop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isrv.iraf.IRAF_StatusReport;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;

/**
 * @ResponsibilityThe class implements the RAF specific StatusReport operation.@EndResponsibility
 */
public class EE_RAF_StatusReport extends IEE_SLE_Operation implements IRAF_StatusReport
{
    /**
     * The number of error free frames delivered.
     */
    private long numErrorFreeFrames = 0;

    /**
     * The total number of frames delivered.
     */
    private long numFrames = 0;

    /**
     * The frame synchroniser lock status.
     */
    private RAF_LockStatus frameSyncLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The carrier demodulator lock status.
     */
    private RAF_LockStatus carrierDemodLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The sub-carrier demodulator lock status.
     */
    private RAF_LockStatus subCarrierDemodLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The symbol synchroniser lock status.
     */
    private RAF_LockStatus symbolSyncLock = RAF_LockStatus.rafLS_invalid;

    /**
     * The production status.
     */
    private RAF_ProductionStatus productionStatus = RAF_ProductionStatus.rafPS_invalid;


    private EE_RAF_StatusReport(final EE_RAF_StatusReport right)
    {
        super(right);
        this.numErrorFreeFrames = right.numErrorFreeFrames;
        this.numFrames = right.numFrames;
        this.frameSyncLock = right.frameSyncLock;
        this.carrierDemodLock = right.carrierDemodLock;
        this.subCarrierDemodLock = right.subCarrierDemodLock;
        this.symbolSyncLock = right.symbolSyncLock;
        this.productionStatus = right.productionStatus;
    }

    public EE_RAF_StatusReport(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, SLE_OpType.sleOT_statusReport, version, false, preporter);
        this.numErrorFreeFrames = 0;
        this.numFrames = 0;
        this.frameSyncLock = RAF_LockStatus.rafLS_invalid;
        this.carrierDemodLock = RAF_LockStatus.rafLS_invalid;
        this.subCarrierDemodLock = RAF_LockStatus.rafLS_invalid;
        this.symbolSyncLock = RAF_LockStatus.rafLS_invalid;
        this.productionStatus = RAF_ProductionStatus.rafPS_invalid;
    }

    @Override
    public synchronized long getNumErrorFreeFrames()
    {
        return this.numErrorFreeFrames;
    }

    @Override
    public synchronized long getNumFrames()
    {
        return this.numFrames;
    }

    @Override
    public synchronized RAF_LockStatus getFrameSyncLock()
    {
        return this.frameSyncLock;
    }

    @Override
    public synchronized RAF_LockStatus getCarrierDemodLock()
    {
        return this.carrierDemodLock;
    }

    @Override
    public synchronized RAF_LockStatus getSubCarrierDemodLock()
    {
        return this.subCarrierDemodLock;
    }

    @Override
    public synchronized RAF_LockStatus getSymbolSyncLock()
    {
        return this.symbolSyncLock;
    }

    @Override
    public synchronized RAF_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    @Override
    public synchronized void setNumErrorFreeFrames(long count)
    {
        this.numErrorFreeFrames = count;
    }

    @Override
    public synchronized void setNumFrames(long count)
    {
        this.numFrames = count;
    }

    @Override
    public synchronized void setFrameSyncLock(RAF_LockStatus status)
    {
        this.frameSyncLock = status;
    }

    @Override
    public synchronized void setCarrierDemodLock(RAF_LockStatus status)
    {
        this.carrierDemodLock = status;
    }

    @Override
    public synchronized void setSubCarrierDemodLock(RAF_LockStatus status)
    {
        this.subCarrierDemodLock = status;
    }

    @Override
    public synchronized void setSymbolSyncLock(RAF_LockStatus status)
    {
        this.symbolSyncLock = status;
    }

    @Override
    public synchronized void setProductionStatus(RAF_ProductionStatus status)
    {
        this.productionStatus = status;
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RAF_StatusReport pobj = new EE_RAF_StatusReport(this);
        return pobj;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("Num  error free frames : " + this.numErrorFreeFrames + "\n");
        oss.append("Number of frames       : " + this.numFrames + "\n");
        oss.append("Frame sync lock        : " + this.frameSyncLock + "\n");
        oss.append("Symbol sync lock       : " + this.symbolSyncLock + "\n");
        oss.append("Carrier demod lock     : " + this.carrierDemodLock + "\n");
        oss.append("Subcarrier demod lock  : " + this.subCarrierDemodLock + "\n");
        oss.append("Production status      : " + this.productionStatus + "\n");

        String ret = oss.toString();
        return ret;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {

        super.verifyInvocationArguments();

        if (getOpVersionNumber() < 2)
        {
            if (this.frameSyncLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.subCarrierDemodLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.productionStatus == RAF_ProductionStatus.rafPS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
        }
        else
        {
            if (this.frameSyncLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.frameSyncLock == RAF_LockStatus.rafLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.symbolSyncLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.symbolSyncLock == RAF_LockStatus.rafLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.carrierDemodLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.carrierDemodLock == RAF_LockStatus.rafLS_notInUse)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.subCarrierDemodLock == RAF_LockStatus.rafLS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
            if (this.productionStatus == RAF_ProductionStatus.rafPS_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == IRAF_StatusReport.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
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
        return "EE_RAF_StatusReport [numErrorFreeFrames=" + this.numErrorFreeFrames + ", numFrames=" + this.numFrames
               + ", frameSyncLock=" + this.frameSyncLock + ", carrierDemodLock=" + this.carrierDemodLock
               + ", subCarrierDemodLock=" + this.subCarrierDemodLock + ", symbolSyncLock=" + this.symbolSyncLock
               + ", productionStatus=" + this.productionStatus + "]";
    }

}
