package ccsds.sle.api.isle.iapl;

import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The interface is passed to all API components and is used to enter messages
 * into the system log and to notify the application of specific alarms. The
 * types of alarms, which can be passed to this interface, are defined in
 * chapter 4. An alarm is complemented by a brief 20-character text that can be
 * used for display. The methods in this interface do not report the time of an
 * event. It is expected that the time is added by the implementation of the
 * interface.
 * 
 * @version: 1.0, October 2015
 */

public interface ISLE_Reporter extends IUnknown
{
    /**
     * Enters a message into the system log
     * 
     * @param component the SLE_Component
     * @param sii the Service Instance identifier
     * @param type the log message type
     * @param messageId the message identifier
     * @param message the message
     */
    void logRecord(SLE_Component component, ISLE_SII sii, SLE_LogMessageType type, long messageId, String message);

    /**
     * Notifies the application of a specific event
     * 
     * @param alarm the alarm type
     * @param component the SLE_Component
     * @param sii the Service Instance identifier
     * @param messageId the message identifier
     * @param message the message
     */
    void notify(SLE_Alarm alarm, SLE_Component component, ISLE_SII sii, long messageId, String message);
}
