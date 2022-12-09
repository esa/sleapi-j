package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import esa.sle.impl.api.apiut.EE_SLE_SII;
import esa.sle.impl.api.apiut.EE_SLE_Time;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public abstract class CommonParser
{

    private static final Logger LOG = Logger.getLogger(CommonParser.class.getName());

    public final String END_GROUP = "end_group";
    
    public final String VOID = "void"; // SLEAPIJ-12

    private BufferedReader br = null;

    protected Map<String, ValueProcessor> processors = new TreeMap<>();

    private boolean startTime = false;

    private boolean stopTime = false;

    ISLE_Time serviceInstanceStopTime = new EE_SLE_Time();

    ISLE_Time serviceInstanceStartTime = new EE_SLE_Time();


    public CommonParser(BufferedReader br)
    {
        this.br = br;
        this.processors.put(this.END_GROUP, (v, si) -> {
            return true;
        });
        this.processors.put("service-instance-id", this::extractServiceInstanceId);
        this.processors.put("service-instance-start-time", this::extractServiceInstanceStartTime);
        this.processors.put("service-instance-stop-time", this::extractServiceInstanceStopTime);
        this.processors.put("initiator-id", this::extractServiceInstanceInitiatorId);
        this.processors.put("responder-id", this::extractServiceInstanceResponderId);
        this.processors.put("responder-port-id", this::extractServiceInstanceResponderPortId);
        this.processors.put("return-timeout-period", this::extractServiceInstanceReturnTimeOutPeriod);
    }

    private boolean extractServiceInstanceReturnTimeOutPeriod(String v, SIDescriptor si)
    {
        Integer returnTimeOutPeriod = Integer.parseInt(v);
        si.setReturnTimeoutPeriod(returnTimeOutPeriod);
        return false;
    }

    private boolean extractServiceInstanceResponderPortId(String v, SIDescriptor si)
    {
        si.setResponderPortIdentifier(v);
        return false;
    }

    private boolean extractServiceInstanceResponderId(String v, SIDescriptor si)
    {
        si.setResponderId(v);
        return false;
    }

    private boolean extractServiceInstanceInitiatorId(String v, SIDescriptor si)
    {
        si.setInitiatorId(v);
        return false;
    }

    private boolean extractServiceInstanceStopTime(String v, SIDescriptor si)
    {
        try
        {
        	if(v.equalsIgnoreCase(this.VOID) == false)
        	{
        		this.serviceInstanceStopTime.setDateAndTime(v);
        	}
        	else
        	{
        		this.serviceInstanceStopTime = null; // SLEAPIJ-12
        	}
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        this.stopTime = true;
        if (this.stopTime && this.startTime)
        {
            si.setProvisionPeriod(this.serviceInstanceStartTime, this.serviceInstanceStopTime);
        }
        return false;
    }

    private boolean extractServiceInstanceStartTime(String v, SIDescriptor si)
    {
        try
        {
        	if(v.equalsIgnoreCase(this.VOID) == false)
        	{
        		this.serviceInstanceStartTime.setDateAndTime(v);
        	}
        	else
        	{
        		this.serviceInstanceStartTime = null; // SLEAPIJ-12
        	}
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        this.startTime = true;
        if (this.stopTime && this.startTime)
        {
            si.setProvisionPeriod(this.serviceInstanceStartTime, this.serviceInstanceStopTime);
        }
        return false;
    }

    private boolean extractServiceInstanceId(String v, SIDescriptor si)
    {
        ISLE_SII isleSII = new EE_SLE_SII();
        try
        {
            if (v.contains(".vcf="))
            { // version 1
                isleSII.setInitialFormat();
            } // else version 2
            isleSII.setAsciiForm(v);
            si.setServiceInstanceId(isleSII);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        return false;
    }

    public SIDescriptor fillDataFromFile()
    {
        SIDescriptor result = createSIDescription();
        String line = "";
        boolean endOfGroup = false;
        try
        {
            while (!endOfGroup && (line = this.br.readLine()) != null)
            {
                endOfGroup = processLine(result, line);
            }
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
        return result;
    }

    private boolean processLine(SIDescriptor result, String line)
    {
        String key = getKey(line).toLowerCase();
        String value = getValue(line);
        value = Util.prepareValue(value);
        if (key.equalsIgnoreCase("service-instance-start-time"))
        {
            value = value.toUpperCase();
        }
        if (key.equalsIgnoreCase("service-instance-stop-time"))
        {
            value = value.toUpperCase();
        }

        return this.processors.get(key).processValue(value, result);
    }

    protected abstract SIDescriptor createSIDescription();

    private String getKey(String line)
    {
        return Util.splitOnFirst(line, '=')[0];
    }

    private String getValue(String line)
    {
        return Util.splitOnFirst(line, '=')[1];
    }

}
