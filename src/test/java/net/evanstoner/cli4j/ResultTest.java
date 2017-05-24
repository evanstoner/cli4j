package net.evanstoner.cli4j;


import org.junit.Test;

import static org.junit.Assert.*;

public class ResultTest {


    @Test
    public void givenEmptyOutputWithNullConstructor() {
        Result r = new Result(-1, null);
        assertEquals(-1, r.getExitCode());
        assertFalse(r.hasOutput());
        assertFalse(r.isSuccessful());
        assertEquals(null, r.getOutput());
        assertFalse(r.hasErrorOutput());
    }

    @Test
    public void givenStringWhenNotSuccessful() {
        Result r = new Result(8675309, "mystring");
        assertEquals(8675309, r.getExitCode());
        assertTrue(r.hasOutput());
        assertFalse(r.isSuccessful());
        assertEquals("mystring", r.getOutput());
        assertFalse(r.hasErrorOutput());
    }

    @Test
    public void givenStringWhenSuccessful() {
        Result r = new Result(0, "mystring");
        assertEquals(0, r.getExitCode());
        assertTrue(r.hasOutput());
        assertTrue(r.isSuccessful());
        assertEquals("mystring", r.getOutput());
        assertFalse(r.hasErrorOutput());
    }

    @Test
    public void givenNoOutputOnStringLen0() {
        Result r = new Result(0, "");
        assertEquals(0, r.getExitCode());
        assertFalse(r.hasOutput());
        assertTrue(r.isSuccessful());
        assertEquals("", r.getOutput());
        assertFalse(r.hasErrorOutput());
    }

    @Test
    public void givenErrorOutput() {
        Result r = new Result(25, "", "myerror");
        assertEquals(25, r.getExitCode());
        assertFalse(r.hasOutput());
        assertTrue(r.hasErrorOutput());
        assertFalse(r.isSuccessful());
        assertEquals("", r.getOutput());
        assertEquals("myerror", r.getErrorOutput());
    }
}