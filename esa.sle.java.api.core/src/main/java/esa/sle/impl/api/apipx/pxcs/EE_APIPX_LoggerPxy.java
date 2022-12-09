/**
 * @(#) EE_APIPX_LoggerPxy.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_Listener;
import esa.sle.impl.eapi.dfl.idfl.ESLE_DefaultLogger;
import esa.sle.impl.ifs.gen.EE_MessageRepository;

/**
 * The class transfers any message for reporting and tracing to a specific
 * application process. If no route to the client process (via a link object)
 * can be found, the message is transferred to a client registered for the
 * reception of default messages. If no client for default message is
 * registered, the message is discarded. The class also holds all relevant
 * information for trace control, which is received from the link object.
 */
public class EE_APIPX_LoggerPxy extends EE_APIPX_LinkAdapter implements ESLE_DefaultLogger, ISLE_Trace,
                                                            ISLE_TraceControl, ISLE_Reporter
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_LoggerPxy.class.getName());

    /**
     * Trace level.
     */
    private SLE_TraceLevel traceLevel;

    /**
     * Indicates if the traces are started or not.
     */
    private boolean traceStarted;

    /**
     * Indicates if it is a normal close of the IPC link or not.
     */
    private boolean normalStop;

    private EE_APIPX_Link eeAPIPXLink;

    private final String instanceId;

    public EE_APIPX_LoggerPxy(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.traceStarted = false;
        this.normalStop = false;
        this.eeAPIPXLink = null;
    }

    /**
     * The link object calls this function when some data are received on the
     * IPC link. The startTrace, stopTrace and setReporter messages are
     * received, and the local attributes traceLevel, traceStarted, and
     * reporterSet are correctly set.
     */
    @Override
    public void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean last_pdu)
    {
        PXCS_TraceReporter_Mess mess = null;
        HRESULT res = HRESULT.E_FAIL;

        if (dataType == PXCS_MessId.mid_StartTrace.getCode())
        {
            mess = new PXCS_TraceReporter_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            // get the trace reference
            ISLE_Trace pTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
            if (pTrace != null)
            {
                try
                {
                    startTrace(pTrace, mess.getLevel(), false);
                    res = HRESULT.S_OK;
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    res = e.getHResult();
                }

                // send the result
                sendResultMessage(PXCS_MessId.mid_Rsp_StartTrace.getCode(), res, 0, this.eeAPIPXLink);
            }
        }
        else if (dataType == PXCS_MessId.mid_StopTrace.getCode())
        {
            try
            {
                stopTrace();
                res = HRESULT.S_OK;
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
                res = e.getHResult();
            }

            // send the result
            sendResultMessage(PXCS_MessId.mid_Rsp_StopTrace.getCode(), res, 0, this.eeAPIPXLink);
        }
        else if (dataType == PXCS_MessId.mid_NormalStop.getCode())
        {
            this.normalStop = true;
            // send the result
            sendResultMessage(PXCS_MessId.mid_Rsp_NormalStop.getCode(), HRESULT.S_OK, -1, this.eeAPIPXLink);
        }
    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost. The attributes traceLevel and reporterSet are set to false.
     */
    @Override
    public void ipcClosed(EE_APIPX_Link pLink)
    {
        EE_APIPX_Binder pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        if (!this.normalStop && !this.linkClosed)
        {
            this.linkClosed = true;
            ISLE_SII psii = null;
            SLE_PeerAbortDiagnostic diag = SLE_PeerAbortDiagnostic.slePAD_communicationsFailure;

            // the ipc link to the client is broken. Notify the default logger
            if (this.eeAPIPXLink != null)
            {
                // get the sii from the link
                psii = pBinder.getSii(this.eeAPIPXLink);
            }

            ISLE_Reporter pIsleReporter = EE_APIPX_ReportTrace.getReporterInterface(this.instanceId);
            if (pIsleReporter != null)
            {
                String mess = EE_MessageRepository.getMessage(1003, diag.toString());
                pIsleReporter.logRecord(SLE_Component.sleCP_proxy, psii, SLE_LogMessageType.sleLM_alarm, 1003, mess);
                pIsleReporter.notify(SLE_Alarm.sleAL_commsFailure, SLE_Component.sleCP_proxy, psii, 1003, mess);
            }

            this.traceStarted = false;
            this.linkClosed = true;
            this.eeAPIPXLink = null;
        }
    }

    /**
     * Set the Link associated with the BinderAdapter object.
     */
    public void setLink(EE_APIPX_Link pLink)
    {
        this.eeAPIPXLink = pLink;
    }

    /**
     * Checks if the trace level given as parameter is compatible with the
     * attribute traceLevel set by the StartTrace() method.
     */
    private boolean checkTraceLevel(SLE_TraceLevel traceLevel)
    {
        boolean res = false;
        if (!this.traceStarted)
        {
            res = false;
        }
        else
        {
            if (this.traceLevel.getCode() >= traceLevel.getCode())
            {
                res = true;
            }
            else
            {
                res = false;
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == ISLE_Trace.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Reporter.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void logRecord(SLE_Component component, ISLE_SII sii, SLE_LogMessageType type, long messageId, String message)
    {
        PXCS_TraceReporter_Mess trMess = new PXCS_TraceReporter_Mess();
        trMess.setMessType(type);
        trMess.setMessId(messageId);
        trMess.setComponent(component);
        trMess.setText(message);
        if (sii != null)
        {
            trMess.setSii(sii.getAsciiForm());
        }
        byte[] trMessByteArray = trMess.toByteArray();

        PXCS_Header_Mess hMess = new PXCS_Header_Mess(false,
                                                      PXCS_MessId.mid_LogRecord.getCode(),
                                                      trMessByteArray.length);

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + trMessByteArray.length];
        System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(trMessByteArray, 0, data, PXCS_Header_Mess.hMsgLength, trMessByteArray.length);

        sendMessage(data, this.eeAPIPXLink, 0);
    }

    @Override
    public void notify(SLE_Alarm alarm, SLE_Component component, ISLE_SII sii, long messageId, String message)
    {
        PXCS_TraceReporter_Mess trMess = new PXCS_TraceReporter_Mess();
        trMess.setAlarm(alarm);
        trMess.setMessId(messageId);
        trMess.setComponent(component);
        trMess.setText(message);
        if (sii != null)
        {
            trMess.setSii(sii.getAsciiForm());
        }
        byte[] trMessByteArray = trMess.toByteArray();

        PXCS_Header_Mess hMess = new PXCS_Header_Mess(false, PXCS_MessId.mid_Notify.getCode(), trMessByteArray.length);

        byte[] data = new byte[PXCS_Header_Mess.hMsgLength + trMessByteArray.length];
        System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(trMessByteArray, 0, data, PXCS_Header_Mess.hMsgLength, trMessByteArray.length);

        sendMessage(data, this.eeAPIPXLink, 0);
    }

    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        this.traceStarted = true;
        this.traceLevel = level;

        if (this.eeAPIPXLink != null)
        {
            // start the trace on the channel
            ISLE_TraceControl pTraceControl = this.eeAPIPXLink.getChannelTraceControl();
            if (pTraceControl != null)
            {
                pTraceControl.startTrace(trace, level, forward);
            }

            // check if it is the default logger link
            if (this.eeAPIPXLink.getIsDefaultLogger())
            {
                // set the trace in the listener
                EE_APIPX_Listener pListener = EE_APIPX_Listener.getInstance(this.instanceId);

                pTraceControl = pListener.queryInterface(ISLE_TraceControl.class);
                if (pTraceControl != null)
                {
                    pTraceControl.startTrace(trace, level, forward);
                }

                // set the trace in the binder
                EE_APIPX_Binder pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
                pTraceControl = pBinder.queryInterface(ISLE_TraceControl.class);
                if (pTraceControl != null)
                {
                    pTraceControl.startTrace(trace, level, forward);
                }
            }
        }
    }

    @Override
    public void stopTrace() throws SleApiException
    {
        this.traceStarted = false;
        if (this.eeAPIPXLink != null)
        {
            // stop the trace on the channel
            ISLE_TraceControl pTraceControl = this.eeAPIPXLink.getChannelTraceControl();
            if (pTraceControl != null)
            {
                pTraceControl.stopTrace();
            }

            // stop the trace on the PDU translator
            pTraceControl = this.eeAPIPXLink.getTranslatorTraceControl();
            if (pTraceControl != null)
            {
                pTraceControl.stopTrace();
            }

            // check if it is the default logger link
            if (this.eeAPIPXLink.getIsDefaultLogger())
            {
                // stop the trace in the listener
                EE_APIPX_Listener pListener = EE_APIPX_Listener.getInstance(this.instanceId);
                pTraceControl = pListener.queryInterface(ISLE_TraceControl.class);
                if (pTraceControl != null)
                {
                    pTraceControl.stopTrace();
                }
            }
        }
    }

    @Override
    public void traceRecord(SLE_TraceLevel level, SLE_Component component, ISLE_SII psii, String text)
    {
        if (checkTraceLevel(level))
        {
            PXCS_TraceReporter_Mess trMess = new PXCS_TraceReporter_Mess();
            trMess.setLevel(level);
            trMess.setComponent(component);
            trMess.setText(text);
            if (psii != null)
            {
                trMess.setSii(psii.getAsciiForm());
            }
            byte[] trMessByteArray = trMess.toByteArray();

            PXCS_Header_Mess hMess = new PXCS_Header_Mess(false,
                                                          PXCS_MessId.mid_TraceRecord.getCode(),
                                                          trMessByteArray.length);

            byte[] data = new byte[PXCS_Header_Mess.hMsgLength + trMessByteArray.length];
            System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
            System.arraycopy(trMessByteArray, 0, data, PXCS_Header_Mess.hMsgLength, trMessByteArray.length);

            sendMessage(data, this.eeAPIPXLink, 0);
        }
    }

    @Override
    public void setReporter(ISLE_Reporter preporter)
    {
        // should not be used in loggerpxy
    }

    @Override
    public void connect(String ipcAddress) throws SleApiException
    {
        // should not be used in LoggerPxy
        throw new SleApiException(HRESULT.E_FAIL);
    }

    @Override
    public void disconnect() throws SleApiException
    {
        // should not be used in LoggerPxy
        throw new SleApiException(HRESULT.E_FAIL);
    }

}
