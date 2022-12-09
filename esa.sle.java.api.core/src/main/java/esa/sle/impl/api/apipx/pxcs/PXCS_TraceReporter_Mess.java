package esa.sle.impl.api.apipx.pxcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_TraceLevel;

public class PXCS_TraceReporter_Mess extends PXCS_Message
{
    private static final Logger LOG = Logger.getLogger(PXCS_TraceReporter_Mess.class.getName());

    private long messId;

    private SLE_TraceLevel level;

    private SLE_Component component;

    private SLE_LogMessageType messType;

    private String sii;

    private String text;

    private SLE_Alarm alarm;


    public PXCS_TraceReporter_Mess(long messId,
                                   SLE_TraceLevel level,
                                   SLE_Component component,
                                   SLE_LogMessageType messType,
                                   String sii,
                                   String text,
                                   SLE_Alarm alarm)
    {
        this.messId = messId;
        this.level = level;
        this.component = component;
        this.messType = messType;
        this.sii = sii;
        this.text = text;
        this.alarm = alarm;
    }

    public PXCS_TraceReporter_Mess()
    {
        this.level = SLE_TraceLevel.sleTL_invalid;
        this.component = SLE_Component.sleCP_invalid;
        this.messType = SLE_LogMessageType.sleLM_invalid;
        this.alarm = SLE_Alarm.sleAL_invalid;
        this.sii = "";
        this.text = "";
    }

    public PXCS_TraceReporter_Mess(byte[] data)
    {
        fromByteArray(data);
    }

    public long getMessId()
    {
        return this.messId;
    }

    public void setMessId(long messId)
    {
        this.messId = messId;
    }

    public SLE_TraceLevel getLevel()
    {
        return this.level;
    }

    public void setLevel(SLE_TraceLevel level)
    {
        this.level = level;
    }

    public SLE_Component getComponent()
    {
        return this.component;
    }

    public void setComponent(SLE_Component component)
    {
        this.component = component;
    }

    public SLE_LogMessageType getMessType()
    {
        return this.messType;
    }

    public void setMessType(SLE_LogMessageType messType)
    {
        this.messType = messType;
    }

    public String getSii()
    {
        return this.sii;
    }

    public void setSii(String sii)
    {
        this.sii = sii;
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public SLE_Alarm getAlarm()
    {
        return this.alarm;
    }

    public void setAlarm(SLE_Alarm alarm)
    {
        this.alarm = alarm;
    }

    @Override
    public byte[] toByteArray()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
            // set the messId
            dos.writeLong(this.messId);

            // set the trace level
            dos.writeInt(this.level.getCode());

            // set the component
            dos.writeInt(this.component.getCode());

            // set the message type
            dos.writeInt(this.messType.getCode());

            // set the sii
            dos.writeUTF(this.sii);

            // set the text
            dos.writeUTF(this.text);

            // set the alarm
            dos.writeInt(this.alarm.getCode());

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
            // set the messId
            this.messId = dis.readLong();

            // set the trace level
            this.level = SLE_TraceLevel.getTraceLevelByCode(dis.readInt());

            // set the component
            this.component = SLE_Component.getComponentByCode(dis.readInt());

            // set the message type
            this.messType = SLE_LogMessageType.getLogMessageByCode(dis.readInt());

            // set the sii
            this.sii = dis.readUTF();

            // set the text
            this.text = dis.readUTF();

            // set the alarm
            this.alarm = SLE_Alarm.getAlarmByCode(dis.readInt());
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
        return "PXCS_TraceReporter Message[ messId = " + this.messId + ", level = " + this.level + ", component = "
               + this.component + ", messType = " + this.messType + ", sii = " + this.sii + ", text = " + this.text
               + ", alarm =" + this.alarm + "]";
    }
}
