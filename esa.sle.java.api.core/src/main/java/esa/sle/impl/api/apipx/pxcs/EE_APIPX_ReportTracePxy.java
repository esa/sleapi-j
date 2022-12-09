/**
 * @(#) EE_APIPX_ReportTracePxy.java
 */

package esa.sle.impl.api.apipx.pxcs;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The class implements the interfaces ISLE_Trace and ISLE_Reporter and
 * transfers any message for reporting and tracing to a specific application
 * process. If no route to the client process (via a link object) can be found,
 * the message is transferred to a client registered for the reception of
 * default messages. If no client for default message is registered, the message
 * is discarded.
 */
public class EE_APIPX_ReportTracePxy implements ISLE_Trace, ISLE_Reporter
{
    /**
     * Reference to the default logger link.
     */
    private EE_APIPX_Link dflLink;

    /**
     * The pointer to the local default logger interface.
     */
    private ISLE_Reporter localDefaultLogger;

    /**
     * The pointer to the local trace interface.
     */
    private ISLE_Trace localTrace;

    /**
     * The selected trace level.
     */
    private SLE_TraceLevel traceLevel;

    private final String instanceId;

    public EE_APIPX_ReportTracePxy(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.dflLink = null;
        this.localDefaultLogger = null;
        this.localTrace = null;
        setTraceLevel(SLE_TraceLevel.sleTL_low);
    }

    public void setDflLink(EE_APIPX_Link dflLink)
    {
        this.dflLink = dflLink;
    }

    private EE_APIPX_LoggerPxy getLoggerPxy(ISLE_SII psii)
    {
        EE_APIPX_Link pLink = null;
        EE_APIPX_Binder pBinder = null;
        EE_APIPX_LoggerPxy pLoggerPxy = null;

        // get the logger pxy
        pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        if (pBinder != null && psii != null)
        {
            pLink = pBinder.getLink(psii);
        }

        // if no link, try the default logger
        if (pLink == null || pLink.isClosed())
        {
            pLink = this.dflLink;
        }

        if (pLink != null && !pLink.isClosed())
        {
            pLoggerPxy = pLink.getLoggerPxy();
        }

        return pLoggerPxy;
    }

    /**
     * Sets the reference to the local ISLE_Reporter interface for default
     * logging. This is needed if the Communication Server is used as a library
     * in an application.
     */
    public void setLocalDefaultReporter(ISLE_Reporter pReporter)
    {
        if (this.localDefaultLogger != null)
        {
            this.localDefaultLogger = null;
        }

        if (pReporter != null)
        {
            this.localDefaultLogger = pReporter;
        }
    }

    /**
     * Sets the reference to the local ISLE_Trace interface. This is needed if
     * the Communication Server is used as a library in an application.
     */
    public void setLocalTrace(ISLE_Trace pTrace, SLE_TraceLevel traceLevel)
    {
        if (this.localTrace != null)
        {
            this.localTrace = null;
        }

        if (pTrace != null)
        {
            this.localTrace = pTrace;
            setTraceLevel(traceLevel);
        }
    }

    /**
     * Returns true if the SI with the supplied ID is connected.
     */
    public boolean isSIconnected(ISLE_SII siid)
    {
        if (siid == null)
        {
            return false;
        }

        EE_APIPX_Binder pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        if (pBinder == null)
        {
            return false;
        }

        EE_APIPX_Link pLink = pBinder.getLink(siid);
        if (pLink == null)
        {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == ISLE_Trace.class)
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
        // check if we have a local default logger
        // and use it in case no SI is related
        if (this.localDefaultLogger != null)
        {
            if (!isSIconnected(sii))
            {
                // no SI related, therefore default logging needed:
                this.localDefaultLogger.logRecord(component, sii, type, messageId, message);
                return;
            }
        }

        // otherwise: normal log processing:
        EE_APIPX_LoggerPxy pLoggerPxy = getLoggerPxy(sii);
        if (pLoggerPxy == null)
        {
            return;
        }

        ISLE_Reporter pIsleReporter = pLoggerPxy.queryInterface(ISLE_Reporter.class);
        if (pIsleReporter != null)
        {
            pIsleReporter.logRecord(component, sii, type, messageId, message);
        }
    }

    @Override
    public void notify(SLE_Alarm alarm, SLE_Component component, ISLE_SII sii, long messageId, String message)
    {
        // check if we have a local default logger
        // and use it in case no SI is related
        if (this.localDefaultLogger != null)
        {
            if (!isSIconnected(sii))
            {
                // no SI related, therefore default logging needed:
                this.localDefaultLogger.notify(alarm, component, sii, messageId, message);
                return;
            }
        }

        // otherwise: normal log processing:
        EE_APIPX_LoggerPxy pLoggerPxy = getLoggerPxy(sii);
        if (pLoggerPxy == null)
        {
            return;
        }

        ISLE_Reporter pIsleReporter = pLoggerPxy.queryInterface(ISLE_Reporter.class);
        if (pIsleReporter != null)
        {
            pIsleReporter.notify(alarm, component, sii, messageId, message);
        }
    }

    @Override
    public void traceRecord(SLE_TraceLevel level, SLE_Component component, ISLE_SII psii, String text)
    {
        // check if we have a local tracing
        // and use it in case no SI is related
        if (this.localTrace != null)
        {
            if (!isSIconnected(psii))
            {
                // no SI related, therefore default logging needed:
                this.localTrace.traceRecord(level, component, psii, text);
                return;
            }
        }

        // otherwise: normal trace processing:
        EE_APIPX_LoggerPxy pLoggerProxy = getLoggerPxy(psii);
        if (pLoggerProxy == null)
        {
            return;
        }

        ISLE_Trace pIsleTrace = pLoggerProxy.queryInterface(ISLE_Trace.class);
        if (pIsleTrace != null)
        {
            pIsleTrace.traceRecord(level, component, psii, text);
        }
    }

    public SLE_TraceLevel getTraceLevel()
    {
        return this.traceLevel;
    }

    public void setTraceLevel(SLE_TraceLevel traceLevel)
    {
        this.traceLevel = traceLevel;
    }
}
