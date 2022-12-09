/**
 * @(#) EE_CLTU_StatusReport.java
 */

package esa.sle.impl.api.apiop.cltuop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;

/**
 * @NameCLTU StatusReport Operation@EndName
 * @ResponsibilityThe class implements the CLTU specific StatusReport operation.@EndResponsibility
 */
public class EE_CLTU_StatusReport extends IEE_SLE_Operation implements ICLTU_StatusReport
{

    /**
     * The identification of the CLTU last processed.
     */
    private long cltuLastProcessed = 0;

    /**
     * The radiation start time of the last CLTU processed.
     */
    private ISLE_Time radiationStartTime = null;

    /**
     * The status of the CLTU last processed.
     */
    private CLTU_Status cltuStatus = CLTU_Status.cltuST_invalid;

    /**
     * The identificaton of the CLTU last radiated.
     */
    private long cltuLastOK = 0;

    /**
     * The radiation stop time of the CLTU last radiated.
     */
    private ISLE_Time radiationStopTime = null;

    /**
     * The production status.
     */
    private CLTU_ProductionStatus productionStatus = CLTU_ProductionStatus.cltuPS_invalid;

    /**
     * The uplink status.
     */
    private CLTU_UplinkStatus uplinkStatus = CLTU_UplinkStatus.cltuUS_invalid;

    /**
     * The number of CLTU that have been received.
     */
    private long numberOfCLTUsReceived = 0;

    /**
     * The number of CLTU that have been processed.
     */
    private long numberOfCLTUsProcessed = 0;

    /**
     * The number of CLTU that have been successfully radiated.
     */
    private long numberOfCLTUsRadiated = 0;

    /**
     * The size of the available CLTU buffer.
     */
    private long cltuBufferAvailable = 0;


    private EE_CLTU_StatusReport(final EE_CLTU_StatusReport right)
    {
        super(right);
        this.cltuLastProcessed = right.cltuLastProcessed;
        if (right.radiationStartTime != null)
        {
            this.radiationStartTime = right.radiationStartTime.copy();
        }
        this.cltuStatus = right.cltuStatus;
        this.cltuLastOK = right.cltuLastOK;
        if (right.radiationStopTime != null)
        {
            this.radiationStopTime = right.radiationStopTime.copy();
        }
        this.productionStatus = right.productionStatus;
        this.uplinkStatus = right.uplinkStatus;
        this.numberOfCLTUsReceived = right.numberOfCLTUsReceived;
        this.numberOfCLTUsProcessed = right.numberOfCLTUsProcessed;
        this.numberOfCLTUsRadiated = right.numberOfCLTUsRadiated;
        this.cltuBufferAvailable = right.cltuBufferAvailable;
    }

