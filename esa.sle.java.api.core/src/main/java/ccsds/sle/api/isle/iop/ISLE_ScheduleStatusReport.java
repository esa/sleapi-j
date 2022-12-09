package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.it.SLE_ReportRequestType;
import ccsds.sle.api.isle.it.SLE_ScheduleStatusReportDiagnostic;

/**
 * The interface defines the Schedule Status Report.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_ScheduleStatusReport extends ISLE_ConfirmedOperation
{
    /**
     * Returns the report request type
     * 
     * @return the report request type
     */
    SLE_ReportRequestType getReportRequestType();

    /**
     * Sets the report request type
     * 
     * @param type the report request type
     */
    void setReportRequestType(SLE_ReportRequestType type);

    /**
     * Returns the reporting cycle
     * 
     * @return the reporting cycle
     */
    int getReportingCycle();

    /**
     * Sets the reporting cycle
     * 
     * @param cycle the reporting cycle
     */
    void setReportingCycle(int cycle);

    /**
     * Returns the Schedule Status Report diagnostic
     * 
     * @return the Schedule Status Report diagnostic
     */
    SLE_ScheduleStatusReportDiagnostic getSSRDiagnostic();

    /**
     * Sets the Schedule Status Report diagnostic
     * 
     * @param diagnostic the Schedule Status Report diagnostic
     */
    void setSSRDiagnostic(SLE_ScheduleStatusReportDiagnostic diagnostic);
}
