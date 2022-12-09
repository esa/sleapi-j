package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.sle.impl.api.apipx.pxdb.EE_APIPX_PortData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ResponderPort;
import esa.sle.impl.ifs.gen.EE_LogMsg;

public class EE_APIPX_SocketConnectionMng extends Thread
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_SocketConnectionMng.class.getName());

    private final EE_APIPX_InitiatingChannel channel;

    private final EE_APIPX_ResponderPort port;

    private final String portID;

    private final int index;

    private Socket socket;

	private final int connectTimeout;


    public EE_APIPX_SocketConnectionMng(EE_APIPX_InitiatingChannel channel,
                                        EE_APIPX_ResponderPort port,
                                        String portID,
                                        int index,
                                        int connectTimeout /*SLEAPIJ-16*/)
    {
        this.channel = channel;
        this.port = port;
        this.portID = portID;
        this.index = index;
        this.socket = new Socket();
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void run()
    {
        EE_APIPX_PortData portData = this.port.getPortData(this.index);

        try
        {
            // connect the socket
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Connecting to socket address:" + portData.getTcpIPAddress().toString() + " port: "
                           + portData.getTcpPortNumber());

            }

            this.socket.setOOBInline(true);
            this.socket.connect(new InetSocketAddress(portData.getTcpIPAddress(), portData.getTcpPortNumber()), this.connectTimeout);
        }
        catch (IOException | IllegalArgumentException e)
        {
            try
            {
                // close the socket
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Closing socket " + portData.getTcpIPAddress().toString() + ":"
                               + portData.getTcpPortNumber());
                }

                this.socket.close();
            }
            catch (IOException e1)
            {
                String mess1 = "Failure while closing the socket" + portData.getTcpIPAddress().toString() + ":"
                               + portData.getTcpPortNumber();
                this.channel.logError(EE_LogMsg.TMLCONNECTFAIL.getCode(), true, this.portID, mess1);
                return;
            }
            return;
        }

        // notify the channel
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Connected to socket address:" + portData.getTcpIPAddress().toString() + " port: "
                       + portData.getTcpPortNumber());
        }

        this.channel.connectionSucceeded(this);
    }

    public EE_APIPX_PortData getPortData()
    {
        return this.port.getPortData(this.index);
    }

    public Socket getSocket()
    {
        return this.socket;
    }

    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }
}
