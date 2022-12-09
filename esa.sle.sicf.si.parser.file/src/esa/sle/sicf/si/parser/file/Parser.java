package esa.sle.sicf.si.parser.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import esa.sle.impl.api.apiut.EE_SLE_Time;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class Parser
{

    private static final Logger LOG = Logger.getLogger(Parser.class.getName());

    private final ServiceInstanceContainer siContainer;

    private final String inputFile;

    private BufferedReader br = null;


    public Parser(String inputFile)
    {
        this.inputFile = inputFile;
        this.siContainer = new ServiceInstanceContainer();
    }

    public void parseFile()
    {

        try
        {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(this.inputFile), "UTF8");
            this.br = new BufferedReader(isr);
            String line;
            while ((line = this.br.readLine()) != null)
            {
                processLine(line);
            }
        }
        catch (FileNotFoundException e)
        {
            LOG.log(Level.FINE, "FileNotFoundException ", e);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
        finally
        {
            try
            {
                this.br.close();
            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
            }
        }
    }

    private void processLine(String line) throws IOException, SleApiException
    {

        processHeader(line);
        processSpecificServiceInstances(line);

    }

    private void processHeader(String line)
    {
        String[] str = Util.splitOnFirst(line, '=');
        String val = Util.prepareValue(str[1]);
        if (str[0].equalsIgnoreCase("description"))
        {
            this.siContainer.setDescription(val);
        }
        if (str[0].equalsIgnoreCase("requester_name"))
        {
            this.siContainer.setRequesterName(val);
        }
        if (str[0].equalsIgnoreCase("creation_date"))
        {
            ISLE_Time creationDate = new EE_SLE_Time();
            try
            {
                creationDate.setDateAndTime(val);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
            this.siContainer.setCreationDate(creationDate);
        }
        if (str[0].equalsIgnoreCase("version"))
        {
            this.siContainer.setVersion(Integer.parseInt(val));
        }

    }

    private void processSpecificServiceInstances(String line)
    {

        String[] str = Util.splitOnFirst(line, '=');
        if (str.length == 2 && str[0] != null && str[1] != null)
        {
            String value = new String(str[1]);
            value = value.toLowerCase();
            if (value.contains("f-cltu-ts"))
            {
                CltuSIParser cltuSIParser = new CltuSIParser(this.br);
                SIDescriptor si = cltuSIParser.createSIDescription();
                si = cltuSIParser.fillDataFromFile();
                si.setVersion(this.siContainer.getVersion());
                this.siContainer.addElementToList(si);
            }

            if (value.contains("f-sp-ts"))
            {
                FspSIParser fspSIParser = new FspSIParser(this.br);
                SIDescriptor si = fspSIParser.createSIDescription();
                si = fspSIParser.fillDataFromFile();
                si.setVersion(this.siContainer.getVersion());
                this.siContainer.addElementToList(si);
            }

            if (value.contains("r-af-ts"))
            {
                RafSIParser rafSIParser = new RafSIParser(this.br);
                SIDescriptor si = rafSIParser.createSIDescription();
                si = rafSIParser.fillDataFromFile();
                si.setVersion(this.siContainer.getVersion());
                this.siContainer.addElementToList(si);
            }

            if (value.contains("r-cf-ts"))
            {
                RcfSIParser rcfSIParser = new RcfSIParser(this.br);
                SIDescriptor si = rcfSIParser.createSIDescription();
                si = rcfSIParser.fillDataFromFile();
                si.setVersion(this.siContainer.getVersion());
                this.siContainer.addElementToList(si);
            }

            if (value.contains("r-ocf-ts"))
            {
                RocfSIParser rocfSIParser = new RocfSIParser(this.br);
                SIDescriptor si = rocfSIParser.createSIDescription();
                si = rocfSIParser.fillDataFromFile();
                si.setVersion(this.siContainer.getVersion());
                this.siContainer.addElementToList(si);
            }
        }

    }

    public ServiceInstanceContainer getServInstanceContainer()
    {
        return this.siContainer;
    }

}
