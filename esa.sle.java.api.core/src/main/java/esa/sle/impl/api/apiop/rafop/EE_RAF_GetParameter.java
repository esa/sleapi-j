/**
 * @(#) EE_RAF_GetParameter.java
 */

package esa.sle.impl.api.apiop.rafop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_GetParameterDiagnostic;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;

/**
 * The class implements the RAF specific GetParameter operation.
 */
public class EE_RAF_GetParameter extends IEE_SLE_ConfirmedOperation implements IRAF_GetParameter
{
    /**
     * The requested parameter.
     */
    private RAF_ParameterName requestedParam = RAF_ParameterName.rafPN_invalid;

    /**
     * The returned parameter.
     */
    private RAF_ParameterName returnedParam = RAF_ParameterName.rafPN_invalid;

    /**
     * The delivery mode.
     */
    private RAF_DeliveryMode deliveryMode = RAF_DeliveryMode.rafDM_invalid;

    /**
     * The requested frame quality.
     */
    private RAF_ParFrameQuality requestedFrameQual = RAF_ParFrameQuality.rafPQ_invalid;

    /**
     * The latency limit.
     */
    private int latencyLimit = 0;

    /**
     * The transfer buffer size.
     */
    private long transferBufferSize = 0;

    /**
     * The reporting cycle.
     */
    private long reportingCycle = 0;

    /**
     * the return timeout period.
     */
    private long returnTimeoutPeriod = 0;

    /**
     * The GetParameter diagnostic.
     */
    private RAF_GetParameterDiagnostic getParameterDiagnostic = RAF_GetParameterDiagnostic.rafGP_invalid;


    private long minReportingCycle = 0;
    
    private List<RAF_RequestedFrameQuality> permittedFrameQualitySet = null;
    
    /**
     * @FunctionCreator of the RAf GetParameter Operation.@EndFunction
     */
    private EE_RAF_GetParameter(final EE_RAF_GetParameter right)
    {
        super(right);
        this.requestedParam = right.requestedParam;
        this.returnedParam = right.returnedParam;
        this.deliveryMode = right.deliveryMode;
        this.requestedFrameQual = right.requestedFrameQual;
        this.latencyLimit = right.latencyLimit;
        this.transferBufferSize = right.transferBufferSize;
        this.reportingCycle = right.reportingCycle;
        this.returnTimeoutPeriod = right.returnTimeoutPeriod;
        this.getParameterDiagnostic = right.getParameterDiagnostic;
        this.minReportingCycle = right.minReportingCycle;
        this.permittedFrameQualitySet = right.permittedFrameQualitySet;
    }

