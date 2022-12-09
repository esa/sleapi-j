package esa.sle.impl.api.apipx.pxtml;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TCPErrors;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLTimer;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_APIPX_DataTransferState implements ITMLState
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_DataTransferState.class.getName());

    EE_APIPX_Channel channel;


    public EE_APIPX_DataTransferState(EE_APIPX_Channel channel)
    {
        this.channel = channel;
    }

    @Override
    public void hlConnectReq(String respPortId)
    {
        // N/A
    }

    @Override
    public void tcpConnectCnf()
    {
        // N/A
    }

    @Override
    public void tcpConnectInd()
    {
        // N/A
    }

    @Override
    public void tcpDataInd(EE_APIPX_TMLMessage msg)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpDataInd invoked");
        }

        if (msg instanceof EE_APIPX_PDUMessage)
        {
            if (this.channel instanceof EE_APIPX_RespondingChannel)
            {
                if (this.channel.isFirstPDU)
                {
                    this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
                    this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
                    this.channel.setFirstPDU(false);
                }
                else
                {
                    this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
                }
            }
            else if (this.channel instanceof EE_APIPX_InitiatingChannel)
            {
                this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
            }

            // extract the PDU and send it to the inform
            this.channel.forwardPDU(((EE_APIPX_PDUMessage) msg).getBody());
        }
        else if (msg instanceof EE_APIPX_CtxMessage)
        {
            if (this.channel instanceof EE_APIPX_RespondingChannel && this.channel.isFirstPDU)
            {
                this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
                // HL.PROTOCOL-ABORTind
                this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                          EE_APIPX_TMLErrors.eeAPIPXtml_protocolError.getCode(),
                                          false,
                                          0);
            }

            // PEER-ABORT
            this.channel.peerAbortReq(EE_APIPX_TMLErrors.eeAPIPXtml_protocolError.getCode());
            // -> S4
            this.channel.setChannelState(new EE_APIPX_ClosingState(this.channel));
        }
        else if (msg instanceof EE_APIPX_HBMessage)
        {
            this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
        }
    }

    @Override
    public void hlDisconnectReq()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hlDisconnectReq invoked");
        }

        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            // TCP.DISCONNECTreq
            this.channel.tcpAbortReq();
            ((EE_APIPX_InitiatingChannel) this.channel).cleanup();
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            this.channel.setLocalPeerAbort(false);
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
            this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
            // -> S4
            this.channel.setChannelState(new EE_APIPX_ClosingState(this.channel));
        }
    }

    @Override
    public void tcpDisconnectInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpDisconnectInd invoked");
        }

        // TCP.DISCONNECTreq
        this.channel.tcpAbortReq();
        // HL.PROTOCOL-ABORTind
        this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML, 0, false, 0);
        this.channel.cleanup();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void delSLEPDUReq(EE_APIPX_TMLMessage pduMsg, boolean last)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("delSLEPDUReq invoked");
        }
        if (last)
        {
            this.channel.onFinalPdu();
            this.channel.setChannelState(new EE_APIPX_ClosingState(this.channel));
        }
        this.channel.sendPDU(pduMsg);
        this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
    }

    @Override
    public void hlPeerAbortReq(int diagnostic)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hlPeerAbortReq invoked");
        }

        // PEER-ABORT
        this.channel.setLocalPeerAbort(true);
        // -> S4
        this.channel.setChannelState(new EE_APIPX_ClosingState(this.channel));

        this.channel.peerAbortReq(diagnostic);
    }

    @Override
    public void tcpUrgentDataInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpUrgentDataInd invoked");
        }

        this.channel.setLocalPeerAbort(false);
        // -> S3
        this.channel.setChannelState(new EE_APIPX_PeerAbortingState(this.channel));
    }

    @Override
    public void hlResetReq()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hlResetReq invoked");
        }

        // TCP.ABORTreq
        this.channel.tcpAbortReq();
        this.channel.cleanup();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void tcpAbortInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpAbortInd invoked");
        }

        // HL.PROTOCOL-ABORTind
        this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                  0,
                                  false,
                                  EE_APIPX_TCPErrors.eeAPIPXtcp_other.getCode());
        this.channel.cleanup();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void tcpTimeOut()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpTimeOut invoked");
        }

        // TCP.ABORTreq
        this.channel.tcpAbortReq();
        // HL.PROTOCOL-ABORTind
        this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                  0,
                                  false,
                                  EE_APIPX_TCPErrors.eeAPIPXtcp_sendTimeout.getCode());
        this.channel.cleanup();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void tcpError(int code, boolean traceAlso, String[] param)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpError invoked");
        }
        this.channel.logError(code, traceAlso, param);
        // HL.PROTOCOL-ABORTind
        this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML, 0, false, 0);
        this.channel.cleanup();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
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
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                      EE_APIPX_TMLErrors.eeAPIPXtml_establishTimeout.getCode(),
                                      false,
                                      0);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            throw new RuntimeException("The tmsTimeout method is not available for initiating channel!");
        }
    }

    @Override
    public void hbrTimeout()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hbrTimeout invoked");
        }

        // TCP.ABORTreq
        this.channel.tcpAbortReq();

        // HL.PROTOCOL-ABORTind
        this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                  EE_APIPX_TMLErrors.eeAPIPXtml_HBRTimeout.getCode(),
                                  false,
                                  0);
        this.channel.cleanup();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void hbtTimeout()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hbtTimeout invoked");
        }

        this.channel.sendHbMsg();
        this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
    }

    @Override
    public void cpaTimeout()
    {
        // N/A
    }

    @Override
    public void manageBadFormMsg()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("manageBadFormMsg invoked");
        }

        // HL.PROTOCOL-ABORTind
        this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                  EE_APIPX_TMLErrors.eeAPIPXtml_badTMLMsg.getCode(),
                                  false,
                                  0);

        if (this.channel instanceof EE_APIPX_RespondingChannel && this.channel.isFirstPDU)
        {
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
        }

        // PEER-ABORT
        this.channel.peerAbortReq(EE_APIPX_TMLErrors.eeAPIPXtml_badTMLMsg.getCode());
        // -> S4
        this.channel.setChannelState(new EE_APIPX_ClosingState(this.channel));
    }

    @Override
    public String toString()
    {
        return "Data Transfer State";
    }
}
