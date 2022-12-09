package esa.sle.impl.tst.systst;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;
import esa.sle.impl.tst.systst.types.T_RAFCmdAssocClient;

public class EE_SYSTST_RAFAssocClient extends EE_SYSTST_AssocClient
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_RAFAssocClient.class.getName());

    private EE_SYSTST_RAFOpGen opGen;


    public EE_SYSTST_RAFAssocClient(UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, utl);
        this.opGen = null;
    }


    private static String[] helpCommand[] = {
                                             { "bind        (u)     send RAF-BIND operation" },
                                             { "unbind      (u)     send RAF-UNBIND operation" },
                                             { "start       (u)     send RAF-START operation" },
                                             { "stop        (u)     send RAF-STOP operation" },
                                             { "td          (p)     send RAF-TRANSFER-DATA operation" },
                                             { "sn          (p)     send RAF-SYNC-NOTIFY" },
                                             { "ssr         (u)     send RAF-SCHEDULE-STATUS-REPORT operation" },
                                             { "gp          (u)     send RAF-GET-PARAMETER operation" },
                                             { "peer_abort  (u/p)   send RAF-PEER-ABORT operation" },
                                             { "nt                  notify the SI of the transmission of a PDU" },
                                             { "prot_abort          protocol abort to SI" },
                                             { "up                  up to the service element commanding" },
                                             { "s                   switch to the service instace for commanding" },
                                             { "help                this help text" },
                                             { "send_rtn            sends the return PDU of the last memorised confirmed Op" } };


    private T_RAFCmdAssocClient getNextCommand(EE_Reference<String> arg1,
                                               EE_Reference<String> arg2,
                                               EE_Reference<String> arg3)
    {
        T_RAFCmdAssocClient nextCommand = T_RAFCmdAssocClient.T_RAFCmd_Max;
        EE_Reference<String> cmd = new EE_Reference<String>();
        cmd.setReference("");

        while (nextCommand == T_RAFCmdAssocClient.T_RAFCmd_Max)
        {

            prompt();

            utl.read(cmd, false);

            for (int i = 0; i < T_RAFCmdAssocClient.T_RAFCmd_Max.getCode(); i++)
            {
                if (cmd.getReference().equals("?"))
                {
                    cmd.setReference(T_RAFCmdAssocClient.getTRAFCmdAssocClient(T_RAFCmdAssocClient.T_RAFCmd_help
                            .getCode()).toString());
                }
                if ((T_RAFCmdAssocClient.getTRAFCmdAssocClient(i).toString()).equals(cmd.getReference()))
                {
                    nextCommand = T_RAFCmdAssocClient.getTRAFCmdAssocClient(i);
                    break;
                }
            }

            if (nextCommand == T_RAFCmdAssocClient.T_RAFCmd_Max)
            {
                System.out.println("*** unknown command");
                cmd.setReference("");
            }
            else if (nextCommand == T_RAFCmdAssocClient.T_RAFCmd_help)
            {
                help();
                nextCommand = T_RAFCmdAssocClient.T_RAFCmd_Max;
            }
        }
        return nextCommand;
    }

    public void help()
    {
        System.out.println();

        for (int i = 0; i < T_RAFCmdAssocClient.T_RAFCmd_Max.getCode(); i++)
        {
            System.out.println("   " + helpCommand[i]);
        }
        System.out.println();
    }

    private SLE_OpType getOpType(T_RAFCmdAssocClient cmd)
    {
        if (cmd == T_RAFCmdAssocClient.T_RAFCmd_bind)
        {
            return SLE_OpType.sleOT_bind;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_unbind)
        {
            return SLE_OpType.sleOT_unbind;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_start)
        {
            return SLE_OpType.sleOT_start;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_stop)
        {
            return SLE_OpType.sleOT_stop;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_transfer_data)
        {
            return SLE_OpType.sleOT_transferData;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_sync_notify)
        {
            return SLE_OpType.sleOT_syncNotify;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_ssr)
        {
            return SLE_OpType.sleOT_scheduleStatusReport;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_get_prm)
        {
            return SLE_OpType.sleOT_getParameter;
        }
        else if (cmd == T_RAFCmdAssocClient.T_RAFCmd_peer_abort)
        {
            return SLE_OpType.sleOT_peerAbort;
        }
        else
        {
            return SLE_OpType.sleOT_bind; // no better idea
        }
    }

    @Override
    public EE_SYSTST_T_Component startUIF()
    {
        HRESULT rc = HRESULT.S_OK;
        T_RAFCmdAssocClient nextCommand = T_RAFCmdAssocClient.T_RAFCmd_Max;

        EE_Reference<String> arg1 = new EE_Reference<String>();
        EE_Reference<String> arg2 = new EE_Reference<String>();
        EE_Reference<String> arg3 = new EE_Reference<String>();

        while (nextCommand != T_RAFCmdAssocClient.T_RAFCmd_up)
        {
            nextCommand = getNextCommand(arg1, arg2, arg3);

            switch (nextCommand)
            {
            case T_RAFCmd_to_si:
            {
                return EE_SYSTST_T_Component.eeEE_SYSTST_TestSI;
            }
            case T_RAFCmd_bind:
            case T_RAFCmd_unbind:
            case T_RAFCmd_start:
            case T_RAFCmd_stop:
            case T_RAFCmd_transfer_data:
            case T_RAFCmd_sync_notify:
            case T_RAFCmd_ssr:
            case T_RAFCmd_get_prm:
            case T_RAFCmd_peer_abort:
            {
                if (this.opGen == null)
                {
                    this.opGen = new EE_SYSTST_RAFOpGen(this.opFactory, null, false, this.utl);
                }
                ISLE_Operation op = this.opGen.createOp(getOpType(nextCommand), null, null);
                if (op != null)
                {
                    if (this.assoc != null)
                    {
                        rc = HRESULT.S_OK;
                        try
                        {
                            this.assoc.initiateOpInv(op);
                        }
                        catch (SleApiException e)
                        {
                            rc = e.getHResult();
                        }
                        displayResult(rc);
                    }
                    else
                    {
                        System.out.println("No association object yet available");
                    }
                }
                break;

            }
            case T_RAFCmd_notify_transmission:
            {
                break;
            }

            case T_RAFCmd_protocol_abort:
            {
                if (this.assoc != null)
                {
                    try
                    {
                        this.assoc.protocolAbort();
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                }
                else
                {
                    System.out.println("No association object yet available");
                }
            }
            case T_RAFCmd_send_rtn:
            {
                if (this.lastConfirmedOp != null)
                {
                    rc = HRESULT.S_OK;
                    try
                    {
                        this.assoc.initiateOpRtn(this.lastConfirmedOp);
                    }
                    catch (SleApiException e)
                    {
                        rc = e.getHResult();
                    }
                    if (rc != HRESULT.S_OK)
                    {
                        System.out.println("Send Return PDU failed: " + rc);
                    }
                    this.lastConfirmedOp = null;
                }
                else
                {
                    System.out.println("No confirmed operation available");
                }
            }
            default:
                break;
            }

        }
        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;

    }

}
