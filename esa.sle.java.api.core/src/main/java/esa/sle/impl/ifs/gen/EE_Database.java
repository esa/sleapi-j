/**
 * @(#) EE_Database.java
 */

package esa.sle.impl.ifs.gen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;

/**
 * Provides text file handling and tokenising services and integer conversion.
 * Assumes syntax conformant with that described in AIN9904.
 */
public class EE_Database
{
    static private Logger LOG = Logger.getLogger(EE_Database.class.getName());

    /**
     * This is set when the file is opened.
     */
    private FileInputStream file = null;

    private BufferedReader reader = null;

    /**
     * Contains the line number of the last read in line.
     */
    private int lineNumber = 0;

    /**
     * As the derived class requests tokens the file is read line by line. This
     * returns the line number (zero commencing) of the last read line.
     */
    private String currentLine = "";

    /**
     * This is set when parsing encounters an error, and can be retrieved by
     * getCurrentError.
     */
    private String currentError = "";

    private static final char CI_nameValueSeparator = '=';

    private static final char CI_commentKeyword = '#';

    /**
     * Contains the string literal for the symbolic token StartListKeyword.
     * Refer to the Reference Manual for a description of a SLES database
     * grammar.
     */
    private static final char CI_startListKeyword = '{';

    /**
     * Contains the string literal for the symbolic token EndListKeyword. Refer
     * to the Reference Manual for a description of a SLES database grammar.
     */
    private static final char CI_endListKeyword = '}';

    /**
     * Contains the string literal for the BOOLTRUEKeyword. Refer to the SLES
     * Reference Manual for a description of the grammar.
     */
    private static final String C_BOOLTRUEKeyword = "TRUE";

    /**
     * Contains the string literal for the BOOLFALSEKeyword. Refer to the SLES
     * Reference Manual for a description of the grammar.
     */
    private static final String C_BOOLFALSEKeyword = "FALSE";

    public static int C_LengthIntStr = 5;

    public static int C_LengthShortIntStr = 10;

    /**
     * Contains the maximum length of a Database line. Although this is not a
     * formal part of the grammar, the line length must be less than this value
     * characters long. If a line length is entered longer than 2000 characters
     * long, then the correct parsing will not occur. this useful for testing.
     */

    private static int CI_LengthDBLine = 2000;


    public EE_Database()
    {
        this.file = null;
        this.reader = null;
        this.lineNumber = 0;
        this.currentLine = "";
        this.currentError = "";
    }

    private EE_Database(final EE_Database database)
    {
        this.file = database.file;
        this.lineNumber = database.lineNumber;
        this.currentLine = database.currentLine;
        this.currentError = database.currentError;
    }

    public static EE_Database getInstance(final EE_Database database)
    {
        return new EE_Database(database);
    }

    public static EE_Database getInstance()
    {
        return new EE_Database();
    }

    /**
     * @Function Opens the Database
     * @EndFunction
     * @ResultCodes S_OK the database has been opened SLE_E_NOFILE the database
     *              could not be found. E_ACCESSDENIED the process does not have
     *              correct permissions to open the file E_FAIL the open call
     *              failed
     * @EndResultCodes
     */
    public HRESULT open(String filename)
    {
        if (this.file != null)
        {
            this.file = null;
        }
        try
        {

            this.file = new FileInputStream(filename);
            this.reader = new BufferedReader(new InputStreamReader(this.file, "utf-8"));
        }
        catch (FileNotFoundException e)
        {
            return HRESULT.SLE_E_NOFILE;
        }
        catch (SecurityException e)
        {
            return HRESULT.E_ACCESSDENIED;
        }
        catch (UnsupportedEncodingException e)
        {
            LOG.log(Level.FINE, "UnsupportedEncodingException ", e);
            return HRESULT.SLE_E_NOFILE;
        }

        this.lineNumber = 0;
        return HRESULT.S_OK;
    }

