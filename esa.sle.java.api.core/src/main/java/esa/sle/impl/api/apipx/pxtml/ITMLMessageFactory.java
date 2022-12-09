package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.InputStream;

import ccsds.sle.api.isle.exception.SleApiException;

public interface ITMLMessageFactory
{
    EE_APIPX_TMLMessage createTmlMessage(byte[] initialEightBytes, InputStream is) throws SleApiException, IOException;
}
