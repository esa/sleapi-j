/**
 * @(#) EE_CLTU_AsyncNotify.java
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
import ccsds.sle.api.isrv.icltu.ICLTU_AsyncNotify;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_Operation;

/**
 * @NameCLTU AsyncNotify Operation@EndName
 * @ResponsibilityThe class implements the CLTU specific AsyncNotify operation.@EndResponsibility
 */
public class EE_CLTU_AsyncNotify extends IEE_SLE_Operation implements ICLTU_AsyncNotify
{

    /**
     * The notificatin type.
     */
    private CLTU_NotificationType notificationType = CLTU_NotificationType.cltuNT_invalid;

    /**
     * The identification of the thrown event.
     */
    private long eventThrownID = 0;

    /**
     * Indicates if at least one PDU has been processed.
     */
    private boolean cltusProcessed = false;

    /**
     * The identification of the last CLTU processed.
     */
    private long cltuLastProcessed = 0;

    /**
     * The radiation start time of the last CLTU processed.
     */
    private ISLE_Time radiationStartTime = null;

    /**
     * The status of the last CLTU processed.
     */
    private CLTU_Status cltuStatus = CLTU_Status.cltuST_invalid;

    /**
     * Indicates if at least one CLTU has been radiated.
     */
    private boolean cltusRadiated = false;

    /**
     * The identification of the last CLTU successfully radiated.
     */
    private long lastCLTUOk = 0;