    /**
     * Closes the database, and releases all internal references held.
     */
    public void close()
    {
        try
        {
            this.file.close();
            this.reader.close();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
    }

    /**
     * Converts a string into an integral type.
     *
     * @param value a string as a number for example "-12345" or "211".
     * @return
     * @throws SleApiException
     */
    public static int convIntegral(String value) throws SleApiException
    {
        int ret = 0;
        for (int i = 0; i < value.length(); i++)
        {
            if (!Character.isDigit(value.charAt(i)))
            {
                throw new SleApiException(HRESULT.E_FAIL, "the string can not be casted to int");
            }
            else
            {
                ret = ret * 10 + value.charAt(i) - '0';
            }
        }
        return ret;
    }

    /**
     * @throws IOException Reads in the next line of input, and breaks it into a
     *             name/value pair. The meanings of the various enumerators are
     *             as follows: eeGEN_TTinvalidState: this indicates that the
     *             database is not ready to read input. eeGEN_TTeof, this
     *             indicates the end of file has been reached. The name and
     *             value will not be modified. eeGEN_TTeolist,this indicates the
     *             end of a list has been parsed. The name and value will not be
     *             modified. eeGEN_TTsolist, this indicates the start of a list
     *             has been parsed. The name will contain the token assigned to
     *             the start of the list, stripped of trailing and leading
     *             blanks. eeGEN_TTpair , this indicates a pair has been parsed
     *             in, the name will contain any string tokens to the left of
     *             the equals sign stripped of trailing and leading blanks. The
     *             value will contain any string to the right of the equals sign
     *             stripped of trailing and leading blanks. eeGEN_TTsingle ,
     *             this indicates a single entry has been read from a list, ie
     *             no equals sign is present, and the value argument is set to
     *             this entry, which is stripped of trailing and leading
     *             blanks.. eeGEN_TTblankline, this indicates a line with only a
     *             comment or white space, and no tokens has been parsed
     *             eeGEN_TTinvalidFileFormat, this indicates that fie contains
     *             entries not conforming to the syntax given in the description
     *             of the generalised grammar for SLES databases.
     */
    public EEGEN_TokenTypes getNextTokens(EE_Reference<String> name, EE_Reference<String> value)
    {
        boolean thrownException = false;
        this.currentError = "";

        if (this.file == null)
        {
            this.currentError = "File was not opened, error ocurred";
            return EEGEN_TokenTypes.eeGEN_TTinvalidState;
        }
        else
        {
            try
            {
                if (!this.reader.ready())
                {
                    this.currentError = "";
                    return EEGEN_TokenTypes.eeGEN_TTeof;
                }
                else
                {

                    char[] nextLine = new char[CI_LengthDBLine];

                    EE_Reference<StringBuilder> lname = new EE_Reference<>();
                    lname.setReference(new StringBuilder(""));
                    EE_Reference<StringBuilder> lvalue = new EE_Reference<>();
                    lvalue.setReference(new StringBuilder(""));

                    EE_Reference<Integer> curPos = new EE_Reference<Integer>();
                    curPos.setReference(0);

                    String str = this.reader.readLine();
                    if (str != null)
                    {
                        nextLine = str.toCharArray();
                        this.lineNumber++;
                        this.currentLine = String.valueOf(nextLine);
                    }
                    else
                    {
                        this.lineNumber = 0;
                        this.currentLine = "ENDOFFILE";
                    }

                    if (str == null)
                    {
                        return EEGEN_TokenTypes.eeGEN_TTeof;
                    }
                    else
                    {

                        if (parseWhiteSpace(nextLine, curPos.getReference(), curPos))
                        {
                            return EEGEN_TokenTypes.eeGEN_TTblankline;
                        }

                        if (nextLine[curPos.getReference()] == CI_endListKeyword)
                        {
                            curPos.setReference(curPos.getReference() + 1);
                            if (!parseWhiteSpace(nextLine, curPos.getReference(), curPos))
                            {
                                this.currentError = "must be no tokens after end of list keyword";
                                return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                            }
                            else
                            {
                                return EEGEN_TokenTypes.eeGEN_TTeolist;
                            }
                        }
                        else
                        {
                            if (!parseName(nextLine, lname, curPos.getReference(), curPos))
                            {
                                this.currentError = "name token not alphanumeric or *,or :, or _,or .";
                                if (LOG.isLoggable(Level.FINEST))
                                {
                                    LOG.finest(this.currentError);
                                }
                                return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                            }
                        }
                        if (parseWhiteSpace(nextLine, curPos.getReference(), curPos))
                        {
                            name.setReference("");
                            value.setReference(lname.getReference().toString());
                            return EEGEN_TokenTypes.eeGEN_TTsingle;
                        }

                        if (!parseSeparator(nextLine, curPos.getReference(), curPos))
                        {
                            this.currentError = "no separator between name/value pair ";
                            return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                        }
                        // get rid of white space, but don't expect eol here ...

                        if (parseWhiteSpace(nextLine, curPos.getReference(), curPos))
                        {
                            // got a name and separator, eol but no value ...

                            this.currentError = "end of line after separator, require start of list or value";
                            return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                        }

                        if (nextLine[curPos.getReference()] == CI_startListKeyword)
                        {

                            curPos.setReference(curPos.getReference() + 1);
                            if (!parseWhiteSpace(nextLine, curPos.getReference(), curPos))
                            {
                                this.currentError = "no tokens accepted after start list keyword";
                                return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                            }
                            name.setReference(lname.getReference().toString());
                            return EEGEN_TokenTypes.eeGEN_TTsolist;
                        }
                        else
                        {
                            if (!parseValue(nextLine, lvalue, curPos.getReference(), curPos))
                            { // here
                                this.currentError = "no value present after name and separator.";
                                return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                            }
                        }
                        // ok, got everything necessary - expect ws<nl>
                        if (!parseWhiteSpace(nextLine, curPos.getReference(), curPos))
                        {
                            this.currentError = "There is additional information on the line after the value.";
                            return EEGEN_TokenTypes.eeGEN_TTinvalidFileFormat;
                        }
                        name.setReference(lname.getReference().toString());
                        value.setReference(lvalue.getReference().toString());
                        return EEGEN_TokenTypes.eeGEN_TTpair;
                    }
                }
            }
            catch (IOException e)
            {
                thrownException = true;
                LOG.log(Level.FINE, "IOException ", e);
            }
            finally
            {
                if (thrownException)
                {
                    this.currentError = "Reader is not ready";
                    return EEGEN_TokenTypes.eeGEN_TTinvalidState;
                }
            }
        }
        this.currentError = "Something went wrong with the database reading";
        return EEGEN_TokenTypes.eeGEN_TTinvalidState;

    }

    /**
     * Returns the line number inparseWhiteSpace the file of the last read line.
     * Counting starts at 1.
     */
    public int getCurrentLineNumber()
    {
        return this.lineNumber;
    }

    /**
     * Returns a diagnostic string associated with the last error encountered.
     */
    public String getCurrentError()
    {
        return this.currentError;
    }

    public void setCurrentError(String err)
    {
        this.currentError = err;
    }

    /**
     * Returns the last line read in by the database .
     */
    public String getRawLine()
    {
        return this.currentLine;
    }

    /**
     * Used internally to recognise the right hand side of a name value pair.
     * This method is changing the destination and endPos for the client. Source
     * and destination argument string should have the same length. Examples:
     * 
     * @param source Source String. Part of this string will be send to the
     *            destination
     * @param destination Destination String. It is written over the existing
     *            values.
     * @param fromStart the start index to check the source
     * @param endPos end index to check the source
     * @return
     */
    private boolean parseValue(final char[] from,
                               EE_Reference<StringBuilder> to,
                               int fromStart,
                               EE_Reference<Integer> endPos)
    {
        endPos.setReference(fromStart);
        char examine = 0;

        boolean indexOutOfBounds = false;
        try
        {
            examine = from[fromStart];
        }
        catch (IndexOutOfBoundsException e)
        {
            LOG.log(Level.FINE, "IndexOutOfBoundsException ", e);
            indexOutOfBounds = true;
        }
        StringBuilder desRef = new StringBuilder();
        desRef = to.getReference();
        while (indexOutOfBounds == false
               && (Character.isDigit(examine) || Character.isLetter(examine) || examine == '_' || examine == '*'
                   || examine == '.' || examine == ':' || examine == '/' || examine == '-'))
        {
            desRef.append(examine);
            if (endPos.getReference() + 1 < from.length)
            {
                endPos.setReference(endPos.getReference() + 1);
                examine = from[endPos.getReference()];
            }
            else if (endPos.getReference() + 1 == from.length)
            {
                endPos.setReference(endPos.getReference() + 1);
                indexOutOfBounds = true;
            }
        }
        to.setReference(new StringBuilder(desRef.toString()));
        if (endPos.getReference() >= fromStart)
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    private boolean parseName(char[] from, EE_Reference<StringBuilder> to, int fromStart, EE_Reference<Integer> endPos)
    {

        endPos.setReference(fromStart);
        char examine = 0;
        boolean indexOutOfBounds = false;
        try
        {
            examine = from[endPos.getReference()];
        }
        catch (IndexOutOfBoundsException e)
        {
            LOG.log(Level.FINE, "IndexOutOfBoundsException ", e);
            indexOutOfBounds = true;
        }
        StringBuilder desRef = to.getReference();
        while (indexOutOfBounds == false
               && (Character.isDigit(examine) || Character.isLetter(examine) || examine == '_' || examine == '*'
                   || examine == '.' || examine == ':' || examine == '/' || examine == '-'))
        {
            desRef.append(examine);
            to.setReference(new StringBuilder(String.valueOf(desRef)));
            if (endPos.getReference() + 1 < from.length)
            {
                endPos.setReference(endPos.getReference() + 1);
                examine = from[endPos.getReference()];
            }
            else if (endPos.getReference() + 1 == from.length)
            {
                endPos.setReference(endPos.getReference() + 1);
                indexOutOfBounds = true;
            }
        }
        if (endPos.getReference() > fromStart)
        {
            return true;
        }
        return false;

    }

    /**
     * Used internally to recognize the separator between a name/value pair.
     */
    private boolean parseSeparator(char[] from, int startPos, EE_Reference<Integer> endPos)
    {
        endPos.setReference(startPos);
        if (from[endPos.getReference()] == CI_nameValueSeparator)
        {
            endPos.setReference(endPos.getReference() + 1);
            return true;
        }
        else
        {
            return false;
        }
    }

    /** Parses white space and returns true if the end of line is reached */
    private boolean parseWhiteSpace(char[] from, int startpos, EE_Reference<Integer> endPos)
    {

        endPos.setReference(startpos);
        char examine = '?';
        if (endPos.getReference() < from.length)
        {
            examine = from[endPos.getReference()];
            while (examine != '?')
            {
                if (examine == CI_commentKeyword)
                {
                    endPos.setReference(from.length);
                    return true; // eof
                }
                if (Character.isSpaceChar(examine) || examine == '\t' || examine == '\f' || examine == '\r')
                {
                    endPos.setReference(endPos.getReference() + 1);
                    if (endPos.getReference() < from.length)
                    {
                        examine = from[endPos.getReference()];
                        if (examine == '\n')
                        {
                            endPos.setReference(from.length);
                            return true; // eof
                        }
                    }
                    else
                    {
                        return true;
                    }
                }
                else
                {
                    return false;
                }
            }
        }

        return true; // oef

    }

    public static String getcBooltruekeyword()
    {
        return C_BOOLTRUEKeyword;
    }

    public static String getcBoolfalsekeyword()
    {
        return C_BOOLFALSEKeyword;
    }

}
