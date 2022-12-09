package esa.sle.impl.api.apipx.pxtml;

import java.util.logging.Level;
import java.util.logging.Logger;

import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLTimer;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_APIPX_ClosingState implements ITMLState
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_ClosingState.class.getName());

    EE_APIPX_Channel channel;


    public EE_APIPX_ClosingState(EE_APIPX_Channel channel)
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

        if (!this.channel.isLocalPeerAbort())
        {
            // TCP.ABORTreq
            this.channel.tcpAbortReq();
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
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

        // TCP.DISCONNECTreq
        this.channel.cleanup();
        this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
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

        if (!this.channel.isLocalPeerAbort())
        {
            // TCP.ABORTreq
            this.channel.tcpAbortReq();
            this.channel.stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
            // -> S0
            this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
        }
        else
        {
            // -> S3
            this.channel.setChannelState(new EE_APIPX_PeerAbortingState(this.channel));
        }
    }

    @Override
    public void hlResetReq()
    {
        // N/A
    }

    @Override
    public void tcpAbortInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpAbortInd invoked");
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
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("cpaTimeout invoked");
        }

        this.channel.logError(EE_LogMsg.TMLCLOSETIMEOUT.getCode());

        // TCP.ABORTreq
        this.channel.tcpAbortReq();
        // -> S0
        this.channel.setChannelState(new EE_APIPX_ClosedState(this.channel));
    }

    @Override
    public void manageBadFormMsg()
    {
        // Nothing to do here
    }

    @Override
    public String toString()
    {
        return "Closing State";
    }
}
