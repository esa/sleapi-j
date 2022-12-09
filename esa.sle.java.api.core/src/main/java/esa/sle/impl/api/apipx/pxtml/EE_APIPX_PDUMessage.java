package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.OutputStream;

import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;

public class EE_APIPX_PDUMessage extends EE_APIPX_TMLMessage
{
    private static byte firstByte = 0x01;

    private static int hdrLength = 8;

    private byte[] body;


    public EE_APIPX_PDUMessage(byte[] body)
    {
        this.body = body;
    }

    @Override
    public void writeTo(OutputStream socketOutStream) throws SleApiException, IOException
    {
        // build the PDU
        int bodyLen = this.body.length;
        byte[] buff = new byte[8 + bodyLen];
        buff[0] = firstByte;
        buff[1] = 0x00;
        buff[2] = 0x00;
        buff[3] = 0x00;
        EE_IntegralEncoder.encodeUnsignedMSBFirst(buff, 4, 4, bodyLen);
        System.arraycopy(this.body, 0, buff, 8, bodyLen);

        // write it to the socket
        socketOutStream.write(buff);

        EE_GenStrUtil.print("Writing to socket:", buff);
    }

    @Override
    public void processOn(EE_APIPX_Channel channel)
    {
        channel.pduReceived(this);
    }

    @Override
    public int getLength()
    {
        return hdrLength + this.body.length;
    }

    public byte[] getBody()
    {
        return this.body;
    }

    public void setBody(byte[] body)
    {
        this.body = body;
    }
}
