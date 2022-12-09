package esa.sle.impl.eapi.dcw;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;

/**
 * The class holds all information relevant to identify an event (up-call)
 * received from the service instance in the service element.
 */
public class EE_DCW_Event
{
    private final DCW_Event_Type eventType;

    private final ISLE_Operation operation;


    @SuppressWarnings("unused")
    private EE_DCW_Event()
    {
        this.eventType = DCW_Event_Type.dcwEVT_noEvent;
        this.operation = null;
    }

    @SuppressWarnings("unused")
    private EE_DCW_Event(final EE_DCW_Event right)
    {
        this.eventType = right.eventType;
        this.operation = right.operation;
    }

    public EE_DCW_Event(DCW_Event_Type type, ISLE_Operation pop)
    {
        this.eventType = type;
        this.operation = pop;
    }

    public DCW_Event_Type getEventType()
    {
        return this.eventType;
    }

    public ISLE_Operation getOperation()
    {
        return this.operation;
    }
}
