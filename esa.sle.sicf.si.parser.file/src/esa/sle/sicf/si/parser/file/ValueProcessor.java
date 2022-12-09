package esa.sle.sicf.si.parser.file;

import esa.sle.sicf.si.descriptors.SIDescriptor;

@FunctionalInterface
public interface ValueProcessor
{

    public boolean processValue(String value, SIDescriptor descriptor);

}
