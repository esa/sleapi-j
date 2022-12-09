package esa.sle.impl.eapi.dfl;

import java.util.HashMap;
import java.util.Map;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxcs.IEE_APIPX_LoggerAdapter;
import esa.sle.impl.eapi.dfl.idfl.ESLE_DefaultLogger;

/**
 * The class implements the interfaces ISLE_DefaultLogger and ISLE_TraceControl
 * as specified in reference [API-RM] for the component class 'Default Logger'.
 * It receives any reporting and tracing message from the communication server
 * process after registration via an IPC connection and forwards it to the
 * interface supplied by the application. This class is responsible for creation
 * and deletion of the link object.
 */
public class EE_DFL_DefaultLogger implements ISLE_TraceControl, ESLE_DefaultLogger
{
    /**
     * Set whenever StartTrace is called and succeeds.
     */
    private boolean traceStarted = false;

    /**
     * Set whenever SetReporter is called and succeeds.
     */
    private boolean reporterIsSet = false;

    private IEE_APIPX_LoggerAdapter loggerAdapter;

    private static Map<String, EE_DFL_DefaultLogger> instanceMap = new HashMap<>();


    public static synchronized void initialiseInstance(String instanceKey)
    {
    	EE_DFL_DefaultLogger instance = instanceMap.get(instanceKey);
        if (instance == null)
        {
            instance = new EE_DFL_DefaultLogger(instanceKey);
            instanceMap.put(instanceKey, instance);
        }
    }

    public static EE_DFL_DefaultLogger getInstance(String instanceKey)
    {
    	EE_DFL_DefaultLogger instance = instanceMap.get(instanceKey);
        if (instance == null)
        {
            throw new IllegalStateException("The initialise method has never been called and the instance never created for instance " + instanceKey);
        }
        return instance;
    }

    private EE_DFL_DefaultLogger(String instanceKey)
    {
    	this.loggerAdapter = new IEE_APIPX_LoggerAdapter(instanceKey);
        this.loggerAdapter.setIsDefaultLogger(true);
    }

    /**
     * Starts tracing for the default logger, and gives the pointer to the trace
     * interface. CodesS_OK Tracing is started. SLE_E_STATE The tracing is
     * already active. E_FAIL The request fails due to a further unspecified
     * error.
     * 
     * @throws SleApiException
     */
    @Override
    public void startTrace(ISLE_Trace ptrace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        if (!this.loggerAdapter.getIsConnected())
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        // check if already started
        if (this.traceStarted == true)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check the ptrace pointer
        assert ptrace != null : "ptrace is null";
        if (ptrace == null)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        // check the trace level
        assert ((level.getCode() >= SLE_TraceLevel.sleTL_low.getCode()) && (level.getCode() <= SLE_TraceLevel.sleTL_full
                .getCode())) : "Trace level unknown";
        if ((level.getCode() < SLE_TraceLevel.sleTL_low.getCode())
            || ((level.getCode() > SLE_TraceLevel.sleTL_full.getCode())))
        {
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.loggerAdapter.startTrace(level, ptrace);
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        if (rc == HRESULT.S_OK)
        {
            this.traceStarted = true;
        }
        else
        {
            throw new SleApiException(rc);
        }

    }

    /**
     * Stops tracing in the default logger. S_OK Tracing was stopped.
     * SLE_E_STATE Tracing already stopped. E_FAIL The request fails due to a
     * further unspecified error.
     * 
     * @throws SleApiException
     */
    @Override
    public void stopTrace() throws SleApiException
    {
        if (!this.loggerAdapter.getIsConnected())
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }

        // check if already stopped
        if (this.traceStarted == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        HRESULT rc = HRESULT.S_OK;
        try
        {
            this.loggerAdapter.stopTrace();
        }
        catch (SleApiException e)
        {
            rc = e.getHResult();
        }
        if (rc == HRESULT.S_OK)
        {
            this.traceStarted = false;
        }
        else
        {
            throw new SleApiException(rc);
        }
    }

    /**
     * Connects the default logger to the communication server process S_OK The
     * default logger is connected. SLE_E_STATE The object is already connected.
     * SLE_E_CONFIG No reporter interface available. E_FAIL The connection has
     * failed.
     * 
     * @throws SleApiException
     */
    @Override
    public void connect(String ipcAddress) throws SleApiException
    {
        assert (ipcAddress != null) : "ipcAddress is null";
        if (ipcAddress == null)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        // check if a reporter has been set
        if (!getReporterIsSet())
        {
            throw new SleApiException(HRESULT.SLE_E_CONFIG);
        }

        // check if the default logger is already connected
        if (this.loggerAdapter.getIsConnected())
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }
        HRESULT r = HRESULT.S_OK;
        r = this.loggerAdapter.connect(ipcAddress);

        if (r != HRESULT.S_OK)
        {
            throw new SleApiException(r);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (IUnknown.class == iid)
        {
            return (T) this;
        }
        else if (ISLE_TraceControl.class == iid)
        {
            return (T) this;
        }
        else if (ESLE_DefaultLogger.class == iid)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * Disconnects the default logger from the communication server process.
     * CodesS_OK The default logger is disconnected. SLE_E_STATE The default
     * logger is not connected. E_FAIL The disconnection has failed.
     * 
     * @throws SleApiException
     */
    @Override
    public void disconnect() throws SleApiException
    {
        if (!this.loggerAdapter.getIsConnected())
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        HRESULT r = HRESULT.S_OK;
        r = this.loggerAdapter.disconnect();
        if (r != HRESULT.S_OK)
        {
            throw new SleApiException(r);
        }
    }

    /**
     * Set the pointer to the reporter interface. No return code.
     */
    @Override
    public void setReporter(ISLE_Reporter preporter)
    {
        assert (preporter != null) : "preporter is null";
        if (preporter == null)
        {
            return;
        }
        this.loggerAdapter.setReporter(preporter);
        setReporterIsSet(true);
    }

    public boolean getReporterIsSet()
    {
        return this.reporterIsSet;
    }

    public void setReporterIsSet(boolean reporterIsSet)
    {
        this.reporterIsSet = reporterIsSet;
    }

    public boolean getTraceStarted()
    {
        return this.traceStarted;
    }

    public void setTraceStarted(boolean traceStarted)
    {
        this.traceStarted = traceStarted;
    }

}
