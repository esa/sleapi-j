/**
 * @(#) EE_CLTU_TransferData.java
 */

package esa.sle.impl.api.apiop.cltuop;

import java.util.Arrays;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_TransferData;
import ccsds.sle.api.isrv.icltu.types.CLTU_TransferDataDiagnostic;
import esa.sle.impl.api.apiop.sleop.IEE_SLE_ConfirmedOperation;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;

/**
 * The class implements the CLTU specific TransferData operation
 */
public class EE_CLTU_TransferData extends IEE_SLE_ConfirmedOperation implements ICLTU_TransferData
{
    /**
     * The CLTU identification.
     */
    private long cltuID = 0;

    /**
     * The next expected CLTU identification.
     */
    private long expectedCLTUID = 0;

    /**
     * The earliest radiation time.
     */
    private ISLE_Time earliestRadTime = null;

    /**
     * The latest radiation time.
     */
    private ISLE_Time latestRadTime = null;

    /**
     * The delay time.
     */
    private long delayTime;

    /**
     * Indicates whether a notification shall be returned when the CLTU has been
     * radiated.
     */
    private SLE_SlduStatusNotification radiationNotification = SLE_SlduStatusNotification.sleSN_invalid;

    /**
     * The CLTU data.
     */
    private byte[] data = null;

    /**
     * The available CLTU buffer size in bytes.
     */
    private long cltuBufferAvailable = 0;

    /**
     * The CLTU Transfer Data diagnostic.
     */
    private CLTU_TransferDataDiagnostic transferDataDiagnostic = CLTU_TransferDataDiagnostic.cltuXFD_invalid;


    private EE_CLTU_TransferData(final EE_CLTU_TransferData right)
    {
        super(right);
        if (right.data != null)
        {
            this.data = new byte[right.data.length];
            for (int i = 0; i < right.data.length; i++)
            {
                this.data[i] = right.data[i];
            }
        }
        if (right.earliestRadTime != null)
        {
            this.earliestRadTime = right.earliestRadTime.copy();
        }
        if (right.latestRadTime != null)
        {
            this.latestRadTime = right.latestRadTime.copy();
        }
        this.delayTime = right.delayTime;
        this.cltuID = right.cltuID;
        this.expectedCLTUID = right.expectedCLTUID;
        this.radiationNotification = right.radiationNotification;
        this.cltuBufferAvailable = right.cltuBufferAvailable;
        this.transferDataDiagnostic = right.transferDataDiagnostic;
    }

