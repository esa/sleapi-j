package ccsds.sle.api.isrv.iraf;

import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * This interface provides method to control the internal transfer buffer of the
 * service instance.
 */
public interface IRAF_SITransferBufferControl extends IUnknown
{
    /**
     * Forces the sending of the transfer buffer to the application, regardless
     * of the buffer size and of the latency limit.
     */
    public void sendBufferTransfer(boolean withNotification);

}
