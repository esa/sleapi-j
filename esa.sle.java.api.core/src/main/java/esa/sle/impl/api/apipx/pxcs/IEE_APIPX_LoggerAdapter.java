/**
 * @(#) IEE_APIPX_LoggerAdapter.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxcs.local.EE_APIPX_LocalLink;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;

/**
 * The class builds the link between the link object (class EE_APIPX_Link) and
 * the recipient of the reporting and tracing messages. The class encodes the
 * messages before they are passed to the link object and decodes messages
 * received from the link object and passes them on to the reporter/trace
 * interface. Objects of this class either reside in the SLE application process
 * (proxy component) or in a process that is registered for the reception of
 * default reporting and tracing messages.
 */
public class IEE_APIPX_LoggerAdapter extends EE_APIPX_LinkAdapter
{
    private static final Logger LOG = Logger.getLogger(IEE_APIPX_LoggerAdapter.class.getName());

    /**
     * Pointer to the trace interface.
     */
    private ISLE_Trace pTrace;

    /**
     * Pointer to the reporter interface.
     */
    private ISLE_Reporter pReporter;

    /**
     * Set when the IPC link is connected.
     */
    private boolean isConnected;

    /**
     * Result of the startTrace or stopTrace method.
     */
    private HRESULT result;

    /**
     * Indicates if the Logger Adapter object is used for the default logger.
     */
    private boolean isForDefaultLogger;

    /**
     * The link
     */
    private EE_APIPX_Link eeLink = null;

    /**
     * The instance id.
     */
    protected final String instanceId;
    
    public IEE_APIPX_LoggerAdapter(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.pTrace = null;
        this.pReporter = null;
        this.isConnected = false;
        this.result = HRESULT.E_FAIL;
        this.isForDefaultLogger = true;
        this.eeLink = null;
    }

    public boolean getIsConnected()
    {
        return this.isConnected;
    }

    private void setIsConnected(boolean isConnected)
    {
        this.isConnected = isConnected;
    }

    /**
     * Sets the pointer to the reporter interface that must be used for
     * reporting.
     */
    public void setReporter(final ISLE_Reporter preporter)
    {
        this.pReporter = preporter;
    }

    /**
     * Disconnects a link to the communication server process.
     */
    public HRESULT disconnect()
    {
        if (this.eeLink != null)
        {
            this.eeLink.disconnect();
        }
        setIsConnected(false);
        return HRESULT.S_OK;
    }

