package ccsds.sle.api.isle.exception;

public class SleApiException extends Exception
{

    private static final long serialVersionUID = 1L;

    private HRESULT hresult = HRESULT.S_FALSE;


    public SleApiException(HRESULT hresult)
    {
        super("HRESULT = " + hresult);
        this.hresult = hresult;
    }

    public SleApiException(HRESULT hresult, String message)
    {
        super(message);
        this.hresult = hresult;
    }

    public HRESULT getHResult()
    {
        return this.hresult;
    }
}
