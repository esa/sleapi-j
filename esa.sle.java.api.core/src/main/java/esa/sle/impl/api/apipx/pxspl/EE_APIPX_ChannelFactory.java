/**
 * @(#) EE_APIPX_ChannelFactory.java
 */

package esa.sle.impl.api.apipx.pxspl;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_ChannelPxy;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_Link;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_InitiatingChannel;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_RespondingChannel;

/**
 * The class creates any kind of channel objects, according to the creation
 * function. The following types of channel objects can be created: - channel
 * objects for initiating associations that transfer/receive data to/from the
 * TCP socket. - channel objects for responding associations that
 * transfer/receive data to/from the communication server process
 * (EE_APIPX_ChannelPxy). - channel objects for responding associations residing
 * in the communication server process, which transfers/receives data to/from
 * the TCP socket. Note that an object of the class EE_APIPX_ChannelFactory is a
 * singleton.
 */
public class EE_APIPX_ChannelFactory
{
    /**
     * Used to create a Channel object in TML, or a ChannelPxy object. For an
     * initiating association, this function is called by the InitiatingAssoc
     * object to create the Channel. For a responding association, this function
     * is called : - by the BinderPxy object when a BIND PDU is received. A
     * ChannelPxy object is created and linked with the Link given as parameter.
     * - by the Listener (TML) to create the Channel object when a new
     * connection is established. This method also creates the Event Monitor if
     * needed.
     */
    public static IEE_ChannelInitiate createChannel(String instanceId, 
    												boolean initiatingChannel,
                                                    ISLE_Reporter pReporter,
                                                    EE_APIPX_Link pLink)
    {
        IEE_ChannelInitiate pChannelInitiate = null;

        if (initiatingChannel)
        {
            // initiating side
            EE_APIPX_InitiatingChannel pChannel = new EE_APIPX_InitiatingChannel();
            pChannelInitiate = pChannel.queryInterface(IEE_ChannelInitiate.class);
        }
        else
        {
            // responding side
            if (pLink == null)
            {
                // create a channel object
                EE_APIPX_RespondingChannel pChannel = new EE_APIPX_RespondingChannel();
                pChannelInitiate = pChannel.queryInterface(IEE_ChannelInitiate.class);
            }
            else
            {
                // create a channel proxy object
                EE_APIPX_ChannelPxy pChannelPxy = new EE_APIPX_ChannelPxy(instanceId, pReporter, pLink);
                pChannelInitiate = pChannelPxy.queryInterface(IEE_ChannelInitiate.class);
                pLink.setChannelPxy(pChannelPxy);
            }
        }
        return pChannelInitiate;
    }
}
