package esa.sle.impl.api.apipx.pxcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PXCS_Register_Mess extends PXCS_Message
{
    private static final Logger LOG = Logger.getLogger(PXCS_Register_Mess.class.getName());

    private boolean initialFormatUsed;

    private int regId;

    private String sii;

    private String portname;


    public PXCS_Register_Mess(boolean initialFormatUsed, int regId, String sii, String portname)
    {
        this.initialFormatUsed = initialFormatUsed;
        this.regId = regId;
        this.sii = sii;
        this.portname = portname;
    }

    public PXCS_Register_Mess()
    {
        this.sii = "";
        this.portname = "";
    }

    public PXCS_Register_Mess(byte[] data)
    {
        fromByteArray(data);
    }

    public boolean isInitialFormatUsed()
    {
        return this.initialFormatUsed;
    }

    public void setInitialFormatUsed(boolean initialFormatUsed)
    {
        this.initialFormatUsed = initialFormatUsed;
    }

    public int getRegId()
    {
        return this.regId;
    }

    public void setRegId(int regId)
    {
        this.regId = regId;
    }

    public String getSii()
    {
        return this.sii;
    }

    public void setSii(String sii)
    {
        this.sii = sii;
    }

    public String getPortname()
    {
        return this.portname;
    }

    public void setPortname(String portname)
    {
        this.portname = portname;
    }

    @Override
    public byte[] toByteArray()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
            // set the initial format used
            dos.writeBoolean(this.initialFormatUsed);
            // set the reg id
            dos.writeInt(this.regId);
            // set the sii
            dos.writeUTF(this.sii);
            // set the portname
            dos.writeUTF(this.portname);

            dos.flush();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
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
            // set the initial format used
            this.initialFormatUsed = dis.readBoolean();
            // set the reg id
            this.regId = dis.readInt();
            // set the sii
            this.sii = dis.readUTF();
            // set the portname
            this.portname = dis.readUTF();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
    }

    @Override
    public String toString()
    {
        return "PXCS_Register Message[initialFormatUsed = " + this.initialFormatUsed + ", regId = " + this.regId
               + ", sii = " + this.sii + ", portname = " + this.portname + "]";
    }
}
