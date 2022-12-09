package esa.sle.impl.api.apipx.pxtml;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_TMLData;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLTimer;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.time.EE_Duration;

public class EE_APIPX_StartingState implements ITMLState
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_StartingState.class.getName());

    private static byte[] CIProtocolID = { 'I', 'S', 'P', '1' };

    @SuppressWarnings("unused")
    private static int CIVersion = 1;

    EE_APIPX_Channel channel;


    public EE_APIPX_StartingState(EE_APIPX_Channel channel)
    {
        this.channel = channel;
        this.channel.startCommThreads();
    }

    @Override
    public void hlConnectReq(String respPortId)
    {
        // N/A
    }

    @Override
    public void tcpConnectCnf()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpConnectCnf invoked");
        }

        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            ((EE_APIPX_InitiatingChannel) this.channel).sendContextMsg();
            this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
            this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
            this.channel.hlConnectedInd();
            // -> S2
            this.channel.setChannelState(new EE_APIPX_DataTransferState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            throw new RuntimeException("The tcpConnectCnf method is not available for responding channel!");
        }
    }

    @Override
    public void tcpConnectInd()
    {
        // N/A
    }

    @Override
    public void tcpDataInd(EE_APIPX_TMLMessage msg)
    {
        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("tcpDataInd invoked - Initiating Channel");
            }

            // TCP.ABORTreq
            this.channel.tcpAbortReq();
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML, 0, false, 0);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("tcpDataInd invoked - Responding Channel");
            }

            if (msg instanceof EE_APIPX_CtxMessage)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("tcpDataInd invoked - Responding Channel Context Message Received");
                }

                // A context message has been received
                // check the protocol and version
                if (!Arrays.equals(((EE_APIPX_CtxMessage) msg).getProtocol(), CIProtocolID)
                    || ((EE_APIPX_CtxMessage) msg).getVersion() != 1)
                {
                    this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
                    // TCP.ABORTreq
                    this.channel.tcpAbortReq();
                    // -> S0
                    this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
                    return;
                }

                // check whether the hb is acceptable
                EE_APIPX_TMLData ptml = this.channel.getDb().getTMLData();
                int maxhbt = ptml.getMaxHB();
                int minhbt = ptml.getMinHB();
                int maxdf = ptml.getMaxDeadFactor();
                int mindf = ptml.getMinDeadFactor();
                boolean hbtNotUsed = ptml.getNonUseHB();
                this.channel.setUsingHBT(hbtNotUsed);

                boolean checkPassed = (((EE_APIPX_CtxMessage) msg).getDeadFactor() >= mindf && ((EE_APIPX_CtxMessage) msg)
                        .getDeadFactor() <= maxdf)
                                      && (((EE_APIPX_CtxMessage) msg).getHbtDuration() >= minhbt && ((EE_APIPX_CtxMessage) msg)
                                              .getHbtDuration() <= maxhbt)
                                      || (((EE_APIPX_CtxMessage) msg).getHbtDuration() == 0 && hbtNotUsed == true);

                if (checkPassed)
                {
                    if (this.channel.trace != null
                        && this.channel.traceLevel.getCode() >= SLE_TraceLevel.sleTL_low.getCode())
                    {
                        this.channel.trace(EE_LogMsg.TMLTR_CONTEXTRCVD.getCode(),
                                           SLE_TraceLevel.sleTL_low,
                                           "Context Message OK");
                    }
                    this.channel.setHbtDuration(new EE_Duration(((EE_APIPX_CtxMessage) msg).getHbtDuration()));
                    this.channel.setHbrDuration(new EE_Duration(((EE_APIPX_CtxMessage) msg).getHbtDuration()
                                                                * ((EE_APIPX_CtxMessage) msg).getDeadFactor()));
                    this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
                    this.channel.setFirstPDU(true);

                    // notify the application
                    this.channel.hlConnectedInd();

                    // -> S2
                    this.channel.setChannelState(new EE_APIPX_DataTransferState(this.channel));
                }
                else
                {
                	// SLEAPIJ-15
                	if((((EE_APIPX_CtxMessage) msg).getDeadFactor() >= mindf && ((EE_APIPX_CtxMessage) msg)
                            .getDeadFactor() <= maxdf) == false)
                	{
                		LOG.severe("Dead factor out of range. Min: " + mindf + " received: " + 
                				((EE_APIPX_CtxMessage) msg).getDeadFactor() + 
                				" max: " + maxdf);
                	}
                	
                	// SLEAPIJ-15
                	if(((((EE_APIPX_CtxMessage) msg).getHbtDuration() >= minhbt && ((EE_APIPX_CtxMessage) msg)
                            .getHbtDuration() <= maxhbt)) == false) {
                		LOG.severe("Heartbeat duration out of range. Min: " + minhbt + " received: " +
                				((EE_APIPX_CtxMessage) msg).getHbtDuration() + 
                				" max: " + maxhbt);
                	}
                
                	
                    this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
                    // PEER ABORT
                    this.channel.peerAbortReq(EE_APIPX_TMLErrors.eeAPIPXtml_heartbeatParamsNotOk.getCode());
                    // -> S4
                    this.channel.setChannelState(new EE_APIPX_ClosingState(this.channel));
                }
            }
            else
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("tcpDataInd invoked - Responding Channel NO Context Message Received!");
                }

                this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
                // TCP.ABORTreq
                this.channel.tcpAbortReq();
                this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
            }
        }
    }

    @Override
    public void hlDisconnectReq()
    {
        // N/A

    }

    @Override
    public void tcpDisconnectInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpDisconnectInd invoked");
        }

        if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            // TCP.DISCONNECTreq
            this.channel.tcpAbortReq();
            // stop TMS timer
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            throw new RuntimeException("the tcpDisconnectInd method is not available for the initiating channel!");
        }
    }

    @Override
    public void delSLEPDUReq(EE_APIPX_TMLMessage pduMsg, boolean last)
    {
        // N/A
    }

    @Override
    public void hlPeerAbortReq(int diagnostic)
    {
        // N/A
    }

    @Override
    public void tcpUrgentDataInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpUrgentDataInd invoked");
        }

        if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            // TCP.ABORTreq
            this.channel.tcpAbortReq();
            // stop TMS timer
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            throw new RuntimeException("the tcpUrgentDataInd method is not available for the initiating channel!");
        }
    }

    @Override
    public void hlResetReq()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hlResetReq invoked");
        }

        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            // TCP.ABORTreq
            this.channel.tcpAbortReq();
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            throw new RuntimeException("the hlResetReq method is not available for the responding channel!");
        }
    }

    @Override
    public void tcpAbortInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpAbortInd invoked");
        }

        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML, 0, false, 0);

            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            throw new RuntimeException("the tcpAbortInd method is not available for the responding channel!");
        }
    }

    @Override
    public void tcpTimeOut()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpTimeOut invoked");
        }

        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            // TCP.ABORTreq
            this.channel.tcpAbortReq();

            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML, 0, false, 0);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            throw new RuntimeException("the tcpTimeOut method is not available for the responding channel!");
        }
    }

    @Override
    public void tcpError(int code, boolean traceAlso, String[] param)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpError invoked");
        }
        this.channel.logError(code, traceAlso, param);
        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            this.channel.logError(EE_LogMsg.TMLCONNECTEDHARD.getCode(), true);

            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML, 0, false, 0);
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
    }

    @Override
    public void tmsTimeout()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tmsTimeout invoked");
        }

        if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            this.channel.trace(EE_LogMsg.TMLTR_ESTABLISHTIMEOUT.getCode(), SLE_TraceLevel.sleTL_high);
            this.channel.logError(EE_LogMsg.TMLESTTIMEOUT.getCode(), false);

            // TCP.ABORTreq
            this.channel.tcpAbortReq();

            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            throw new RuntimeException("the tmsTimeout method is not available for the initiating channel!");
        }
    }

    @Override
    public void cpaTimeout()
    {
        // N/A
    }

    @Override
    public void hbrTimeout()
    {
        // N/A
    }

    @Override
    public void hbtTimeout()
    {
        // N/A
    }

    @Override
    public void manageBadFormMsg()
    {
        // Nothing to be done here

    }

    @Override
    public String toString()
    {
        return "Starting State";
    }
}
