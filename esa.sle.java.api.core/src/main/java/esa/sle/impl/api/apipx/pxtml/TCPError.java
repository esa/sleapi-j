package esa.sle.impl.api.apipx.pxtml;

import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TCPErrors;

public class TCPError
{
    private long errorCode;

    private EE_APIPX_TCPErrors error;


    public TCPError(long errorCode, EE_APIPX_TCPErrors error)
    {
        this.errorCode = errorCode;
        this.error = error;
    }

    public long getErrorCode()
    {
        return this.errorCode;
    }

    public void setErrorCode(long errorCode)
    {
        this.errorCode = errorCode;
    }

    public EE_APIPX_TCPErrors getError()
    {
        return this.error;
    }

    public void setError(EE_APIPX_TCPErrors error)
    {
        this.error = error;
    }
}
