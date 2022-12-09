package esa.sle.impl.api.apipx.pxtml;

import java.io.InputStream;

public class EE_APIPX_HBMessageFactory implements ITMLMessageFactory
{

    @Override
    public EE_APIPX_TMLMessage createTmlMessage(byte[] initialEightBytes, InputStream is)
    {
        return new EE_APIPX_HBMessage();
    }

    public EE_APIPX_TMLMessage createTmlMessage()
    {
        return new EE_APIPX_HBMessage();
    }

}
