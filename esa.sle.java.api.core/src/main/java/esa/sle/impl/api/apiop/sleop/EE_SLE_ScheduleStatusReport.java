/**
 * @(#) EE_SLE_ScheduleStatusReport.java
 */

package esa.sle.impl.api.apiop.sleop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_ScheduleStatusReport;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_ReportRequestType;
import ccsds.sle.api.isle.it.SLE_ScheduleStatusReportDiagnostic;

/**
 * The class implements the SCHEDULE-STATUS-REPORT operation.
 */
public class EE_SLE_ScheduleStatusReport extends IEE_SLE_ConfirmedOperation 
										implements ISLE_ScheduleStatusReport
{
    /**
     * The report request type
     */
    private SLE_ReportRequestType reportReqType;

    /**
     * The reporting cycle
     */
    private int reportingCycle;

    /**
     * The diagnostic code
     */
    private SLE_ScheduleStatusReportDiagnostic ssrDiagnostic;

    /**
     * The minimum value of the reporting cycle in seconds.
     * For SLES V4 and earlier the default value is 2, but with 
     * SLES V5 and later the value shall be obtained from Service
     * Management (parameterId = 301 minReportingCycle).
     * Prior to version V5 the variable was static int.
     */
    private long minRepCycle = 2;

    /**
     * The maximum value of the reporting cycle in seconds.
     */
    private static int maxRepCycle = 600;


    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     * 
     * @param opSrvType
     * @param version
     * @param preporter
     */
    public EE_SLE_ScheduleStatusReport(SLE_ApplicationIdentifier opSrvType, int version, ISLE_Reporter preporter)
    {
        super(opSrvType, SLE_OpType.sleOT_scheduleStatusReport, version, preporter);
        this.reportReqType = SLE_ReportRequestType.sleRRT_invalid;
        this.reportingCycle = 0;
        this.ssrDiagnostic = SLE_ScheduleStatusReportDiagnostic.sleSSD_invalid;
    }

    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     * 
     * @param opSrvType
     * @param version
     * @param preporter
     * @param minRepCycle 
     */
    public EE_SLE_ScheduleStatusReport(SLE_ApplicationIdentifier opSrvType, int version, 
    		                           ISLE_Reporter preporter, long minRepCycle)
    {
        this(opSrvType, version, preporter);
        
        if(version >= 5)
        {
        	this.minRepCycle = minRepCycle;
        }
    }
    
    /**
     * Copy constructor
     * 
     * @param right
     */
    protected EE_SLE_ScheduleStatusReport(EE_SLE_ScheduleStatusReport right)
    {
        super(right);
        this.reportReqType = right.reportReqType;
        this.reportingCycle = right.reportingCycle;
        this.ssrDiagnostic = right.ssrDiagnostic;
    }

    /**
	 * 
	 */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ScheduleStatusReport.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.reportReqType == SLE_ReportRequestType.sleRRT_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG, "Invald Report Request Type");
        }
        else
        {
            if (this.reportReqType == SLE_ReportRequestType.sleRRT_periodically)
            {
                if (this.reportingCycle < minRepCycle || this.reportingCycle > maxRepCycle)
                {
                    throw new SleApiException(HRESULT.SLE_E_RANGE, "Invalid Reporting Cycle");
                }
            }
        }
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();

        if (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics
            && this.ssrDiagnostic == SLE_ScheduleStatusReportDiagnostic.sleSSD_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG, "Invalid Schedule Status Report Diagnostic");
        }
    }

    @Override
    public synchronized SLE_ReportRequestType getReportRequestType()
    {
        return this.reportReqType;
    }

    @Override
    public synchronized void setReportRequestType(SLE_ReportRequestType type)
    {
        this.reportReqType = type;
    }

    @Override
    public synchronized int getReportingCycle()
    {
        return this.reportingCycle;
    }

    @Override
    public synchronized void setReportingCycle(int cycle)
    {
        this.reportingCycle = cycle;
    }

    @Override
    public synchronized SLE_ScheduleStatusReportDiagnostic getSSRDiagnostic()
    {
        return this.ssrDiagnostic;
    }

    @Override
    public synchronized void setSSRDiagnostic(SLE_ScheduleStatusReportDiagnostic diagnostic)
    {
        setSpecificDiagnostics();
        this.ssrDiagnostic = diagnostic;
    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        return new EE_SLE_ScheduleStatusReport(this);
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder(maxDumpLength);
        printOn(os, maxDumpLength);

        os.append("Report Request Type    : " + this.reportReqType.toString() + "\n");
        os.append("Reporting Cycle        : " + this.reportingCycle + " seconds" + "\n");
        os.append("SSR Diagnostic         : " + this.ssrDiagnostic + "\n");
        return os.toString();
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.reportReqType == null) ? 0 : this.reportReqType.hashCode());
        result = prime * result + this.reportingCycle;
        result = prime * result + ((this.ssrDiagnostic == null) ? 0 : this.ssrDiagnostic.hashCode());
        return result;
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_SLE_ScheduleStatusReport other = (EE_SLE_ScheduleStatusReport) obj;
        if (this.reportReqType != other.reportReqType)
        {
            return false;
        }
        if (this.reportingCycle != other.reportingCycle)
        {
            return false;
        }
        if (this.ssrDiagnostic != other.ssrDiagnostic)
        {
            return false;
        }
        return true;
    }

    @Override
    public synchronized String toString()
    {
        return "EE_SLE_ScheduleStatusReport [reportReqType=" + this.reportReqType + ", reportingCycle="
               + this.reportingCycle + ", ssrDiagnostic=" + this.ssrDiagnostic + "]";
    }

}