    public EE_CLTU_StatusReport(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, SLE_OpType.sleOT_statusReport, version, false, preporter);
        this.cltuLastProcessed = 0;
        this.radiationStartTime = null;
        this.cltuStatus = CLTU_Status.cltuST_invalid;
        this.cltuLastOK = 0;
        this.radiationStopTime = null;
        this.productionStatus = CLTU_ProductionStatus.cltuPS_invalid;
        this.uplinkStatus = CLTU_UplinkStatus.cltuUS_invalid;
        this.numberOfCLTUsReceived = 0;
        this.numberOfCLTUsProcessed = 0;
        this.numberOfCLTUsRadiated = 0;
        this.cltuBufferAvailable = 0;
    }

    @Override
    public synchronized long getCltuLastProcessed()
    {
        assert (this.numberOfCLTUsProcessed > 0) : "error";
        return this.cltuLastProcessed;
    }

    @Override
    public synchronized ISLE_Time getRadiationStartTime()
    {
        assert (((this.numberOfCLTUsProcessed > 0 && (this.cltuStatus != CLTU_Status.cltuST_expired) || (this.cltuStatus != CLTU_Status.cltuST_radiationNotStarted)))) : "error";
        return this.radiationStartTime;
    }

    @Override
    public synchronized CLTU_Status getCltuStatus()
    {
        assert (this.numberOfCLTUsProcessed > 0) : "error";
        return this.cltuStatus;
    }

    @Override
    public synchronized long getCltuLastOk()
    {
        assert (this.numberOfCLTUsRadiated > 0) : "error";
        return this.cltuLastOK;
    }

    @Override
    public synchronized ISLE_Time getRadiationStopTime()
    {
        assert (this.numberOfCLTUsRadiated > 0) : "error";
        return this.radiationStopTime;
    }

    @Override
    public synchronized CLTU_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    @Override
    public synchronized CLTU_UplinkStatus getUplinkStatus()
    {
        return this.uplinkStatus;
    }

    @Override
    public synchronized long getNumberOfCltusReceived()
    {
        return this.numberOfCLTUsReceived;
    }

    @Override
    public synchronized long getNumberOfCltusProcessed()
    {
        return this.numberOfCLTUsProcessed;
    }

    @Override
    public synchronized long getNumberOfCltusRadiated()
    {
        return this.numberOfCLTUsRadiated;
    }

    @Override
    public synchronized long getCltuBufferAvailable()
    {
        return this.cltuBufferAvailable;
    }

    @Override
    public synchronized void setCltuLastProcessed(long id)
    {
        this.cltuLastProcessed = id;
    }

    @Override
    public synchronized void setRadiationStartTime(ISLE_Time startTime)
    {
        this.radiationStartTime = startTime.copy();
    }

    @Override
    public synchronized void putRadiationStartTime(ISLE_Time pstartTime)
    {
        this.radiationStartTime = pstartTime;
    }

    @Override
    public synchronized void setCltuStatus(CLTU_Status status)
    {
        this.cltuStatus = status;
    }

    @Override
    public synchronized void setCltuLastOk(long id)
    {
        this.cltuLastOK = id;
    }

    @Override
    public synchronized void setRadiationStopTime(ISLE_Time stopTime)
    {
        this.radiationStopTime = stopTime.copy();
    }

    @Override
    public synchronized void putRadiationStopTime(ISLE_Time pstopTime)
    {
        this.radiationStopTime = pstopTime;
    }

    @Override
    public synchronized void setProductionStatus(CLTU_ProductionStatus status)
    {
        this.productionStatus = status;
    }

    @Override
    public synchronized void setUplinkStatus(CLTU_UplinkStatus status)
    {
        this.uplinkStatus = status;
    }

    @Override
    public synchronized void setNumberOfCltusReceived(long numRecv)
    {
        this.numberOfCLTUsReceived = numRecv;
    }

    @Override
    public synchronized void setNumberOfCltusProcessed(long numProc)
    {
        this.numberOfCLTUsProcessed = numProc;
    }

    @Override
    public synchronized void setNumberOfCltusRadiated(long numRad)
    {
        this.numberOfCLTUsRadiated = numRad;
    }

    @Override
    public synchronized void setCltuBufferAvailable(long size)
    {
        this.cltuBufferAvailable = size;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {

        super.verifyInvocationArguments();

        if (this.radiationStartTime == null)
        {
            if (((this.cltuStatus == CLTU_Status.cltuST_radiationStarted)
                 || (this.cltuStatus == CLTU_Status.cltuST_radiated) || (this.cltuStatus == CLTU_Status.cltuST_interrupted))
                && (this.numberOfCLTUsProcessed > 0))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }

        if (this.cltuStatus == CLTU_Status.cltuST_invalid && (this.numberOfCLTUsProcessed > 0))
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        if (this.radiationStopTime == null)
        {
            if (this.numberOfCLTUsRadiated > 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        if (this.productionStatus == CLTU_ProductionStatus.cltuPS_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
        if (this.uplinkStatus == CLTU_UplinkStatus.cltuUS_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
        if (this.numberOfCLTUsReceived < this.numberOfCLTUsProcessed)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        if (this.numberOfCLTUsProcessed < this.numberOfCLTUsRadiated)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_CLTU_StatusReport ptmp = new EE_CLTU_StatusReport(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("CLTU Last Processed    : " + this.cltuLastProcessed + "\n");
        if (this.radiationStartTime != null)
        {
            oss.append("Radiation start time   : ");
            String str = this.radiationStartTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                SLE_TimeRes.sleTR_microSec);
            oss.append(str + "\n");
        }
        else
        {
            oss.append("Radiation start time   : \n");
        }
        oss.append("CLTU status            : " + this.cltuStatus + "\n");
        oss.append("CLTU Last OK           : " + this.cltuLastOK + "\n");
        oss.append("Radiation stop time    : ");
        if (this.radiationStopTime != null)
        {
            String str = this.radiationStopTime
                    .getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append(str);
        }
        oss.append("\n");
        oss.append("Production status       : " + this.productionStatus + "\n");
        oss.append("Uplink status           : " + this.uplinkStatus + "\n");
        oss.append("Num. of cltus received  : " + this.numberOfCLTUsReceived + "\n");
        oss.append("Num. of cltus processed : " + this.numberOfCLTUsProcessed + "\n");
        oss.append("Num. of cltus radiated  : " + this.numberOfCLTUsRadiated + "\n");
        oss.append("CLTU buffer available   : " + this.cltuBufferAvailable + "\n");
        oss.append("\n");
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
        else if (iid == ICLTU_StatusReport.class)
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
        return "EE_CLTU_StatusReport [cltuLastProcessed=" + this.cltuLastProcessed + ", radiationStartTime="
               + ((this.radiationStartTime != null) ? this.radiationStartTime : "") + ", cltuStatus=" + this.cltuStatus
               + ", cltuLastOK=" + this.cltuLastOK + ", radiationStopTime="
               + ((this.radiationStopTime != null) ? this.radiationStopTime : "") + ", productionStatus="
               + this.productionStatus + ", uplinkStatus=" + this.uplinkStatus + ", numberOfCLTUsReceived="
               + this.numberOfCLTUsReceived + ", numberOfCLTUsProcessed=" + this.numberOfCLTUsProcessed
               + ", numberOfCLTUsRadiated=" + this.numberOfCLTUsRadiated + ", cltuBufferAvailable="
               + this.cltuBufferAvailable + "]";
    }

}
