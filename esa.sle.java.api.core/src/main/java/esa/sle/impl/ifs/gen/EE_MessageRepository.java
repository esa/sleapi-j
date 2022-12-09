/**
 * @(#) EE_MessageRepository.java
 */

package esa.sle.impl.ifs.gen;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;

/**
 * Provides a central repository of message IDs and associated messages.
 * Currently uses a static array of pair values, lookup time is O(n). If this
 * was to be avoided, then some form of initialisation would be required.
 */
public class EE_MessageRepository
{
    private static List<SimpleEntry<Integer, String>> msgRepos = Arrays
            .asList(new AbstractMap.SimpleEntry<Integer, String>(1001, "Protocol Abort"),
                    new AbstractMap.SimpleEntry<Integer, String>(1002, "Operation rejected. <P1>. <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1003,
                                                                 "The link to the application process is broken. <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1004, "<P1> : <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1005,
                                                                 "<P1> : Association state change from <P2> to <P3>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1006,
                                                                 "Read configuration file failed, line <P1>, diagnostic: <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1007, "Operation is queued. <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1008, "Protocol Abort"),
                    new AbstractMap.SimpleEntry<Integer, String>(1009,
                                                                 "Operation queue is full. Operation rejected. <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1010, "Binder receives a TCP CNX indication"),
                    new AbstractMap.SimpleEntry<Integer, String>(1011, "Association receives a Connect indication"),
                    new AbstractMap.SimpleEntry<Integer, String>(1012,
                                                                 "Association sends a Connect request to TML on port <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1013, "Proxy sends a negative Bind Return. <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1014, "Bind aborted before delivery. <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1015, "Peer Abort"),
                    new AbstractMap.SimpleEntry<Integer, String>(1016, "Configuration error: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(1017, "Initialisation error: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_NoSuchFile.getCode(),
                                                                 "No such file: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_OpenDbFailed.getCode(),
                                                                 "Open configuration file failed, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ParsingError.getCode(),
                                                                 "Parsing error: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                                                                 "Configuration error: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_AddPxyRejected.getCode(),
                                                                 "Add Proxy rejected, invalid state"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ProtIdNotSupported.getCode(),
                                                                 "Protocol Id <P1> not supported"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_DuplicateProtId.getCode(),
                                                                 "Duplicate protocol Id <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ProxyNotRegistered.getCode(),
                                                                 "No proxy registered for <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ProxyNotStarted.getCode(),
                                                                 "Proxy not started for <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_NoProxyStarted.getCode(),
                                                                 "No proxy started"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_AccessViolation.getCode(),
                                                                 "Access violation by initiator <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_StateChange.getCode(),
                                                                 "State transition from <P1> to <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_PxyProtocolError.getCode(),
                                                                 "Protocol error, PDU: <P1>, proxy state: <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ProtocolError.getCode(),
                                                                 "Protocol error, event: <P1>, originator: <P2>, SI state: <P3>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_ReturnTimerExpired.getCode(),
                                                                 "Return timer expired"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_PpEnds.getCode(),
                                                                 "End of service provision period"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_PpEndsOnRequest.getCode(),
                                                                 "End of service provision period (Unbind with 'end')"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_BufferQueued.getCode(),
                                                                 "Transfer buffer queued"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_BufferXmitted.getCode(),
                                                                 "Transfer buffer transmitted"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_BufferDiscarded.getCode(),
                                                                 "Transfer buffer discarded"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_LatencyTimerExpired.getCode(),
                                                                 "Latency limit reached"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_SSRRequested.getCode(),
                                                                 "<P1> status report requested"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_SendingPeriodicReport.getCode(),
                                                                 "Sending periodic status report"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_UnexpectedTbPdu.getCode(),
                                                                 "Unexpected PDU in concatenation buffer: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_TimerAborted.getCode(),
                                                                 "<P1> aborted"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_EmptyBufferRec.getCode(),
                                                                 "Empty transfer buffer received"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_InconsistentInvArgs.getCode(),
                                                                 "Inconsistent or incomplete invocation argument(s): <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_InconsistentRtnArgs.getCode(),
                                                                 "Inconsistent or incomplete return argument(s): <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_IncompatibleInvPDU.getCode(),
                                                                 "Incompatible invocation PDU received: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_IncompatibleRtnPDU.getCode(),
                                                                 "Incompatible return PDU received: <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_BindVersionMismatch.getCode(),
                                                                 "Version mismatch between Bind Invocation and Bind Return"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_SE_LM_UnsolicitedPdu.getCode(),
                                                                 "Transmision report for unknown PDU"),
                    new AbstractMap.SimpleEntry<Integer, String>(2000, "Unexpected list element <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(2001, "Value for <P1> missing"),
                    new AbstractMap.SimpleEntry<Integer, String>(2002, "Value for <P1> already set"),
                    new AbstractMap.SimpleEntry<Integer, String>(2003, "Unknown keyword <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(2004, "Duplicate value <P1> for <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLLISTENUNK.getCode(),
                                                                 "Unable to listen, <P1> is unknown"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLLISTENFAIL.getCode(),
                                                                 "TCP listen failed for port <P1>, <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCONNECTFAILALL.getCode(),
                                                                 "Cannot connect to port <P1>, <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCONNECTFAIL.getCode(),
                                                                 "Error connecting to port <P1>, <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLWRITECTXFAIL.getCode(),
                                                                 "Cannot write context message, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLREADCTXFAIL0.getCode(), "Protocol error"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLREADCTXFAIL1.getCode(),
                                                                 "Bad context mesage"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLREADCTXFAIL2.getCode(),
                                                                 "Bad heartbeat parameters"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLESTTIMEOUT.getCode(), "Start-up timeout"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCONNECTEDTIMEOUT.getCode(),
                                                                 "Heartbeat receive timeout"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCLOSETIMEOUT.getCode(),
                                                                 "Disconnect timeout"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCLOSINGDATA.getCode(),
                                                                 "Unexpected data after last PDU"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCLOSINGURG.getCode(),
                                                                 "Unexpected urgent data"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCLOSINGHARD.getCode(),
                                                                 "TCP read/write error, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCONNECTEDCLOSE.getCode(),
                                                                 "Unexpected disconnect by peer"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCONNECTEDHARD.getCode(),
                                                                 "TCP read/write error, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCONNECTEDBADHDR.getCode(),
                                                                 "Bad header received"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTRCVTIMEOUT.getCode(),
                                                                 "Urgent data not received"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTRCVCLOSED.getCode(),
                                                                 "Urgent data not received"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTRCVHARD.getCode(),
                                                                 "TCP read error when in abort, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTHARD.getCode(),
                                                                 "TCP write error when in abort, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTTIMEOUT.getCode(), "Abort timeout"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTRCV.getCode(),
                                                                 "Aborting, diagnostic=<P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCROSSEDHARD.getCode(),
                                                                 "Crossed abort (1) <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCROSSEDCLOSED.getCode(),
                                                                 "Crossed abort (2)"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCROSSEDTIMEOUT.getCode(),
                                                                 "Crossed abort (3)"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCROSSEDURG.getCode(),
                                                                 "Crossed abort, diagnostic = <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTFAIL1.getCode(),
                                                                 "Local abort ignored in closing state"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTFAIL2.getCode(),
                                                                 "Local abort ignored in closing state"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLSENDFAIL.getCode(),
                                                                 "Send request ignored in closing state"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLABORTRCVTOOLATE.getCode(),
                                                                 "Abort received in closing state, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLCLOSELISTENFAIL.getCode(),
                                                                 "Close listen failed, invalid port <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLESTRESPONDERCLOSED.getCode(),
                                                                 "Unexpected close"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLACCEPTFAIL.getCode(),
                                                                 "TCP accept failed, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLLISTENABORTED.getCode(),
                                                                 "TML: listening port aborted."),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLBADINVOCATION.getCode(),
                                                                 "API internal problem (<P1>, <P2>)"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLESTRESPONDERHARD.getCode(),
                                                                 "TCP read error, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_STARTLISTEN.getCode(),
                                                                 "TML: start listen called port=<P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_STOPLISTEN.getCode(),
                                                                 "TML: stop listen called port=<P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_NEWCONN.getCode(),
                                                                 "TML: new connection created"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_SENDCONNECT.getCode(),
                                                                 "TML: connection being attempted <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ONCONNECTED.getCode(),
                                                                 "TML: connection succeeded <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ONPDUTRANSMITTED.getCode(),
                                                                 "TML: PDU transmitted <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ONHBTTRANSMITTED.getCode(),
                                                                 "TML: HBT transmitted <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ONDATAWRITTEN.getCode(),
                                                                 "TML: Data written <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_TIMEOUTHBT.getCode(),
                                                                 "TML: HBT Timer expired <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_SENDPDU.getCode(),
                                                                 "TML: PDU transmission request <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_CANREAD.getCode(),
                                                                 "TML: read notification received "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_HBTREAD.getCode(),
                                                                 "TML: heartbeat message read "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_PDUREAD.getCode(),
                                                                 "TML: PDU read <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_READYTORECEIVEPDU.getCode(),
                                                                 "TML: now receiving incoming PDUs "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_NOTREADYTORECEIVE.getCode(),
                                                                 "TML: not receiving incoming PDUs "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_SENDABORT.getCode(),
                                                                 "TML: abort requested to be sent <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_SENDDISCONNECT.getCode(),
                                                                 "TML: disconnecting from peer "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_SENDRESET.getCode(),
                                                                 "TML: resetting connection  "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_CANREADABORTING.getCode(),
                                                                 "TML: aborting, read notification <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_LASTPDU.getCode(),
                                                                 "TML: receipt of last pdu <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_LASTPDUWRITTEN.getCode(),
                                                                 "TML: last pdu sent <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_CONNCLOSEDNOMINAL.getCode(),
                                                                 "TML: connection is closed "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_BADCONTEXT.getCode(),
                                                                 "TML: Bad context message: <P1> hex "),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_BADHDRREAD.getCode(),
                                                                 "TML: Bad header: <P1> hex"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ESTABLISHCLOSED.getCode(),
                                                                 "TML: Error establishing TML association, peer losed unexpectedly"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ESTABLISHTIMEOUT.getCode(),
                                                                 "TML: Error establishing TML association, timeout occurred"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_ESTABLISHHARDERROR.getCode(),
                                                                 "TCP: Error establishing TML association, <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_TIMEOUT.getCode(),
                                                                 "TML: Timeout occurred <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_HDRREAD.getCode(),
                                                                 "TML: PDU header read <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_CONTEXTRCVD.getCode(),
                                                                 "TML: context message received <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_IOEVENT.getCode(),
                                                                 "TML: notification of IO <P1> event, <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_REQIOEVENT.getCode(),
                                                                 "TML: IO event <P1> requested, <P2> <P3>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_CANCELIOEVENT.getCode(),
                                                                 "TML: IO event <P1> cancelled, <P2> <P3>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_TRACEON.getCode(),
                                                                 "TML: tracing started, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.TMLTR_TRACEOFF.getCode(),
                                                                 "TML: tracing stopped, <P1>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_OP_LM_MissingArg.getCode(),
                                                                 "<P1>: Parameter <P2> not set"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_OP_LM_Inconsistent.getCode(),
                                                                 "<P1>: Inconsistent value for parameter <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_OP_LM_TimeRange.getCode(),
                                                                 "<P1>: start time must be earlier than stop time"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_OP_LM_Range.getCode(),
                                                                 "<P1>: <P2> not in the range of <P3>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_OP_LM_InvalidID.getCode(),
                                                                 "<P1>: Invalid ID given for parameter <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(EE_LogMsg.EE_OP_LM_InvalidMode.getCode(),
                                                                 "<P1>: Invalid mode given for parameter <P2>"),
                    new AbstractMap.SimpleEntry<Integer, String>(0, Character.toString((char) 0)));

    /**
     * Placeholder for parameter 1
     */
    private final static String p1 = "<P1>";

    /**
     * Placeholder for parameter 2
     */
    private final static String p2 = "<P2>";

    /**
     * Placeholder for parameter 3
     */
    private final static String p3 = "<P3>";

    /**
     * Standard error text to be returned if a message cannot be found.
     */
    private final static String errorText = "***";


    /**
     * Returns the message text associated with a particular Message ID.
     * 
     * @throws SleApiException
     */

    public static String getMessageText(long msgID) throws SleApiException
    {
        for (SimpleEntry<Integer, String> element : msgRepos)
        {
            if ((element.getValue() == null) && (element.getKey() == null))
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
            else
            {
                if (element.getKey() == msgID)
                {
                    return element.getValue();
                }
            }
        }
        throw new SleApiException(HRESULT.E_FAIL);

    }

    /**
     * Returns the message text associated with a particular Message ID. If the
     * supplied message-parameters are not zero, the function replaces the
     * parameter-placeholders in the original message with the actual
     * parameters. If the message cannot be found in the repository a standard
     * error string is returned. The returned object must be deleted by the
     * client
     */
    public static String getMessage(long msgId, String... p)
    {
        String msg = null;
        try
        {
            msg = getMessageText(msgId);
        }
        catch (SleApiException e)
        {
            return new String(errorText);
        }
        if (p.length >= 1)
        {
            if (p[0] != null)
            {
                int pos = msg.indexOf(p1, 0);
                if (pos != -1)
                {
                    msg = msg.replace(p1, p[0]);
                }
                if (p.length >= 2)
                {
                    if (p[1] != null)
                    {
                        pos = msg.indexOf(p2, 0);
                        if (pos != -1)
                        {
                            msg = msg.replace(p2, p[1]);
                        }
                    }
                    if (p.length >= 3)
                    {
                        if (p[2] != null)
                        {
                            pos = msg.indexOf(p3, 0);
                            if (pos != -1)
                            {
                                msg = msg.replace(p3, p[2]);
                            }
                        }
                    }
                }
            }
        }
        return msg;

    }

}
