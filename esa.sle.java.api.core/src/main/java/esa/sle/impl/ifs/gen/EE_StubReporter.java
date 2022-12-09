/**
 * @(#) EE_StubReporter.java
 */

package esa.sle.impl.ifs.gen;

import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.ifs.time.EE_TIME_Fmt;
import esa.sle.impl.ifs.time.EE_Time;

/**
 * @ResponsibilityProvides a stub implementation for the reporter functions.@EndResponsibility
 */
public final class EE_StubReporter implements ISLE_Reporter
{

    @Override
    public void logRecord(SLE_Component component, ISLE_SII sii, SLE_LogMessageType type, long messageId, String message)
    {
    	EE_Time tm = new EE_Time();;
    	tm.update();
    	  
    	String aTime = tm.getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA);
        StringBuilder strErr = new StringBuilder();
        strErr.append("-------------------------------------------------\n");
        strErr.append(aTime + "\n");
        strErr.append("Message received from component: " + component + "\n");
        if ((sii != null) && (!sii.isNull()))
        {
            String tmp = sii.getAsciiForm();
            strErr.append("    service instance: " + tmp + "\n");
            if (tmp != null)
            {
                tmp = null;
            }
        }
        else
        {
            strErr.append("    no service instance associated \n");
        }
        	strErr.append("   message type: " + type + "\n");
            strErr.append("   message id: " + messageId + "\n");
            strErr.append("   message: " + message + "\n");
            System.err.println(strErr);
            aTime = null;
    
    }

    @Override
    public void notify(SLE_Alarm alarm, SLE_Component component, ISLE_SII sii, long messageId, String message)
    {

        EE_Time tm = new EE_Time();
        tm.update();

        String aTime = tm.getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA);

        StringBuilder strErr = new StringBuilder();
        strErr.append("-------------------------------------------------\n");
        strErr.append(aTime + "\n");
        strErr.append("Notification of a " + alarm + "\n");
        strErr.append("    component: " + component + "\n");
        if ((sii != null) && (!sii.isNull()))
        {
            String tmp = sii.getAsciiForm();
            strErr.append("    service instance: " + tmp + "\n");
            if (tmp != null)
            {
                tmp = null;
            }
        }
        else
        {
            strErr.append("    no service instance associated \n");
        }
        strErr.append("   message id: " + messageId + "\n");
        strErr.append("   message: " + message + "\n");
        System.err.println(strErr);
        aTime = null;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
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

}
