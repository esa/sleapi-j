package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import esa.sle.impl.ifs.gen.EE_GenStrUtil;

public class EE_APIPX_HBMessage extends EE_APIPX_TMLMessage
{
    private static byte firstByte = 0x03;

    private static int hdrLength = 8;


    public EE_APIPX_HBMessage()
    {}

    @Override
    public void writeTo(OutputStream socketOutStream) throws IOException
    {
        // build the HB message
        byte[] buff = new byte[hdrLength];
        Arrays.fill(buff, (byte) 0x00);
        buff[0] = firstByte;

        // write it to the socket
        socketOutStream.write(buff);

        EE_GenStrUtil.print("Writing to socket:", buff);
    }

    @Override
    public void processOn(EE_APIPX_Channel channel)
    {
        channel.hbtReceived(this);
    }

    @Override
    public int getLength()
    {
        return hdrLength;
    }
}
