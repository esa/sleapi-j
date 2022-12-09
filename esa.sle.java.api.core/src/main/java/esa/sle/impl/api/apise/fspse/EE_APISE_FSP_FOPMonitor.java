package esa.sle.impl.api.apise.fspse;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isrv.ifsp.IFSP_FOPMonitor;
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * The class implements IFSP_FOPMonitor. The class provides access to the FSP
 * parameters related to the FOP machine of the VC.
 */
public class EE_APISE_FSP_FOPMonitor implements IFSP_FOPMonitor
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_FSP_FOPMonitor.class.getName());

    /**
     * The FOP sliding window.
     */

    private long fopSlidingWindow = 0;

    /**
     * Specifies the FOP behaviour in case of a timeout.
     */

    private FSP_TimeoutType timeoutType = FSP_TimeoutType.fspTT_invalid;

    /**
     * The initial value for countdown timer when AD or BD frame is transmitted.
     */

    private long timerInitial = 0;

    /**
     * The maximum number of times the first frame on the Sent Queue may be
     * transmitted.
     */

    private long transmissionLimit = 0;

    /**
     * The current FOP Transmitter Frame Sequence Number V(S).
     */

    private long transmitterFrameSequenceNum = 0;

    /**
     * The current value of the FOP state.
     */

    private FSP_FopState fopState = FSP_FopState.fspFS_invalid;

    /**
     * The priority list of the MAP multiplexing scheme 'absolute priority'.
     */

    private FSP_AbsolutePriority[] mapPriorityList = null;

    /**
     * The polling vector for the MAP multiplexing scheme 'polling vector'.
     */

    private long[] mapPollingVector = null;

    /**
     * The MAP multiplexing scheme in effect.
     */

    private FSP_MuxScheme mapMuxScheme = FSP_MuxScheme.fspMS_invalid;

    /**
     * The pointer to the SI.
     */

    private EE_APISE_FSP_PFSI fspSI = null;

    private final ReentrantLock obj = new ReentrantLock();


    /**
     * Initializes the FSP FOP Monitor object with the supplied data.
     */
    public EE_APISE_FSP_FOPMonitor(EE_APISE_FSP_PFSI fspSI)
    {
        this.fspSI = fspSI;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IFSP_FOPMonitor.class)
        {
            return (T) this;
        }
        else
        {
            return this.fspSI.queryInterface(iid);
        }
    }

    @Override
    public void setFopSlidingWindow(long width)
    {
        this.obj.lock();
        this.fopSlidingWindow = width;
        this.obj.unlock();
    }

    @Override
    public void setTimeoutType(FSP_TimeoutType type)
    {
        this.obj.lock();
        this.timeoutType = type;
        this.obj.unlock();
    }

    @Override
    public void setTimerInitial(long timer)
    {
        this.obj.lock();
        this.timerInitial = timer;
        this.obj.unlock();

    }

    @Override
    public void setTransmissionLimit(long limit)
    {
        this.obj.lock();
        this.transmissionLimit = limit;
        this.obj.unlock();
    }

    @Override
    public void setTransmitterFrameSequenceNumber(long number)
    {
        this.obj.lock();
        this.transmitterFrameSequenceNum = number;
        this.obj.unlock();
    }

    @Override
    public void setFopState(FSP_FopState state)
    {
        this.obj.lock();
        this.fopState = state;
        this.obj.unlock();
    }

    @Override
    public void setMapMuxScheme(FSP_MuxScheme scheme)
    {
        this.obj.lock();
        this.mapMuxScheme = scheme;
        this.obj.unlock();
    }

    @Override
    public long getFopSlidingWindowWidth()
    {
        return this.fopSlidingWindow;
    }

    @Override
    public FSP_TimeoutType getTimeoutType()
    {
        return this.timeoutType;
    }

    @Override
    public long getTimerInitial()
    {
        return this.timerInitial;
    }

    @Override
    public long getTransmissionLimit()
    {
        return this.transmissionLimit;
    }

    @Override
    public long getTransmitterFrameSequenceNumber()
    {
        return this.transmitterFrameSequenceNum;
    }

    @Override
    public FSP_FopState getFopState()
    {
        return this.fopState;
    }

    @Override
    public FSP_MuxScheme getMapMuxScheme()
    {
        return this.mapMuxScheme;
    }

    /**
     * Performs FSP provider service instance specific checks on the FOP Monitor
     * parameters.
     */
    public HRESULT doConfigCompleted()
    {
        HRESULT rc = HRESULT.S_OK;

        if (this.fopSlidingWindow < EE_APISE_FSP_Limits.getMinFopSlidingWindow()
            || this.fopSlidingWindow > EE_APISE_FSP_Limits.getMaxFopSlidingWindow())
        {
            logAlarm("FOP Sliding Window out of range");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (this.timeoutType == FSP_TimeoutType.fspTT_invalid)
        {
            logAlarm("Invalid or missing Timeout Type");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (this.timerInitial == 0)
        {
            logAlarm("Invalid or missing Timer Initial");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (this.transmissionLimit < EE_APISE_FSP_Limits.getMinTransmissionLimit()
            || this.transmissionLimit > EE_APISE_FSP_Limits.getMaxTransmissionLimit())
        {
            logAlarm("Transmission Limit out of range");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (this.transmitterFrameSequenceNum < EE_APISE_FSP_Limits.getMinTransmitterFrameSequenceNum()
            || this.transmitterFrameSequenceNum > EE_APISE_FSP_Limits.getMaxTransmitterFrameSequenceNum())
        {
            logAlarm("Transmitter Frame Sequence Number out of range");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (this.fopState == FSP_FopState.fspFS_invalid)
        {
            logAlarm("Invalid or missing FOP State");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (this.mapMuxScheme == FSP_MuxScheme.fspMS_absolutePriority)
        {
            if (this.mapPriorityList == null || this.mapPriorityList.length == 0)
            {
                logAlarm("Empty Map Priority List");
                rc = HRESULT.SLE_E_CONFIG;
            }
            for (FSP_AbsolutePriority element : this.mapPriorityList)
            {
                if (element.getMapOrVc() > EE_APISE_FSP_Limits.getMaxMapOrVcId())
                {
                    logAlarm("Map Priority List: Map Id out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
                if (element.getPriority() < EE_APISE_FSP_Limits.getMinAbsolutePriority()
                    || element.getPriority() > EE_APISE_FSP_Limits.getMaxAbsolutePriority())
                {
                    logAlarm("Map Priority List: Priority out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
            }
        }
        else if (this.mapMuxScheme == FSP_MuxScheme.fspMS_pollingVector)
        {
            if (this.mapPollingVector == null || this.mapPollingVector.length == 0)
            {
                logAlarm("Empty Map Polling Vector");
                rc = HRESULT.SLE_E_CONFIG;
            }
            for (long element : this.mapPollingVector)
            {
                if (element > EE_APISE_FSP_Limits.getMaxMapOrVcId())
                {
                    logAlarm("Map Polling Vector: Map Id out of range");
                    rc = HRESULT.SLE_E_CONFIG;
                }
            }
        }
        else if (this.mapMuxScheme == FSP_MuxScheme.fspMS_invalid)
        {
            logAlarm("Invalid or missing Mux Scheme");
            rc = HRESULT.SLE_E_CONFIG;
        }
        // if map mux scheme is fifo, nothing to check

        return rc;

    }

    /**
     * Initializes the supplied FSP-GET-PARAMETER-operation with the current
     * status information data.
     */
    public HRESULT setUpGetParameter(IFSP_GetParameter prm)
    {

        FSP_ParameterName pname = prm.getRequestedParameter();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(" pname: " + pname);
        }

        switch (pname)
        {
        case fspPN_fopSlidingWindow:
        {
            prm.setFopSlidingWindow(this.fopSlidingWindow);
            return HRESULT.S_OK;
        }
        case fspPN_fopState:
        {
            prm.setFopState(this.fopState);
            return HRESULT.S_OK;
        }
        case fspPN_mapMuxControl:
        {
            if (this.mapMuxScheme == FSP_MuxScheme.fspMS_absolutePriority)
            {
                prm.setMapPriorityList(this.mapPriorityList);
            }
            else if (this.mapMuxScheme == FSP_MuxScheme.fspMS_pollingVector)
            {
                prm.setMapPollingVector(this.mapPollingVector);
            }
            else
            {
                // map mux scheme is fifo, nothing to set.
                prm.setMapPriorityList(null);
            }
            return HRESULT.S_OK;
        }
        case fspPN_mapMuxScheme:
        {
            prm.setMapMuxScheme(this.mapMuxScheme);
            return HRESULT.S_OK;
        }
        case fspPN_timeoutType:
        {
            prm.setTimeoutType(this.timeoutType);
            return HRESULT.S_OK;
        }
        case fspPN_timerInitial:
        {
            prm.setTimerInitial(this.timerInitial);
            return HRESULT.S_OK;
        }
        case fspPN_transmissionLimit:
        {
            prm.setTransmissionLimit(this.transmissionLimit);
            return HRESULT.S_OK;
        }
        case fspPN_transmitterFrameSequenceNumber:
        {
            prm.setTransmitterFrameSequenceNumber(this.transmitterFrameSequenceNum);
            return HRESULT.S_OK;
        }
        default:
            return HRESULT.SLE_E_UNKNOWN;
        }
    }

    /**
     * Issues a configuration alarm with the message supplied as argument.
     */
    public void logAlarm(String msg)
    {
        this.fspSI.logRecord(SLE_LogMessageType.sleLM_alarm, EE_LogMsg.EE_SE_LM_ConfigError.getCode(), msg);
    }

    @Override
    public void setMapPriorityList(FSP_AbsolutePriority[] priorities)
    {
        this.obj.lock();
        this.mapPriorityList = new FSP_AbsolutePriority[priorities.length];
        for (int i = 0; i < this.mapPriorityList.length; i++)
        {
        	this.mapPriorityList[i] = new FSP_AbsolutePriority(priorities[i].getMapOrVc(), priorities[i].getPriority()); // SLEAPIJ-77
        }
        this.obj.unlock();
    }

    @Override
    public void setMapPollingVector(long[] pvec)
    {

        this.obj.lock();
        this.mapPollingVector = new long[pvec.length];
        for (int i = 0; i < pvec.length; i++)
        {
            this.mapPollingVector[i] = pvec[i];
        }
        this.obj.unlock();

    }

    @Override
    public FSP_AbsolutePriority[] getMapPriorityList()
    {
        if (this.mapMuxScheme != FSP_MuxScheme.fspMS_absolutePriority)
        {
            return null;
        }
        return this.mapPriorityList;
    }

    @Override
    public long[] getMapPollingVector()
    {
        if (this.mapMuxScheme != FSP_MuxScheme.fspMS_pollingVector)
        {
            return null;
        }
        return this.mapPollingVector;
    }

}