    /**
     * Connects a link to the communication server process.
     */
    public HRESULT connect(String ipcAddress)
    {
        if (this.eeLink == null)
        {
            if (EE_APIPX_LocalLink.isLocalAddress(ipcAddress))
            {
                this.eeLink = new EE_APIPX_LocalLink(this.instanceId);
            }
            else
            {
                this.eeLink = new EE_APIPX_Link(this.instanceId);
            }
            this.eeLink.setLoggerAdapter(this);
        }
        if (this.eeLink.connect(ipcAddress) == HRESULT.S_OK)
        {
            if (this.eeLink.waitMsg() == HRESULT.S_OK)
            {
                setIsConnected(true);
                return HRESULT.S_OK;
            }
            else
            {
                this.eeLink.disconnect();
            }
        }
        else
        {
            this.eeLink = null;
        }
        return HRESULT.E_FAIL;
    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost.
     */
    @Override
    public void ipcClosed(EE_APIPX_Link pLink)
    {
        if (this.eeLink != null)
        {
            this.eeLink = null;
        }

        setIsConnected(false);
        signalResponseReceived();
    }

    /**
     * Set the LoggerAdapter associated with the link object.
     */
    public void setLink(EE_APIPX_Link pLink)
    {
        this.eeLink = pLink;
    }

    public boolean getIsDefaultLogger()
    {
        return this.isForDefaultLogger;
    }

    /**
     * Sets the attribute isDefaultLogger.
     */
    public void setIsDefaultLogger(boolean isDefaultLogger)
    {
        this.isForDefaultLogger = isDefaultLogger;
    }

    public void startTrace(SLE_TraceLevel level, ISLE_Trace ptrace, boolean waitResult) throws SleApiException
    {
        if (this.eeLink == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        this.pTrace = ptrace;

        PXCS_TraceReporter_Mess mess = new PXCS_TraceReporter_Mess();
        mess.setLevel(level);
        byte[] messByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false,
                                                       PXCS_MessId.mid_StartTrace.getCode(),
                                                       messByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(messByteArray, 0, data, PXCS_Header_Mess.hMsgLength, messByteArray.length);

        if (!waitResult)
        {
            this.result = HRESULT.S_OK;
            sendMessage(data, this.eeLink, 0);
        }
        else
        {
            this.result = HRESULT.E_FAIL;
            sendMessage(data, this.eeLink, 5);

            if (this.result != HRESULT.S_OK)
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

    }

    public void stopTrace() throws SleApiException
    {
        if (this.eeLink == null)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        this.pTrace = null;

        PXCS_TraceReporter_Mess mess = new PXCS_TraceReporter_Mess();
        byte[] messByteArray = mess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(mess.toString());
        }

        PXCS_Header_Mess header = new PXCS_Header_Mess(false, PXCS_MessId.mid_StopTrace.getCode(), messByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + messByteArray.length];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        this.result = HRESULT.E_FAIL;
        sendMessage(data, this.eeLink, 3);
        if (this.result != HRESULT.S_OK)
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }
    }

    /**
     * The link object calls this function when some data are received on the
     * IPC link. The traces and reporter messages are received, decoded, and
     * given to the appropriate interface (ISLE_Reporter or ISLE_Trace).
     */
    @Override
    public void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean last_pdu)
    {
        if (dataType == PXCS_MessId.mid_Rsp_StartTrace.getCode() || dataType == PXCS_MessId.mid_Rsp_StopTrace.getCode())
        {
            PXCS_Response_Mess mess = new PXCS_Response_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            // give the result
            this.result = mess.getResult();

            signalResponseReceived();
        }
        else if (dataType == PXCS_MessId.mid_LogRecord.getCode() || dataType == PXCS_MessId.mid_Notify.getCode()
                 || dataType == PXCS_MessId.mid_TraceRecord.getCode())
        {
            if (dataType == PXCS_MessId.mid_TraceRecord.getCode() && this.pTrace == null)
            {
                return;
            }

            if (dataType == PXCS_MessId.mid_LogRecord.getCode() || dataType == PXCS_MessId.mid_Notify.getCode()
                && this.pReporter == null)
            {
                return;
            }

            PXCS_TraceReporter_Mess mess = new PXCS_TraceReporter_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            String text = "";
            String sii = "";
            ISLE_SII psii = null;

            if (!mess.getText().isEmpty())
            {
                text = mess.getText();
            }

            if (!mess.getSii().isEmpty())
            {
                sii = mess.getSii();
                // create the Service Instance identifier
                ISLE_UtilFactory puf = EE_SLE_UtilityFactory.getInstance(this.instanceId);
                if (puf != null)
                {
                    try
                    {
                        psii = puf.createSII(ISLE_SII.class);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }

                    if (psii != null)
                    {
                        try
                        {
                            psii.setAsciiForm(sii);
                        }
                        catch (SleApiException e)
                        {
                            LOG.log(Level.FINE, "SleApiException ", e);
                        }
                    }
                }
            }

            if (dataType == PXCS_MessId.mid_TraceRecord.getCode())
            {
                this.pTrace.traceRecord(mess.getLevel(), mess.getComponent(), psii, text);
            }
            else if (dataType == PXCS_MessId.mid_LogRecord.getCode())
            {
                this.pReporter.logRecord(mess.getComponent(), psii, mess.getMessType(), mess.getMessId(), text);
            }
            else
            {
                this.pReporter.notify(mess.getAlarm(), mess.getComponent(), psii, mess.getMessId(), text);
            }
        }
    }

    /**
     * Called from the default logger.
     * 
     * @param level
     * @param ptrace2
     * @return
     */
    public void startTrace(SLE_TraceLevel level, ISLE_Trace ptrace2) throws SleApiException
    {
        startTrace(level, ptrace2, true);
    }
}
