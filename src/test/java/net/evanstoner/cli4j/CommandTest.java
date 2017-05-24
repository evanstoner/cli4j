package net.evanstoner.cli4j;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class CommandTest {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    @Test
    public void fakeCommandThrowsIOException() throws IOException, InterruptedException {
        try {
            new Command("kfjndl") {
            }.exec();
            fail("exception not caught");
        } catch (IOException e) {
            assertEquals("Cannot run program \"kfjndl\": error=2, No such file or directory", e.getMessage());
        }
    }

    @Test
    public void failedCommandErrStreamIsSet() throws IOException, InterruptedException {
        String cmd = (OS.contains("win")) ? "dir" : "ls";

        Command ls = new Command(cmd) {
        };


        ls.positional(0, "/path/to/fake/dir");
        Result r = ls.exec();

        assertTrue(r.hasErrorOutput());
        assertFalse(r.hasOutput());

        assertEquals(1, r.getExitCode());

        assertEquals("ls: /path/to/fake/dir: No such file or directory" + System.lineSeparator(), r.getErrorOutput());
        assertEquals("", r.getOutput());

        assertEquals(String.format("%s /path/to/fake/dir", cmd), ls.build());
    }

    @Test
    public void testGolangProgramParams() {
        Command go = new Command("go") {
        };

        Command run = new Command("run", go) {
        };

        Command golangProg = new Command("myprog.go", run) {
        };

        // temporarily use namedprefix until new changes
        golangProg.namedPrefix("-");
        golangProg.named("raddr", "localhost:8080");
        golangProg.named("delay", "200ms");

        assertEquals("go run myprog.go -delay 200ms -raddr localhost:8080", golangProg.build());
    }

    @Test
    public void successfulCommandOutputStreamIsSet() throws IOException, InterruptedException {
        String cmd = (OS.contains("win")) ? "dir" : "ls";

        Command ls = new Command(cmd) {
        };


        ls.positional(0, "./");
        Result r = ls.exec();

        assertFalse(r.hasErrorOutput());
        assertTrue(r.hasOutput());

        assertEquals(0, r.getExitCode());

        assertEquals("", r.getErrorOutput());
        assertTrue(r.getOutput().contains("src" + System.lineSeparator()));
    }

    @Test
    public void positionalFirstThenNamedArgs() throws IOException, InterruptedException {
        Command curl = new Command("curl") {
        };


        curl.parameterOrder(Command.ParameterOrder.POSITIONAL_THEN_NAMED);
        curl.positional(0, "https://www.google.com");
        curl.named("http2", "");

        assertEquals("curl https://www.google.com --http2", curl.build());
    }

    @Test
    public void emptyStringOnNullStringBuilder() {
        assertEquals("", new Command("") {
        }.emptyOrString(null));
    }

    @Test
    public void emptyStringOnEmptyStringBuilder() {
        assertEquals("", new Command("") {
        }.emptyOrString(new StringBuilder("")));
    }

    @Test
    public void prependedStringOnNonEmptyStringBuilder() {
        assertEquals(" my data", new Command("") {
        }.emptyOrString(new StringBuilder("my data")));
    }
}
