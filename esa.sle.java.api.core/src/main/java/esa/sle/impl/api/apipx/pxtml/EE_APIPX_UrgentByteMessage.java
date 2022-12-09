package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.OutputStream;

import ccsds.sle.api.isle.exception.SleApiException;

public class EE_APIPX_UrgentByteMessage extends EE_APIPX_TMLMessage
{
    private final int paDiag;


    public EE_APIPX_UrgentByteMessage(int diag)
    {
        this.paDiag = diag;
    }

    @Override
    public void writeTo(OutputStream socketOutStream) throws SleApiException, IOException
    {       
    }

    @Override
    public void processOn(EE_APIPX_Channel channel)
    {
        channel.peerAbortInd(this);
    }

    @Override
    public int getLength()
    {
        return 1;
    }

    public int getUBDiagnostic()
    {
        return this.paDiag;
    }
}
