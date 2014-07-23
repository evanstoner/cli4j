package net.evanstoner.cli4j;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class CommandTest extends TestCase
{



    public CommandTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite( CommandTest.class );
    }

    /**
     * Verify that named arguments are appended.
     */
    public void testSetNamed()
    {
        setNamed("foo");
    }
}
