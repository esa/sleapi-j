package esa.sle.impl.api.apipx.pxtml;

import java.util.logging.Level;
import java.util.logging.Logger;

import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLTimer;

public class EE_APIPX_ClosedState implements ITMLState
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_ClosedState.class.getName());

    EE_APIPX_Channel channel;


    public EE_APIPX_ClosedState(EE_APIPX_Channel channel)
    {
        this.channel = channel;
    }

    @Override
    public void hlConnectReq(String respPortId)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("hlConnectReq invoked");
        }

        if (this.channel instanceof EE_APIPX_InitiatingChannel)
        {
            // -> S1
            this.channel.setChannelState(new EE_APIPX_StartingState(this.channel));
            ((EE_APIPX_InitiatingChannel) this.channel).tcpConnectReq(respPortId);
        }
        else if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            throw new RuntimeException("the hlConnectReq method is not available for the responding channel!");
        }
    }

    @Override
    public void tcpConnectCnf()
    {
        // N/A
    }

    @Override
    public void tcpConnectInd()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("tcpConnectInd invoked");
        }

        if (this.channel instanceof EE_APIPX_RespondingChannel)
        {
            this.channel.startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
            this.channel.setFirstPDU(false);
            // -> S1
            this.channel.setChannelState(new EE_APIPX_StartingState(this.channel));
        }
        else
        {
            throw new RuntimeException("the tcpConnectInd method is not available for the initiating channel!");
        }
    }

    @Override
    public void tcpDataInd(EE_APIPX_TMLMessage msg)
    {
        // N/A
    }

    @Override
    public void hlDisconnectReq()
    {
        // N/A
    }

    @Override
    public void tcpDisconnectInd()
    {
        // N/A
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
        // N/A
    }

    @Override
    public void hlResetReq()
    {
        // N/A
    }

    @Override
    public void tcpAbortInd()
    {
        // N/A
    }

    @Override
    public void tcpTimeOut()
    {
        // N/A
    }

    @Override
    public void tcpError(int code, boolean traceAlso, String[] param)
    {
        // N/A
    }

    @Override
    public void tmsTimeout()
    {
        // N/A
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
        // Nothing to do here
    }

    @Override
    public String toString()
    {
        return "Closed State";
    }

}
