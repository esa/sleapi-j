/**
 * @(#) IFSProblem.java
 */

package esa.sle.impl.ifs.gen;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @NameInfrastructure problem@EndName
 * @ResponsibilityProvides logging of Unexpected internal problems with
 *                         infrastructure.@EndResponsibility
 */
public class IFSProblem
{
    /**
     * Static shared Mutex which is locked at construction and unlocked at
     * destruction. For this reason the IFS_Problem should only be allocated on
     * the stack.
     */
    // private static int shared;

    private final Logger logger = Logger.getLogger(IFSProblem.class.getName());


    @SuppressWarnings("unused")
    private IFSProblem(final IFSProblem right)
    {}

    public IFSProblem()
    {}

    /**
     * @FunctionSends an unsigned integral types to the IFSProblem
     *                implementation defined output stream. The argument can be
     *                cast to an unsigned long by the caller if necessary@EndFunction
     */
    public IFSProblem operatorShiftLeft(long argintegral)
    {
        this.logger.log(Level.INFO, "{0}", argintegral);
        return this;
    }

    /**
     * @FunctionSends a character string to the IFSProblem implementation
     *                defined output stream.@EndFunction
     */
    public IFSProblem deleteMeOperatorShiftLeft(String argstr)
    {
        this.logger.log(Level.INFO, "{0}", argstr);
        return this;
    }

}
