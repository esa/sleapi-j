package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.OutputStream;

import ccsds.sle.api.isle.exception.SleApiException;

public abstract class EE_APIPX_TMLMessage
{
    public abstract void writeTo(OutputStream socketOutStream) throws SleApiException, IOException;

    public abstract void processOn(EE_APIPX_Channel channel);

    public abstract int getLength();
}
