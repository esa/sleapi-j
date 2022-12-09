package ccsds.sle.api.isrv.ircf;

import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * Service Instance Transfer Buffer Control This interface provides method to
 * control the internal transfer buffer of the service instance.
 */
public interface IRCF_SITransferBufferControl extends IUnknown
{
    /**
     * Forces the sending of the transfer buffer to the application, regardless
     * of the buffer size and of the latency limit. A kind of flush buffer.
     */
    void sendBufferTransfer(boolean withNotification);

}
