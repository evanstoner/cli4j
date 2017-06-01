package net.evanstoner.cli4j;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

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
            String err = (OS.contains("win")) ? "Cannot run program \"kfjndl\": CreateProcess error=2, The system cannot find the file specified"
                    : "Cannot run program \"kfjndl\": error=2, No such file or directory";
            assertEquals(err, e.getMessage());
        }
    }

    @Test
    public void failedCommandErrStreamIsSet() throws IOException, InterruptedException {
        String cmd = (OS.contains("win")) ? "cmd.exe /c dir" : "ls";

        Command ls = new Command(cmd) {
        };


        ls.positional(0, String.valueOf(Paths.get("/path/to/fake/dir")));
        Result r = ls.exec();

        assertTrue(r.hasErrorOutput());
        assertFalse(r.hasOutput());

        assertEquals(1, r.getExitCode());

        String err = (OS.contains("win")) ? "The system cannot find the path specified." : "ls: /path/to/fake/dir: No such file or directory";
        assertEquals(err, r.getErrorOutput());
        assertEquals("", r.getOutput());

        assertEquals(String.format("%s %s", cmd, Paths.get("/path/to/fake/dir").toString()), ls.build());
    }

    @Test
    public void successfulCommandOutputStreamIsSet() throws IOException, InterruptedException {
        String cmd = (OS.contains("win")) ? "cmd.exe /c dir" : "ls";

        Command ls = new Command(cmd) {
        };


        ls.positional(0, ".");
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
        curl.longOption("http2");

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

    @Test
    public void testTreeMapPositionals() {
        int iterations = 100;

        Command echo = new Command("echo") {
        };

        StringBuilder sb = new StringBuilder("echo ");
        for (int i = 0; i < iterations; i++) {
            echo.positional(i, String.valueOf(i));
            sb.append(i).append(" ");
        }

        assertEquals(sb.toString().trim(), echo.build());
    }

    @Test
    public void testGolangProgramParams() {
        Command go = new Command("go") {
        };

        Command run = new Command("run", go) {
        };

        Command golangProg = new Command("myprog.go", run) {
        };

        golangProg.shortOption("raddr", "localhost:8080");
        golangProg.shortOption("delay", "200ms");

        assertEquals("go run myprog.go -delay 200ms -raddr localhost:8080", golangProg.build());
    }

    @Test
    public void testRsyncFlagsWithPaths() {
        Command rsync = new Command("rsync") {
        };

        rsync.shortOption("a");
        rsync.shortOption("z");
        rsync.positional(0, "user@localhost:/var/log/apache2");
        rsync.positional(1, "/home/user/Desktop/");
        assertEquals("rsync -az user@localhost:/var/log/apache2 /home/user/Desktop/", rsync.build());
    }

    @Test
    public void testCommandWithSpacesInDoubleQuotes() {
        Command echo = new Command("echo") {
        };

        echo.positional(0, "\"this is my echo test\"");

        assertEquals("echo \"this is my echo test\"", echo.build());
    }

    @Test
    public void testCommandWithSpacesInSingleQuotes() {
        Command echo = new Command("echo") {
        };

        echo.positional(0, "\'this is my echo test\'");
        assertEquals("echo \'this is my echo test\'", echo.build());
    }

    @Test
    public void testCommandWithPiping() {
        Command ls = new Command("ls") {
        };

        ls.shortOption("l");
        ls.shortOption("ah");
        ls.positional(0, "~");

        Command pipe = new Command("|", ls) {
        };

        Command grep = new Command("grep", pipe) {
        };

        grep.shortOption("nri");
        grep.positional(0, "\'Desktop\'");

        assertEquals("ls -ahl ~ | grep -nri \'Desktop\'", grep.build());
    }

    @Test
    public void testIPtablesBlockIP() {
        Command iptables = new Command("iptables") {
        };

        iptables.shortOption("A", "INPUT");
        iptables.shortOption("s", "\"10.23.32.21\"");
        iptables.shortOption("j", "DROP");
        assertEquals("iptables -A INPUT -s \"10.23.32.21\" -j DROP", iptables.build());
    }

    @Test
    public void parentCommandGlueWithGitFlow() {
        Command git = new Command("git") {
        };

        Command gitFlow = new Command("flow", git) {
        }.parentCommandGlue("-");

        Command gitFlowInit = new Command("init", gitFlow) {
        };


        gitFlowInit.shortOption('d');

        assertEquals("git-flow init -d", gitFlowInit.build());
    }


    class AddFixedIpCommand extends Command {

        AddFixedIpCommand(NovaCli nova) {
            super("add-fixed-ip", nova);
        }

        AddFixedIpCommand server(String serverNameOrId) {
            positional(0, serverNameOrId);
            return this;
        }

        AddFixedIpCommand networkId(String networkId) {
            positional(1, networkId);
            return this;
        }
    }

    class NovaCli extends Command {

        NovaCli(String username, String password, String tenantName, String authUrl) {
            super("nova");
            longOption("os-username", username);
            longOption("os-password", password);
            longOption("os-tenant-name", tenantName);
            longOption("os-auth-url", authUrl);
        }

        AddFixedIpCommand addFixedIp() {
            return new AddFixedIpCommand(this);
        }
    }

    @Test
    public void testNovaLongOption() {
        NovaCli nova = new NovaCli("user", "passw0rd", "tenant", "http://openstack-server:5000");
        String cmd = nova.addFixedIp().server("my-server").networkId("some-uuid").build();

        assertEquals("nova --os-tenant-name tenant --os-username user --os-auth-url " +
                "http://openstack-server:5000 --os-password passw0rd add-fixed-ip my-server some-uuid", cmd);
    }

    @Test
    public void testJavaMemoryConstraints() {
        Command java = new Command("java") {
        };

        java.shortOption("Xms32M", "");
        java.shortOption("Xmx1024M", "");
        java.positional(0, "MyProgram");

        assertEquals("java -Xmx1024M -Xms32M MyProgram", java.build());
    }

    @Test
    public void testJavaMemoryConstraintsPositionalsFirst() {
        Command java = new Command("java") {
        };
        java.parameterOrder(Command.ParameterOrder.POSITIONAL_THEN_NAMED);

        java.shortOptsPrefix("-X");
        java.shortOption("ms32M", "");
        java.shortOption("mx1024M", "");
        java.positional(0, "MyProgram");

        assertEquals("java MyProgram -Xmx1024M -Xms32M", java.build());
    }

    @Test
    public void testCommandWithCustomShortOptsPrefix() {
        Command docker = new Command("docker") {
        };

        Command dockerRun = new Command("run", docker) {
        };

        dockerRun.shortOptsPrefix("#");
        dockerRun.shortOption("it");
        dockerRun.positional(0, "ubuntu");
        dockerRun.positional(1, "/bin/bash");


        assertEquals("docker run #it ubuntu /bin/bash", dockerRun.build());
    }

    @Test
    public void complexCurlHTTPRequest() {

        Command curl = new Command("curl") {
        };

        curl.shortOption("v");
        curl.shortOption('0');
        curl.longOption("compressed");
        curl.shortOption("c", "\"cookiejar.txt\"");
        curl.longOption("cookie", "\"authretries=0\"");
        curl.positional(0, "http://example.com");


        // verbose
        // use HTTP/1.0
        // enable compression
        // write all cookies to 'cookiejar.txt'
        // set request cookie to 'authretries=0'
        assertEquals("curl -0v -c \"cookiejar.txt\" --cookie \"authretries=0\" --compressed http://example.com", curl.build());
    }

    @Test
    public void testCommandWithCustomShortOptsAndCustomLongOptPrefixWithLongGlue() {
        Command docker = new Command("docker") {
        };

        Command dockerRun = new Command("run", docker) {
        };

        dockerRun.shortOptsPrefix("#");
        dockerRun.longOptsPrefix("--D");
        dockerRun.shortOption("it");

        dockerRun.longOptsGlue("=");
        dockerRun.longOption("network", "my_docker_network");

        dockerRun.positional(0, "ubuntu");
        dockerRun.positional(1, "/bin/bash");

        assertEquals("docker run #it --Dnetwork=my_docker_network ubuntu /bin/bash", dockerRun.build());
    }

    class Docker extends Command {
        protected Docker() {
            super("docker");
        }

        public Docker(Docker clone) {
            super(clone);
        }

        public Docker verbose() {
            Docker c = new Docker(this);
            c.shortOption('v');
            return c;
        }

        public Drun run(String file) {
            return new Drun(file, this);
        }
    }

    class Drun extends Command {
        protected Drun(String baseCommand, Command parent) {
            super("run", parent);
            this.positional(0, baseCommand);
        }
    }

    @Test
    public void testReusedCommand() {
        Docker docker = new Docker();
        assertEquals("docker -v", docker.verbose().build());

        String r = docker.run("ubuntu").build();
        assertEquals("docker run ubuntu", r);
    }
}
