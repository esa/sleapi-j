package esa.sle.impl.api.apipx.pxtml;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;

public class EE_APIPX_ISP1ProtocolAbortDiagnostics
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_ISP1ProtocolAbortDiagnostics.class.getName());

    private static final int length = 9;

    private EE_APIPX_ISP1ProtocolAbortOriginator paOriginator;

    private int tmlDiagnosticCode;

    private int tcpErrorCode;


    public EE_APIPX_ISP1ProtocolAbortDiagnostics(EE_APIPX_ISP1ProtocolAbortOriginator paOriginator,
                                                 int tmlDiagnosticCode,
                                                 int tcpErrorCode)
    {
        this.paOriginator = paOriginator;
        this.tmlDiagnosticCode = tmlDiagnosticCode;
        this.tcpErrorCode = tcpErrorCode;
    }

    public EE_APIPX_ISP1ProtocolAbortOriginator getPaOriginator()
    {
        return this.paOriginator;
    }

    public void setPaOriginator(EE_APIPX_ISP1ProtocolAbortOriginator paOriginator)
    {
        this.paOriginator = paOriginator;
    }

    public int getTmlDiagnosticCode()
    {
        return this.tmlDiagnosticCode;
    }

    public void setTmlDiagnosticCode(int tmlDiagnosticCode)
    {
        this.tmlDiagnosticCode = tmlDiagnosticCode;
    }

    public int getTcpErrorCode()
    {
        return this.tcpErrorCode;
    }

    public void setTcpErrorCode(int tcpErrorCode)
    {
        this.tcpErrorCode = tcpErrorCode;
    }

    public byte[] getDiagAsByteArray()
    {
        byte[] diag = new byte[length];
        diag[0] = (byte) this.paOriginator.getCode();
        try
        {
            EE_IntegralEncoder.encodeUnsignedMSBFirst(diag, 1, 4, this.tmlDiagnosticCode);
            EE_IntegralEncoder.encodeUnsignedMSBFirst(diag, 5, 4, this.tcpErrorCode);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);

        }
        return diag;
    }

    @Override
    public String toString()
    {
        return "[paOriginator=" + this.paOriginator + ", tmlDiagnosticCode=" + this.tmlDiagnosticCode
               + ", tcpErrorCode=" + this.tcpErrorCode + "]";
    }
}
