package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.InputStream;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;

public class EE_APIPX_CtxMessageFactory implements ITMLMessageFactory
{
    private static int msgLength = 12;

    private static byte[] CIProtocolID = { 'I', 'S', 'P', '1' };

    private static int CIVersion = 1;


    @Override
    public EE_APIPX_TMLMessage createTmlMessage(byte[] initialEightBytes, InputStream is) throws SleApiException,
                                                                                         IOException
    {
        // extract information from header and check the length
        int length = (int) EE_IntegralEncoder.decodeUnsignedMSBFirst(initialEightBytes, 4, 4);
        if (length != msgLength)
        {
            throw new SleApiException(HRESULT.EE_E_NOTCTX);
        }

        // extract the body
        byte[] body = new byte[msgLength];
        is.read(body);

        byte[] protocol = new byte[CIProtocolID.length];

        for (int i = 0; i < CIProtocolID.length; i++)
        {
            protocol[i] = body[i];
        }

        // check the version
        int ver = body[7];

        // get the hbt and dead factor
        int hbt = (int) EE_IntegralEncoder.decodeUnsignedMSBFirst(body, 8, 2);
        int deadFactor = (int) EE_IntegralEncoder.decodeUnsignedMSBFirst(body, 10, 2);

        EE_GenStrUtil.print("Creating Tml Message", body);

        // create the context message
        return new EE_APIPX_CtxMessage(hbt, deadFactor, protocol, ver);
    }

    public EE_APIPX_TMLMessage createTmlMessage(int hbt, int deadFactor)
    {
        return new EE_APIPX_CtxMessage(hbt, deadFactor, CIProtocolID, CIVersion);
    }
}
