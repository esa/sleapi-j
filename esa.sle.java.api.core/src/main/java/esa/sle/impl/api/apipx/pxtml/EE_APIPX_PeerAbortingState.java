package esa.sle.impl.api.apipx.pxtml;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLTimer;

public class EE_APIPX_PeerAbortingState implements ITMLState
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_PeerAbortingState.class.getName());

    EE_APIPX_Channel channel;


    public EE_APIPX_PeerAbortingState(EE_APIPX_Channel channel)
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

        // discard data
        int ub = this.channel.getUrgentByte();
        if (ub != -1)
        {
            if (!this.channel.isLocalPeerAbort())
            {
                if (SLE_PeerAbortDiagnostic.getDiagByCode(ub) != null)
                {
                    // SLE diagnostic
                    // HL.PEER-ABORTind
                    this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.peerTML, ub, true, 0);
                }
                else if (EE_APIPX_TMLErrors.getDiagByCode(ub) != null)
                {
                    // TML diagnostic
                    // HL.PROTOCOL-ABORTind
                    this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.peerTML, ub, false, 0);
                }
            }

            // TCP.DISCONNECTreq:
            // close the socket and stop the threads
            this.channel.cleanup();

            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
    }

    @Override
    public void hlDisconnectReq()
    {
        // reject(aborting)
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

        if (!this.channel.isLocalPeerAbort())
        {
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.peerTML,
                                      this.channel.getUrgentByte(),
                                      false,
                                      0);
        }

        this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void delSLEPDUReq(EE_APIPX_TMLMessage pduMsg, boolean last)
    {
        // reject(aborting)
    }

    @Override
    public void hlPeerAbortReq(int diagnostic)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hlPeerAbortReq invoked");
        }
        this.channel.setLocalPeerAbort(true);
    }

    @Override
    public void tcpUrgentDataInd()
    {
        // N/A
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
        this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
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

        if (!this.channel.isLocalPeerAbort())
        {
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.peerTML,
                                      this.channel.getUrgentByte(),
                                      false,
                                      0);
        }

        this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void tcpTimeOut()
    {
        // N/A
    }

    @Override
    public void tcpError(int code, boolean traceAlso, String[] param)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpError invoked");
        }
        this.channel.logError(code, traceAlso, param);
        if (!this.channel.isLocalPeerAbort())
        {
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.peerTML,
                                      this.channel.getUrgentByte(),
                                      false,
                                      0);
        }

        this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void tmsTimeout()
    {
        // N/A
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

        if (!this.channel.isLocalPeerAbort())
        {
            // HL.PROTOCOL-ABORTind
            this.channel.forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator.localTML,
                                      EE_APIPX_TMLErrors.eeAPIPXtml_HBRTimeout.getCode(),
                                      false,
                                      0);
        }

        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void hbtTimeout()
    {
        // N/A
    }

    @Override
    public void cpaTimeout()
    {
        // N/A
    }

    @Override
    public void manageBadFormMsg()
    {
        // Nothing to do here
    }

    @Override
    public String toString()
    {
        return "Peer Aborting State";
    }

}
