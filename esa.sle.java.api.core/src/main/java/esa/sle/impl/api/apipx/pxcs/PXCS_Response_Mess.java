package esa.sle.impl.api.apipx.pxcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;

public class PXCS_Response_Mess extends PXCS_Message
{
    private static final Logger LOG = Logger.getLogger(PXCS_Response_Mess.class.getName());

    private HRESULT result;

    private int regId;


    public PXCS_Response_Mess(HRESULT result, int regId)
    {
        this.result = result;
        this.regId = regId;
    }

    public PXCS_Response_Mess(byte[] data)
    {
        fromByteArray(data);
    }

    public HRESULT getResult()
    {
        return this.result;
    }

    public void setResult(HRESULT result)
    {
        this.result = result;
    }

    public int getRegId()
    {
        return this.regId;
    }

    public void setRegId(int regId)
    {
        this.regId = regId;
    }

    @Override
    public byte[] toByteArray()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
            // set the result
            dos.writeInt(this.result.getCode());

            // set the regId
            dos.writeInt(this.regId);

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
            // set the diagnostic
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("result is set, data length : " + data.length);
            }
            this.result = HRESULT.getResultByCode(dis.readInt());

            // set the originatorIsLocal
            this.regId = dis.readInt();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
    }

    @Override
    public String toString()
    {
        return "PXCS_Response Message[result = " + this.result + ", regId = " + this.regId + "]";
    }
}
