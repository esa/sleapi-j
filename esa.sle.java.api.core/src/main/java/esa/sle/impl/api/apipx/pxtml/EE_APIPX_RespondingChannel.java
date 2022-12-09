/**
 * @(#) EE_APIPX_RespondingChannel.java
 */

package esa.sle.impl.api.apipx.pxtml;

import java.net.ServerSocket;
import java.net.Socket;

import esa.sle.impl.api.apipx.pxdb.EE_APIPX_TMLData;
import esa.sle.impl.ifs.time.EE_Duration;

/**
 * This class extends the EE_APIPX_Channel class by providing event processing
 * for the Closing and Establishing states.
 */
public class EE_APIPX_RespondingChannel extends EE_APIPX_Channel
{
    public EE_APIPX_RespondingChannel()
    {
        super();
    }

    @Override
    public void initialise(Socket sock, ServerSocket sSock)
    {
        setConnectedSocket(sock, sSock, 0, 0);

        // read and start the TMS timer
        EE_APIPX_TMLData ptmp = this.db.getTMLData();
        int duration = ptmp.getStartupTimer();
        this.tmsDuration = new EE_Duration(duration);

        super.setChannelState(new EE_APIPX_StartingState(this));
    }
}
