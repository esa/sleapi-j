package esa.sle.impl.tst.systst;

import java.util.logging.Logger;

import ccsds.sle.api.isle.it.SLE_AppRole;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.T_Cmd;

@SuppressWarnings("unused")
public class EE_SYSTST_SEUIF
{

    private final Logger LOG = Logger.getLogger(EE_SYSTST_SEUIF.class.getName());

    private static String[] helpCommand = {
                                           "initialise         initialises the Builder",
                                           "start              starts the Bulder",
                                           "terminate          terminates the Builder",
                                           "create_si          creates a service instance",
                                           "use_si             creates a service instance specifying the service instance identifier", // //similar
                                                                                                                                       // to
                                                                                                                                       // create_si
                                                                                                                                       // but
                                                                                                                                       // allows
                                                                                                                                       // to
                                                                                                                                       // specify
                                                                                                                                       // the
                                                                                                                                       // full
                                                                                                                                       // SI
                                                                                                                                       // ID
                                                                                                                                       // instead
                                                                                                                                       // of
                                                                                                                                       // only
                                                                                                                                       // the
                                                                                                                                       // lastRDN
                                           "destroy_si         destroys a SI",
                                           "list_si            lists all created SI",
                                           "shutdown           performs shutdown of the Builder",
                                           "wait_event_all_si  Wait for the next event from DCW",
                                           "help               this help text",
                                           "exit               exits the test program",
                                           "down               to a specific service instance",
                                           "sii_base_rtn       set base sii for return SI",
                                           "sii_base_fwd       set base sii for forward SI",
                                           "start_rec          starts recording the commands to a file",
                                           "stop_rec           stops recording commands",
                                           "playback           starts playback of previously recorded commands" };

    private final SLE_AppRole role;

    private UTL utl;

    public EE_SYSTST_SEUIF(SLE_AppRole role, UTL utl)
    {
        this.role = role;
        this.utl = utl;
    }

    private void prompt()
    {
        if (this.role == SLE_AppRole.sleAR_user)
        {
            System.out.println("######U-SE> ");
        }
        else
        {
            System.out.println("######P-SE> ");
        }
    }

    private void help()
    {
        System.out.println("");
        System.out.println("Available commands:");
        for (String helpCmd : helpCommand)
        {
            System.out.println("   " + helpCmd);
        }
        System.out.println("");
    }

    public T_Cmd getNextCommand(EE_Reference<String> arg1,
                                EE_Reference<String> arg2,
                                EE_Reference<String> arg3,
                                EE_Reference<String> arg4,
                                boolean playback)
    {
        T_Cmd nextCommand = T_Cmd.T_Cmd_Max;
        EE_Reference<String> cmd = new EE_Reference<String>();
        cmd.setReference("");

        while (nextCommand == T_Cmd.T_Cmd_Max)
        {
            prompt();
            utl.read(cmd, playback);

            for (int i = 0; i < T_Cmd.T_Cmd_Max.getCode(); i++)
            {
                if (cmd.equals("?"))
                {
                    cmd.setReference(T_Cmd.T_Cmd_help.toString());
                }

                if (T_Cmd.getDiagByCode(i).toString().compareTo(cmd.getReference()) == 0)
                {
                    nextCommand = T_Cmd.getDiagByCode(i);
                    break;
                }
            }

            if (nextCommand == T_Cmd.T_Cmd_Max)
            {
                System.out.println("*** SEUIF unknown command " + T_Cmd.T_Cmd_Max);
                cmd.setReference("");
                break;
            }
            else if (nextCommand == T_Cmd.T_Cmd_help)
            {
                help();
                nextCommand = T_Cmd.T_Cmd_Max;
            }
        }

        if (nextCommand == T_Cmd.T_Cmd_create_si)
        {
            boolean isOk = false;
            boolean isFwdService = false;
            while (!isOk)
            {
                System.out.println("Service Type (RAF/RCF/CLTU/ROCF/FSP/FTCF): ");
                utl.read(arg1, playback);

                if (arg1.getReference().equals("RAF") || arg1.getReference().equals("RCF")
                    || arg1.getReference().equals("ROCF"))
                {
                    isOk = true;
                    isFwdService = false;
                }
                else if (arg1.getReference().equals("FSP") || arg1.getReference().equals("CLTU")
                         || arg1.getReference().equals("FTCF"))
                {
                    isOk = true;
                    isFwdService = true;
                }
                else
                {
                    System.out.println(arg1 + " not supported");
                }

                if (isOk)
                {
                    if (isFwdService)
                    {
                        System.out.println("SII LastRDN (cltu=cltuN or fsp=fspN): ");
                    }
                    else
                    {
                        System.out.println("SII LastRDN (raf|rcf|rocf=onltN|onlcN|offlN): ");
                    }
                    utl.read(arg2, playback);
                    System.out.println("Role (user=u, provider=p): ");
                    utl.read(arg3, playback);
                    System.out.println("Version (1, 2, 3, 4 or 5): ");
                    utl.read(arg4, playback);
                }
            }
        }
        else if (nextCommand == T_Cmd.T_Cmd_use_si)
        {
            boolean isOk = false;
            boolean isFwdService = false;
            while (!isOk)
            {
                System.out.println("Service Type (RAF/RCF/CLTU/ROCF/FSP/FTCF): ");
                utl.read(arg1, playback);

                if (arg1.getReference().equals("RAF") || arg1.getReference().equals("RCF")
                    || arg1.getReference().equals("ROCF"))
                {
                    isOk = true;
                    isFwdService = false;
                }
                else if (arg1.getReference().equals("FSP") || arg1.getReference().equals("CLTU")
                         || arg1.getReference().equals("FTCF"))
                {
                    isOk = true;
                    isFwdService = true;
                }
                else
                {
                    System.out.println(arg1 + " not supported");
                }
                if (isOk)
                {
                    if (isFwdService)
                    {
                        System.out
                                .println("SI Identifier (sagr=[SAGR].spack=[SPACK].fsl-fg=[FSL-FG].[sle-protocol]=[id]): ");
                    }
                    else
                    {
                        System.out
                                .println("SI Identifier (sagr=[SAGR].spack=[SPACK].rsl-fg=[RSL-FG].[sle-protocol]=[id] ): ");
                    }
                    utl.read(arg2, playback);
                    System.out.println("Role (user=u, provider=p): ");
                    utl.read(arg3, playback);
                    System.out.println("Version (1, 2 , 3, 4 or 5): ");
                    utl.read(arg4, playback);
                }
            }
        }
        else if (nextCommand == T_Cmd.T_Cmd_destroy_si)
        {
            System.out.println("Last RDN: ");
            utl.read(arg1, playback);
        }
        else if (nextCommand == T_Cmd.T_Cmd_down)
        {
            System.out.println("Last RDN: ");
            utl.read(arg1, playback);
        }
        else if (nextCommand == T_Cmd.T_Cmd_wait_event_all_si)
        {
            System.out.print("Max waiting time for event (sec): ");
            utl.read(arg1, playback);
            System.out.print("Nb of Event to wait for : ");
            utl.read(arg2, playback);
        }
        else if (nextCommand == T_Cmd.T_Cmd_base_sii_rtn || nextCommand == T_Cmd.T_Cmd_base_sii_fwd)
        {
            System.out.print("Base SII (1st three components, no trailing sep.): ");
            utl.read(arg1, playback);
        }

        return nextCommand;
    }
}
