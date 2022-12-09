/**
 * @(#) EE_SLE_LibraryInstance.java
 */
package esa.sle.impl.api.apiut;

import java.util.UUID;

/**
 * This class contains a constant used to identify the default SLE API library instance,
 * when no specific instance ID is specified when the library is used. This name changes 
 * every time a new process is started in order to avoid collisions.
 */
public class EE_SLE_LibraryInstance 
{

	public static final String LIBRARY_INSTANCE_KEY = "default-sle-api-instance-" + UUID.randomUUID().toString();
	
	private EE_SLE_LibraryInstance() 
	{
		throw new IllegalAccessError("This class cannot be instantiated");
	}
}
