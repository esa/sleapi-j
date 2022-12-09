package esa.sle.si.repository;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class EE_SLE_LoggerFormatter extends Formatter
{
    // {0,date},
    private final MessageFormat messageFormat = new MessageFormat("{0} {1} {2}: [T:{4}] \n{3} {5}\n");


    @Override
    public String format(LogRecord record)
    {
        Object[] arguments = new Object[6];
        // arguments[0] = new Date( record.getMillis() );
        arguments[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(new Date());
        arguments[1] = record.getSourceClassName();
        arguments[2] = record.getSourceMethodName();
        arguments[3] = record.getLevel();
        arguments[4] = Long.toString(Thread.currentThread().getId());
        arguments[5] = record.getMessage();

        return this.messageFormat.format(arguments);
    }

}
