/**
 * @(#) EE_StubTrace.java
 */

package esa.sle.impl.ifs.gen;

import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.ifs.time.EE_TIME_Fmt;
import esa.sle.impl.ifs.time.EE_Time;

/**
 * @NameStub Trace@EndName
 * @ResponsibilityProvides a stub implementation for the trace functions.@EndResponsibility
 */
public final class EE_StubTrace implements ISLE_Trace
{

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Trace.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void traceRecord(SLE_TraceLevel level, SLE_Component component, ISLE_SII psii, String text)
    {

        EE_Time tm = new EE_Time();
        tm.update();

        String aTime = tm.getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtA);
        StringBuilder cerr = new StringBuilder();

        cerr.append("-------------------------------------------------\n");
        cerr.append(aTime + "\n");
        cerr.append("Trace Message received from " + component + "\n");
        if ((psii != null) && (!psii.isNull()))
        {
            String tmp = psii.getAsciiForm();
            cerr.append("    service instance: " + tmp + "\n");
            if (tmp != null)
            {
                tmp = null; // ##TD: should use memory manager Free() !!!!
            }
        }
        else
        {
            cerr.append("    no service instance associated \n");
        }
        cerr.append("    trace level: " + level + "\n");
        cerr.append("    text: " + text + "\n");

        System.err.println(cerr);

        if (aTime != null)
        {
            aTime = null;
        }

    }

}
