package esa.sle.impl.api.apipx.pxcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PXCS_Header_Mess extends PXCS_Message
{
    private static final Logger LOG = Logger.getLogger(PXCS_Header_Mess.class.getName());

    public static final int hMsgLength = 9;

    private boolean lastPdu;

    private int mid;

    private int length;


    public PXCS_Header_Mess(boolean lastPdu, int mid, int length)
    {
        this.lastPdu = lastPdu;
        this.mid = mid;
        this.length = length;
    }

    public PXCS_Header_Mess(byte[] data)
    {
        fromByteArray(data);
    }

    public boolean isLastPdu()
    {
        return this.lastPdu;
    }

    public void setLastPdu(boolean lastPdu)
    {
        this.lastPdu = lastPdu;
    }

    public int getMid()
    {
        return this.mid;
    }

    public void setMid(int mid)
    {
        this.mid = mid;
    }

    public int getLength()
    {
        return this.length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    @Override
    public byte[] toByteArray()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
            // set the last pdu
            dos.writeBoolean(this.lastPdu);

            // set the mid
            dos.writeInt(this.mid);

            // set the length
            dos.writeInt(this.length);

            dos.flush();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return null;
        }

        return bos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] data)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        try
        {
            // set the last pdu
            this.lastPdu = dis.readBoolean();

            // set the mid
            this.mid = dis.readInt();

            // set the length
            this.length = dis.readInt();
        }
        catch (IOException e1)
        {
            LOG.log(Level.FINE, "IOException ", e1);
            return;
        }
    }

    @Override
    public String toString()
    {
        return "PXCS_Header Message [lastPdu = " + this.lastPdu + ", mid = "
               + PXCS_MessId.getPXCSMessIdByCode(this.mid) + ", length = " + this.length + "]";
    }
}
