package esa.sle.sicf.si.parser.file;

public class Util
{

    protected static String prepareValue(String value)
    {
        if (value == null)
        {
            return null;
        }
        if (value.charAt(0) == '"' && value.charAt(value.length() - 2) == '"')
        {
            value = value.substring(1, value.length() - 2);
        }
        else if (value.charAt(value.length() - 1) == ';')
        {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    protected static String[] splitOnFirst(String line, char c)
    {
        int index = line.indexOf(c);
        if (index == -1)
        {
            return new String[] { line.trim(), null };
        }
        String head = line.substring(0, index);
        String tail = line.substring(index + 1);
        return new String[] { head.trim(), tail.trim() };
    }

}