    /**
     * The radiation stop time of the last CLTU radiated.
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


    private EE_CLTU_AsyncNotify(final EE_CLTU_AsyncNotify right)
    {
        super(right);
        this.notificationType = right.notificationType;
        this.eventThrownID = right.eventThrownID;
        this.cltusProcessed = right.cltusProcessed;
        this.cltuLastProcessed = right.cltuLastProcessed;
        this.cltuStatus = right.cltuStatus;
        this.cltusRadiated = right.cltusRadiated;
        this.lastCLTUOk = right.lastCLTUOk;
        this.productionStatus = right.productionStatus;
        this.uplinkStatus = right.uplinkStatus;
        if (right.radiationStartTime != null)
        {
            this.radiationStartTime = right.radiationStartTime.copy();
        }
        if (right.radiationStopTime != null)
        {
            this.radiationStopTime = right.radiationStopTime.copy();
        }
    }

    /**
     * Creator of the CLTU AsyncNotify Operation.
     */
    public EE_CLTU_AsyncNotify(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, SLE_OpType.sleOT_asyncNotify, version, false, preporter);
        this.notificationType = CLTU_NotificationType.cltuNT_invalid;
        this.eventThrownID = 0;
        this.cltusProcessed = false;
        this.cltuLastProcessed = 0;
        this.radiationStartTime = null;
        this.cltuStatus = CLTU_Status.cltuST_invalid;
        this.cltusRadiated = false;
        this.lastCLTUOk = 0;
        this.radiationStopTime = null;
        this.productionStatus = CLTU_ProductionStatus.cltuPS_invalid;
        this.uplinkStatus = CLTU_UplinkStatus.cltuUS_invalid;
    }

    /**
     * @FunctionSee specification of ICLTU_AsyncNotify.@EndFunction
     */
    @Override
    public synchronized CLTU_NotificationType getNotificationType()
    {
        return this.notificationType;
    }

    @Override
    public synchronized long getEventThrownId()
    {
        assert (this.notificationType == CLTU_NotificationType.cltuNT_actionListCompleted
                || this.notificationType == CLTU_NotificationType.cltuNT_actionListNotCompleted || this.notificationType == CLTU_NotificationType.cltuNT_eventConditionEvFalse) : "invalid getxxx call";
        return this.eventThrownID;

    }

    @Override
    public synchronized boolean getCltusProcessed()
    {
        return this.cltusProcessed;
    }

    @Override
    public synchronized long getCltuLastProcessed()
    {
        assert (getCltusProcessed()) : "invalid getxxx call";
        return this.cltuLastProcessed;
    }

    @Override
    public synchronized ISLE_Time getRadiationStartTime()
    {
        assert (getCltusProcessed()) : "invalid getxxx call";
        return this.radiationStartTime;
    }

    @Override
    public synchronized CLTU_Status getCltuStatus()
    {
        assert (getCltusProcessed()) : "invalid getxxx call";
        return this.cltuStatus;
    }

    @Override
    public synchronized boolean getCltusRadiated()
    {
        return this.cltusRadiated;
    }

    @Override
    public synchronized long getCltuLastOk()
    {
        assert (getCltusRadiated()) : "invalid getxxx call";
        return this.lastCLTUOk;
    }

    @Override
    public synchronized ISLE_Time getRadiationStopTime()
    {
        assert (getCltusRadiated()) : "cltus not radiated";
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
    public synchronized void setNotificationType(CLTU_NotificationType notifyType)
    {
        this.notificationType = notifyType;
    }

    @Override
    public synchronized void setEventThrownId(long id)
    {
        this.eventThrownID = id;
    }

    @Override
    public synchronized void setCltuLastProcessed(long id)
    {
        this.cltuLastProcessed = id;
        this.cltusProcessed = true;
    }

    @Override
    public synchronized void setRadiationStartTime(final ISLE_Time startTime)
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
        this.lastCLTUOk = id;
        this.cltusRadiated = true;
    }

    @Override
    public synchronized void setRadiationStopTime(final ISLE_Time stopTime)
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
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();

        if (this.notificationType == CLTU_NotificationType.cltuNT_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
        if (this.cltusProcessed == false)
        {
            if ((this.notificationType == CLTU_NotificationType.cltuNT_cltuRadiated)
                || (this.notificationType == CLTU_NotificationType.cltuNT_slduExpired))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if ((this.notificationType == CLTU_NotificationType.cltuNT_productionInterrupted)
                && (getOpVersionNumber() == 1))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.cltusRadiated)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        else
        {
            if ((this.radiationStartTime == null)
                && ((this.cltuStatus == CLTU_Status.cltuST_radiationStarted)
                    || (this.cltuStatus == CLTU_Status.cltuST_radiated) || (this.cltuStatus == CLTU_Status.cltuST_interrupted)))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            if (this.cltuStatus == CLTU_Status.cltuST_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        if (!this.cltusRadiated)
        {
            if (this.notificationType == CLTU_NotificationType.cltuNT_cltuRadiated)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }
        else
        {
            if (this.radiationStopTime == null)
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
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_CLTU_AsyncNotify ptmp = new EE_CLTU_AsyncNotify(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder();
        printOn(oss, maxDumpLength);

        oss.append("Notification type      : " + this.notificationType + " \n");
        oss.append("Event thrown id        : " + this.eventThrownID + "\n");
        oss.append("CLTUs processed        : " + this.cltusProcessed + "\n");
        oss.append("CLTU last processed    : " + this.cltuLastProcessed + "\n");
        if (this.radiationStartTime != null)
        {
            String str = this.radiationStartTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth,
                                                                SLE_TimeRes.sleTR_microSec);
            oss.append("Radiation start time   : " + str + "\n");
        }
        else
        {
            oss.append("Radiation start time   : \n");
        }
        oss.append("CLTU status            : " + this.cltuStatus + "\n");

        oss.append("CLTU radiated          : " + this.cltusRadiated + "\n");

        oss.append("Last CLTU OK           : " + this.lastCLTUOk + "\n");
        if (this.radiationStopTime != null)
        {
            String str = this.radiationStopTime
                    .getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Radiation stop time    : " + str + "\n");
        }
        else
        {
            oss.append("Radiation stop time    : \n");
        }
        oss.append("Production status      : " + this.productionStatus + "\n");
        oss.append("Uplink status          : " + this.uplinkStatus + "\n");

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
        else if (iid == ICLTU_AsyncNotify.class)
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
        return "EE_CLTU_AsyncNotify [notificationType=" + this.notificationType + ", eventThrownID="
               + this.eventThrownID + ", cltusProcessed=" + this.cltusProcessed + ", cltuLastProcessed="
               + this.cltuLastProcessed + ", radiationStartTime="
               + ((this.radiationStartTime != null) ? this.radiationStartTime : "") + ", cltuStatus=" + this.cltuStatus
               + ", cltusRadiated=" + this.cltusRadiated + ", lastCLTUOk=" + this.lastCLTUOk + ", radiationStopTime="
               + ((this.radiationStopTime != null) ? this.radiationStopTime : "") + ", productionStatus="
               + this.productionStatus + ", uplinkStatus=" + this.uplinkStatus + "]";
    }

}