    public EE_RAF_GetParameter(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, SLE_OpType.sleOT_getParameter, version, preporter);
        this.requestedParam = RAF_ParameterName.rafPN_invalid;
        this.returnedParam = RAF_ParameterName.rafPN_invalid;
        this.deliveryMode = RAF_DeliveryMode.rafDM_invalid;
        this.requestedFrameQual = RAF_ParFrameQuality.rafPQ_invalid;
        this.latencyLimit = 0;
        this.transferBufferSize = 0;
        this.reportingCycle = 0;
        this.returnTimeoutPeriod = 0;
        this.getParameterDiagnostic = RAF_GetParameterDiagnostic.rafGP_invalid;
        this.minReportingCycle = 0;
        this.permittedFrameQualitySet = null;
    }

    @Override
    public synchronized RAF_ParameterName getRequestedParameter()
    {
        return this.requestedParam;
    }

    @Override
    public synchronized RAF_ParameterName getReturnedParameter()
    {
        return this.returnedParam;
    }

    @Override
    public synchronized RAF_DeliveryMode getDeliveryMode()
    {
        assert (this.returnedParam == RAF_ParameterName.rafPN_deliveryMode) : "invalid getXXX call";
        return this.deliveryMode;
    }

    @Override
    public synchronized RAF_ParFrameQuality getRequestedFrameQuality()
    {
        assert (this.returnedParam == RAF_ParameterName.rafPN_requestFrameQuality) : "invalid getXXX call";
        return this.requestedFrameQual;
    }

    @Override
    public synchronized int getLatencyLimit()
    {
        assert (this.returnedParam == RAF_ParameterName.rafPN_latencyLimit) : "invalid getXXX call";
        return this.latencyLimit;
    }

    @Override
    public synchronized long getTransferBufferSize()
    {
        assert (this.returnedParam == RAF_ParameterName.rafPN_bufferSize) : "invalid getXXX call";
        return this.transferBufferSize;
    }

    @Override
    public synchronized long getReportingCycle()
    {
        assert (this.returnedParam == RAF_ParameterName.rafPN_reportingCycle) : "invalid getXXX call";
        return this.reportingCycle;
    }

    @Override
    public synchronized long getReturnTimeoutPeriod()
    {
        assert (this.returnedParam == RAF_ParameterName.rafPN_returnTimeoutPeriod) : "invalid get call";
        return this.returnTimeoutPeriod;
    }

    @Override
    public synchronized RAF_GetParameterDiagnostic getGetParameterDiagnostic()
    {
        assert (((getResult() == SLE_Result.sleRES_negative) && (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics))) : "invalid getXXX call";
        return this.getParameterDiagnostic;
    }
    
    @Override
    public synchronized List<RAF_RequestedFrameQuality> getPermittedFrameQuality()
    {
    	assert (this.returnedParam == RAF_ParameterName.rafPN_permittedFrameQuality) : "invalid getXXX call";
    	return this.permittedFrameQualitySet;
    }
    
    @Override
    public synchronized long getMinimumReportingCycle()
    {
    	assert (this.returnedParam == RAF_ParameterName.rafPN_minReportingCycle) : "invalid getXXX call";
    	return this.minReportingCycle;
    }

    @Override
    public synchronized void setRequestedParameter(RAF_ParameterName name)
    {
        this.requestedParam = name;
    }

    @Override
    public synchronized void setDeliveryMode(RAF_DeliveryMode mode)
    {
        this.returnedParam = RAF_ParameterName.rafPN_deliveryMode;
        this.deliveryMode = mode;
    }

    @Override
    public synchronized void setRequestedFrameQuality(RAF_ParFrameQuality quality)
    {
        this.returnedParam = RAF_ParameterName.rafPN_requestFrameQuality;
        this.requestedFrameQual = quality;
    }

    @Override
    public synchronized void setLatencyLimit(int limit)
    {
        this.returnedParam = RAF_ParameterName.rafPN_latencyLimit;
        this.latencyLimit = limit;
    }

    @Override
    public synchronized void setTransferBufferSize(long size)
    {
        this.returnedParam = RAF_ParameterName.rafPN_bufferSize;
        this.transferBufferSize = size;
    }

    @Override
    public synchronized void setReportingCycle(long cycle)
    {
        this.returnedParam = RAF_ParameterName.rafPN_reportingCycle;
        this.reportingCycle = cycle;
    }

    @Override
    public synchronized void setReturnTimeoutPeriod(long period)
    {
        this.returnedParam = RAF_ParameterName.rafPN_returnTimeoutPeriod;
        this.returnTimeoutPeriod = period;
    }

    @Override
    public synchronized void setGetParameterDiagnostic(RAF_GetParameterDiagnostic diagostic)
    {
        this.getParameterDiagnostic = diagostic;
        setSpecificDiagnostics();
    }
    @Override
    public void setMinimumReportingCycle(long mrc)
    {
    	this.returnedParam = RAF_ParameterName.rafPN_minReportingCycle;
    	this.minReportingCycle = mrc;
    }
    
    @Override
    public void setPermittedFrameQuality(RAF_ParFrameQuality[] permFrameQuality)
    {
    	this.returnedParam = RAF_ParameterName.rafPN_permittedFrameQuality;
    	if(this.permittedFrameQualitySet == null)
    	{
    		this.permittedFrameQualitySet = new ArrayList<RAF_RequestedFrameQuality>();
    	}
    	for(RAF_ParFrameQuality qual : permFrameQuality)
    	{
    		if(qual.getCode() == RAF_RequestedFrameQuality.rafRQ_goodFramesOnly.getCode())
    		{
    			this.permittedFrameQualitySet.add(RAF_RequestedFrameQuality.rafRQ_goodFramesOnly);
    		}
    		else if(qual.getCode() == RAF_RequestedFrameQuality.rafRQ_erredFramesOnly.getCode())
    		{
    			this.permittedFrameQualitySet.add(RAF_RequestedFrameQuality.rafRQ_erredFramesOnly);
    		}
    		else if(qual.getCode() == RAF_RequestedFrameQuality.rafRQ_allFrames.getCode())
    		{
    			this.permittedFrameQualitySet.add(RAF_RequestedFrameQuality.rafRQ_allFrames);
    		}
    		else
			{
    			this.permittedFrameQualitySet.add(RAF_RequestedFrameQuality.rafRQ_invalid);
			}
    	}
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.requestedParam == RAF_ParameterName.rafPN_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();
        if (this.returnedParam != this.requestedParam)
        {
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }
        switch (this.returnedParam)
        {
        case rafPN_bufferSize:
            if (this.transferBufferSize == 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rafPN_deliveryMode:
            if (this.deliveryMode == RAF_DeliveryMode.rafDM_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rafPN_latencyLimit:
        case rafPN_reportingCycle:
            break;
        case rafPN_returnTimeoutPeriod:
            if (this.returnTimeoutPeriod <= 0)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
            break;
        case rafPN_permittedFrameQuality:
        	if(this.permittedFrameQualitySet == null)
        	{
        		throw new SleApiException(HRESULT.SLE_E_UNKNOWN);
        	}
        	break;
        case rafPN_minReportingCycle:
        	if(this.minReportingCycle < 1 || this.minReportingCycle > 600)
        	{
        		throw new SleApiException(HRESULT.SLE_E_RANGE);
        	}
        	break;
        case rafPN_invalid:
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        default:
            break;
        }
        if (getResult() == SLE_Result.sleRES_negative)
        {
            if ((getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
                && (this.getParameterDiagnostic == RAF_GetParameterDiagnostic.rafGP_invalid))
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_RAF_GetParameter pobj = new EE_RAF_GetParameter(this);
        return pobj;
    }

    /**
     * This information is printed out on GetParameter operation
     * on both sides user and provider - not on user input, but rather
     * on code.
     */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);

        oss.append("Requested parameter     : " + this.requestedParam + "\n");
        oss.append("Returned parameter      : " + this.returnedParam + "\n");
        oss.append("Delivery mode           : " + this.deliveryMode + "\n");
        oss.append("Requested frame quality : " + this.requestedFrameQual + "\n");
        oss.append("Latency limit           : " + this.latencyLimit + "\n");
        oss.append("Transfer buffer size    : " + this.transferBufferSize + "\n");
        oss.append("Reporting cycle         : " + this.reportingCycle + "\n");
        oss.append("Return timeout period   : " + this.returnTimeoutPeriod + "\n");
        oss.append("Get parameter diag.     : " + this.getParameterDiagnostic + "\n");
        oss.append("Permitted frame quality : " );
        if (this.permittedFrameQualitySet != null)
        {
            int i;
            for (i = 0; i < this.permittedFrameQualitySet.size(); i++)
            {
                oss.append(this.permittedFrameQualitySet.get(i).getCode() + " ");
            }
        }
        oss.append("\n");
        oss.append("Minimum reporting cycle : " + this.minReportingCycle + "\n");
        String ret = oss.toString();
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == IRAF_GetParameter.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized String toString()
    {
        return "EE_RAF_GetParameter [requestedParam=" + this.requestedParam + ", returnedParam=" + this.returnedParam
               + ", deliveryMode=" + this.deliveryMode + ", requestedFrameQual=" + this.requestedFrameQual
               + ", latencyLimit=" + this.latencyLimit + ", transferBufferSize=" + this.transferBufferSize
               + ", reportingCycle=" + this.reportingCycle + ", returnTimeoutPeriod=" + this.returnTimeoutPeriod
               + ", getParameterDiagnostic=" + this.getParameterDiagnostic 
               + ", permittedFrameQual=" + ((this.permittedFrameQualitySet != null) ? permittedFrameQualitySet.toString(): "")
               + ", minReportingCycle=" + this.minReportingCycle +"]";
    }

}
