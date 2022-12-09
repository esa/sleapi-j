package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.OutputStream;

import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;

public class EE_APIPX_CtxMessage extends EE_APIPX_TMLMessage
{
    private static byte firstByte = 0x02;

    private static int msgLength = 20;

    /**
     * Contains the heartbeat transmit interval duration.
     */
    private int hbtDuration;

    /**
     * Contains the dead factor
     */
    private int deadFactor;

    private byte[] protocol;

    private int version;


    /**
     * Constructor
     */
    public EE_APIPX_CtxMessage(int hbtDuration, int deadFactor, byte[] protocol, int version)
    {
        this.hbtDuration = hbtDuration;
        this.deadFactor = deadFactor;
        this.protocol = protocol;
        this.version = version;
    }

    @Override
    public void writeTo(OutputStream socketOutStream) throws IOException, SleApiException
    {
        // build the context message
        byte[] buff = new byte[msgLength];
        buff[0] = firstByte;
        buff[1] = 0x00;
        buff[2] = 0x00;
        buff[3] = 0x00;
        EE_IntegralEncoder.encodeUnsignedMSBFirst(buff, 4, 4, 0x0C);

        buff[8] = (byte) 'I';
        buff[9] = (byte) 'S';
        buff[10] = (byte) 'P';
        buff[11] = (byte) '1';
        buff[12] = 0x00;
        buff[13] = 0x00;
        buff[14] = 0x00;
        buff[15] = 0x01;
        EE_IntegralEncoder.encodeUnsignedMSBFirst(buff, 16, 2, this.hbtDuration);
        EE_IntegralEncoder.encodeUnsignedMSBFirst(buff, 18, 2, this.deadFactor);

        // write it to the socket
        socketOutStream.write(buff);

        EE_GenStrUtil.print("Writing to socket:", buff);
    }

    @Override
    public void processOn(EE_APIPX_Channel channel)
    {
        channel.updateTimeoutOptions(this);
    }

    @Override
    public int getLength()
    {
        return msgLength;
    }

    public int getHbtDuration()
    {
        return this.hbtDuration;
    }

    public void setHbtDuration(int hbtDuration)
    {
        this.hbtDuration = hbtDuration;
    }

    public int getDeadFactor()
    {
        return this.deadFactor;
    }

    public void setDeadFactor(int deadFactor)
    {
        this.deadFactor = deadFactor;
    }

    public byte[] getProtocol()
    {
        return this.protocol;
    }

    public void setProtocol(byte[] protocol)
    {
        this.protocol = protocol;
    }

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
