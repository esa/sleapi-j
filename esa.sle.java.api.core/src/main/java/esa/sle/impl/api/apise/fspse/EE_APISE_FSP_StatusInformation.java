package esa.sle.impl.api.apise.fspse;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.IFSP_StatusReport;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_MTSStatusInformation;

/**
 * The class holds all packet status information parameters. These are update
 * via the interface IFSP_SIUpdate. The client is responsible to lock/unlock the
 * object.@EndBehaviour Note that the accessor and modifier functions
 * get_<Attribute> and set_<Attribute> are generated automatically for the
 * public interface.
 */
public class EE_APISE_FSP_StatusInformation extends EE_APISE_MTSStatusInformation
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_FSP_StatusInformation.class.getName());

    /**
     * The FSP production status
     */
    private FSP_ProductionStatus productionStatus = FSP_ProductionStatus.fspPS_invalid;

    /**
     * The size of the available packetbuffer.
     */
    private long packetBufferAvailable = 0;

    /**
     * The number of AD packets received. This number is incremented by one for
     * every TRANSFER-DATA return with posistive result.
     */
    private long numADPacketsReceived = 0;

    /**
     * The number of BD packets received. This number is incremented by one for
     * every TRANSFER-DATA return with posistive result.
     */
    private long numBDPacketsReceived = 0;

    /**
     * The number of AD packets for which radiation has been attempted. This
     * number is incremented in PacketStarted() and PacketNotStarted().
     */
    private long numADPacketsProcessed = 0;

    /**
     * The number of BD packets for which radiation has been attempted. This
     * number is incremented in PacketStarted() and PacketNotStarted().
     */
    private long numBDPacketsProcessed = 0;

    /**
     * The number of AD packets which have been radiated. This number shall be
     * incremented in PacketRadiated.
     */
    private long numADPacketsRadiated = 0;

    /**
     * The number of BD packets which have been radiated. This number shall be
     * incremented in PacketRadiated.
     */
    private long numBDPacketsRadiated = 0;

    /**
     * The number of packets, which have been acknowledged. This number shall be
     * incremented in PacketAcknowledged.
     */
    private long numPacketsAcknowledged = 0;

    /**
     * The next expected sldu Id.
     */
    private long expectedSlduId = 0;

    /**
     * The next expected directive Id.
     */
    private long expectedDirectiveId = 0;

    /**
     * The next expected event invocation Id.
     */
    private long expectedEventInvId = 0;

    /**
     * Directive invocation online.
     */
    private SLE_YesNo directiveInvocationOnline = SLE_YesNo.sleYN_invalid;


    public EE_APISE_FSP_StatusInformation()
    {}

    /**
     * Increments the number of received AD packets.
     */
    public void incrNumReceivedAD()
    {
        this.numADPacketsReceived++;
    }

    /**
     * Increments the number of received BD packets.
     */
    public void incrNumReceivedBD()
    {
        this.numBDPacketsReceived++;
    }

    /**
     * Increments the number of processed AD packets.
     */
    public void incrNumProcessedAD()
    {
        this.numADPacketsProcessed++;
    }

    /**
     * Increments the number of processed BD packets.
     */
    public void incrNumProcessedBD()
    {
        this.numBDPacketsProcessed++;
    }

    /**
     * Increments the number of radiated AD packets.
     */
    public void incrNumRadiatedAD()
    {
        this.numADPacketsRadiated++;
    }

    /**
     * Increments the number of radiated BD packets.
     */
    public void incrNumRadiatedBD()
    {
        this.numBDPacketsRadiated++;
    }

    /**
     * Increments the number of acknowledged packets.
     */
    public void incrNumAcknowledged()
    {
        this.numPacketsAcknowledged++;
    }

    /**
     * Initializes the supplied status-report-operation with the current status
     * information data.
     */
    public void setUpReport(IFSP_StatusReport sr)
    {
        sr.setProductionStatus(this.productionStatus);
        sr.setNumberOfADPacketsReceived(this.numADPacketsReceived);
        sr.setNumberOfBDPacketsReceived(this.numBDPacketsReceived);
        sr.setNumberOfADPacketsProcessed(this.numADPacketsProcessed);
        sr.setNumberOfBDPacketsProcessed(this.numBDPacketsProcessed);
        sr.setNumberOfADPacketsRadiated(this.numADPacketsRadiated);
        sr.setNumberOfBDPacketsRadiated(this.numBDPacketsRadiated);
        sr.setNumberOfPacketsAcknowledged(this.numPacketsAcknowledged);
        sr.setPacketBufferAvailable(this.packetBufferAvailable);
    }

    /**
     * Initializes the supplied CLTU-GET-PARAMETER-operation with the current
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
        case fspPN_expectedDirectiveId:
        {
            prm.setExpectedDirectiveId(this.expectedDirectiveId);
            return HRESULT.S_OK;
        }
        case fspPN_expectedEventInvocationId:
        {
            prm.setExpectedEventInvocationId(this.expectedEventInvId);
            return HRESULT.S_OK;
        }
        case fspPN_expectedSlduIdentification:
        {
            prm.setExpectedSlduId(this.expectedSlduId);
            return HRESULT.S_OK;
        }
        case fspPN_directiveInvocationOnline:
        {
            prm.setDirectiveInvocationOnline(this.directiveInvocationOnline);
            return HRESULT.S_OK;
        }
        default:
            return HRESULT.SLE_E_UNKNOWN;
        }

    }

    /**
     * Returns true if the supplied production status is 'operational AD and BS'
     * or 'operational BD' or 'operational AD suspended'.
     */
    public boolean isOperational(FSP_ProductionStatus ps)
    {
        if (ps == FSP_ProductionStatus.fspPS_operationalBd || ps == FSP_ProductionStatus.fspPS_operationalAdAndBd
            || ps == FSP_ProductionStatus.fspPS_operationalAdSuspended)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns true if the current production status is 'operational AD and BS'
     * or 'operational BD' or 'operational AD suspended'.
     */
    public boolean isOperational()
    {
        return isOperational(this.productionStatus);
    }

    /**
     * Checks if the production status transition from old to new production
     * status (as indicated by the input arguments) is valid. The function
     * returns S_OK if the status transition is valid. If the production status
     * transition is not valid, SLE_E_SEQUENCE is returned. This function shall
     * be called prior to a change of the production status.
     */
    public HRESULT checkTransition(FSP_ProductionStatus oldState, FSP_ProductionStatus newState)
    {

        switch (oldState)
        {
        case fspPS_configured:
        {
            if (newState == FSP_ProductionStatus.fspPS_halted || newState == FSP_ProductionStatus.fspPS_operationalBd)
            {
                return HRESULT.S_OK;
            }
            else
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
        }

        case fspPS_operationalBd:
        {
            // consider main state transitions +
            // operational sub-state transitions
            if (newState == FSP_ProductionStatus.fspPS_interrupted || newState == FSP_ProductionStatus.fspPS_halted
                || newState == FSP_ProductionStatus.fspPS_operationalAdAndBd)
            {
                return HRESULT.S_OK;
            }
            else
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
        }
        case fspPS_operationalAdAndBd:
        {
            // consider main state transitions +
            // operational sub-state transitions
            if (newState == FSP_ProductionStatus.fspPS_interrupted || newState == FSP_ProductionStatus.fspPS_halted
                || newState == FSP_ProductionStatus.fspPS_operationalAdSuspended
                || newState == FSP_ProductionStatus.fspPS_operationalBd)
            {
                return HRESULT.S_OK;
            }
            else
            {
                return HRESULT.SLE_E_SEQUENCE;
            }

        }
        case fspPS_operationalAdSuspended:
        {
            // consider main state transitions +
            // operational sub-state transitions
            if (newState == FSP_ProductionStatus.fspPS_interrupted || newState == FSP_ProductionStatus.fspPS_halted
                || newState == FSP_ProductionStatus.fspPS_operationalBd
                || newState == FSP_ProductionStatus.fspPS_operationalAdAndBd)
            {
                return HRESULT.S_OK;
            }
            else
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
        }

        case fspPS_interrupted:
        {
            if (newState == FSP_ProductionStatus.fspPS_halted || newState == FSP_ProductionStatus.fspPS_operationalBd)
            {
                return HRESULT.S_OK;
            }
            else
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
        }

        case fspPS_halted:
        {
            if (newState == FSP_ProductionStatus.fspPS_configured)
            {
                return HRESULT.S_OK;
            }
            else
            {
                return HRESULT.SLE_E_SEQUENCE;
            }
        }

        default:
            // fspPS_invalid is not supported
            return HRESULT.SLE_E_SEQUENCE;
        }

    }

    public final FSP_ProductionStatus getProductionStatus()
    {
        return this.productionStatus;
    }

    public void setProductionStatus(FSP_ProductionStatus value)
    {
        this.productionStatus = value;
    }

    public final long getPacketBufferAvailable()
    {
        return this.packetBufferAvailable;
    }

    public void setPacketBufferAvailable(long value)
    {
        this.packetBufferAvailable = value;
    }

    public final long getNumADPacketsReceived()
    {
        return this.numADPacketsReceived;
    }

    public void setNumADPacketsReceived(long value)
    {
        this.numADPacketsReceived = value;
    }

    public final long getNumBDPacketsReceived()
    {
        return this.numBDPacketsReceived;

    }

    public void setNumBDPacketsReceived(long value)
    {
        this.numBDPacketsReceived = value;
    }

    public final long getNumADPacketsProcessed()
    {
        return this.numADPacketsProcessed;
    }

    public void setNumADPacketsProcessed(long value)
    {
        this.numADPacketsProcessed = value;
    }

    public final long getNumBDPacketsProcessed()
    {
        return this.numBDPacketsProcessed;
    }

    public void setNumBDPacketsProcessed(long value)
    {
        this.numBDPacketsProcessed = value;
    }

    public final long getNumADPacketsRadiated()
    {
        return this.numADPacketsRadiated;
    }

    public void setNumADPacketsRadiated(long value)
    {
        this.numADPacketsRadiated = value;
    }

    public final long getNumBDPacketsRadiated()
    {
        return this.numBDPacketsRadiated;
    }

    public void setNumBDPacketsRadiated(long value)
    {
        this.numBDPacketsRadiated = value;
    }

    public final long getNumPacketsAcknowledged()
    {
        return this.numPacketsAcknowledged;

    }

    public void setNumPacketsAcknowledged(long value)
    {
        this.numPacketsAcknowledged = value;
    }

    public final long getExpectedSlduId()
    {
        return this.expectedSlduId;
    }

    public void setExpectedSlduId(long value)
    {
        this.expectedSlduId = value;
    }

    public final long getExpectedDirectiveId()
    {
        return this.expectedDirectiveId;

    }

    public void setExpectedDirectiveId(long value)
    {
        this.expectedDirectiveId = value;
    }

    public final long getExpectedEventInvId()
    {
        return this.expectedEventInvId;

    }

    public void setExpectedEventInvId(long value)
    {
        this.expectedEventInvId = value;
    }

    public final SLE_YesNo getDirectiveInvocationOnline()
    {
        return this.directiveInvocationOnline;
    }

    public void setDirectiveInvocationOnline(SLE_YesNo value)
    {
        this.directiveInvocationOnline = value;
    }

}