    public EE_CLTU_TransferData(int version, ISLE_Reporter preporter)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, SLE_OpType.sleOT_transferData, version, preporter);
        this.cltuID = 0;
        this.expectedCLTUID = 0;
        this.earliestRadTime = null;
        this.latestRadTime = null;
        this.delayTime = 0;
        this.radiationNotification = SLE_SlduStatusNotification.sleSN_invalid;
        this.data = null;
        this.cltuBufferAvailable = 0;
        this.transferDataDiagnostic = CLTU_TransferDataDiagnostic.cltuXFD_invalid;
    }

    @Override
    public synchronized long getCltuId()
    {
        return this.cltuID;
    }

    @Override
    public synchronized long getExpectedCltuId()
    {
        return this.expectedCLTUID;
    }

    @Override
    public synchronized ISLE_Time getEarliestRadTime()
    {
        return this.earliestRadTime;
    }

    @Override
    public synchronized ISLE_Time getLatestRadTime()
    {
        return this.latestRadTime;
    }

    @Override
    public synchronized long getDelayTime()
    {
        return this.delayTime;
    }

    @Override
    public synchronized SLE_SlduStatusNotification getRadiationNotification()
    {
        return this.radiationNotification;
    }

    @Override
    public synchronized byte[] getData()
    {
        return this.data;
    }

    @Override
    public synchronized byte[] removeData()
    {
        byte[] pretval = new byte[this.data.length];
        for (int i = 0; i < this.data.length; i++)
        {
            pretval[i] = this.data[i];
        }
        this.data = null;
        return pretval;
    }

    @Override
    public synchronized long getCltuBufferAvailable()
    {
        return this.cltuBufferAvailable;
    }

    @Override
    public synchronized CLTU_TransferDataDiagnostic getTransferDataDiagnostic()
    {
        assert (getResult() == SLE_Result.sleRES_negative && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics) : "error getxxx";
        return this.transferDataDiagnostic;
    }

    @Override
    public synchronized void setCltuId(long id)
    {
        this.cltuID = id;
    }

    @Override
    public synchronized void setExpectedCltuId(long id)
    {
        this.expectedCLTUID = id;
    }

    @Override
    public synchronized void setEarliestRadTime(ISLE_Time earliestTime)
    {
        this.earliestRadTime = earliestTime.copy();
    }

    @Override
    public synchronized void putEarliestRadTime(ISLE_Time pearliestTime)
    {
        this.earliestRadTime = pearliestTime;
    }

    @Override
    public synchronized void setLatestRadTime(ISLE_Time latestTime)
    {
        this.latestRadTime = latestTime.copy();
    }

    @Override
    public synchronized void putLatestRadTime(ISLE_Time platestTime)
    {
        this.latestRadTime = platestTime;
    }

    @Override
    public synchronized void setDelayTime(long delay)
    {
        this.delayTime = delay;
    }

    @Override
    public synchronized void setRadiationNotification(SLE_SlduStatusNotification ntf)
    {
        this.radiationNotification = ntf;
    }

    @Override
    public synchronized void setData(byte[] pdata)
    {
    	// SLEAPIJ-38 (-44)
    	this.data = Arrays.copyOf(pdata, pdata.length);
    	// 
        //this.data = new byte[pdata.length];
        //for (int i = 0; i < pdata.length; i++) // SLEAPIJ-44
        //{
        //    this.data[i] = pdata[i];
        //}
    }

    @Override
    public synchronized void putData(byte[] pdata)
    {
        this.data = pdata;
    }

    @Override
    public synchronized void setCltuBufferAvailable(long bufAvail)
    {
        this.cltuBufferAvailable = bufAvail;
    }

    @Override
    public synchronized void setTransferDataDiagnostic(CLTU_TransferDataDiagnostic diagnostic)
    {
        setSpecificDiagnostics();
        this.transferDataDiagnostic = diagnostic;
    }

    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {

        super.verifyInvocationArguments();

        if (this.earliestRadTime != null && this.latestRadTime != null)
        {
        	// SLEAPIJ-35 radiation times can be equal, use <= not only <
            if (!(this.earliestRadTime.compareTo(this.latestRadTime) <= 0))
            {
                throw new SleApiException(HRESULT.SLE_E_TIMERANGE);
            }
        }
        if (this.radiationNotification == SLE_SlduStatusNotification.sleSN_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
        if (this.data == null)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }

    }

    @Override
    public synchronized ISLE_Operation copy()
    {
        EE_CLTU_TransferData ptmp = new EE_CLTU_TransferData(this);
        return ptmp;
    }

    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder oss = new StringBuilder("");
        printOn(oss, maxDumpLength);
        oss.append("CLTU ID                : " + this.cltuID + "\n");
        oss.append("Expected CLTU ID       : " + this.expectedCLTUID + "\n");
        if (this.earliestRadTime != null)
        {
            String str = this.earliestRadTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Earliest Radiation time: " + str + "\n");
        }
        else
        {
            oss.append("Earliest Radiation time: \n");
        }
        if (this.latestRadTime != null)
        {
            String str = this.latestRadTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_microSec);
            oss.append("Latest Radiation time  : " + str + "\n");
        }
        else
        {
            oss.append("Latest Radiation time  : \n");
        }
        oss.append("Delay time             : " + this.delayTime + "\n");
        oss.append("Radiation Notification : " + this.radiationNotification + "\n");
        if (this.data != null)
        {
            long inlen = (maxDumpLength < this.data.length) ? maxDumpLength : this.data.length;
            String str = EE_GenStrUtil.convAscii(this.data, inlen);
            oss.append("Data                   : " + str + "\n");
        }
        else
        {
            oss.append("Data                  : \n");
        }
        oss.append("Cltu Buffer Available : " + this.cltuBufferAvailable + "\n");
        oss.append("Transfer Diagnostic   : " + this.transferDataDiagnostic + "\n");

        String ret = oss.toString();
        return ret;
    }

    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();

        if (getResult() == SLE_Result.sleRES_positive)
        {
            if (this.expectedCLTUID != this.cltuID + 1)
            {
                throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
            }

        }
        else if (getResult() == SLE_Result.sleRES_negative)
        {
            if (getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                if (this.transferDataDiagnostic == CLTU_TransferDataDiagnostic.cltuXFD_invalid)
                {
                    throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ICLTU_TransferData.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized String toString()
    {
        return "EE_CLTU_TransferData [cltuID=" + this.cltuID + ", expectedCLTUID=" + this.expectedCLTUID
               + ", earliestRadTime=" + ((this.earliestRadTime != null) ? this.earliestRadTime : "")
               + ", latestRadTime=" + ((this.latestRadTime != null) ? this.latestRadTime : "") + ", delayTime="
               + this.delayTime + ", radiationNotification=" + this.radiationNotification + ", data="
               + ((this.data != null) ? Arrays.toString(this.data) : "") + ", cltuBufferAvailable="
               + this.cltuBufferAvailable + ", transferDataDiagnostic=" + this.transferDataDiagnostic + "]";
    }

}
