/**
 * @(#) ESLE_DefaultLogger.java
 */

package esa.sle.impl.eapi.dfl.idfl;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * The interface is provided to the client for reception of report and tracing
 * messages that cannot be uniquely assigned to a service instance. The
 * interface inherits from ISLE_TraceControl and takes a ISLE_Repoter interface
 * to which all messages are written.
 */
public interface ESLE_DefaultLogger extends IUnknown
{

    void setReporter(ISLE_Reporter preporter);

    void connect(String ipcAddress) throws SleApiException;

    void disconnect() throws SleApiException;

}
