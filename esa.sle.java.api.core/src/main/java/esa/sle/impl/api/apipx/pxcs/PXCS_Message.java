package esa.sle.impl.api.apipx.pxcs;

public abstract class PXCS_Message
{
    public abstract byte[] toByteArray();

    public abstract void fromByteArray(byte[] data);
}
