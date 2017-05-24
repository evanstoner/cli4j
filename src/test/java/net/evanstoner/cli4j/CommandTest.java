package net.evanstoner.cli4j;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class CommandTest {


    @Test
    public void testRsyncFlagsWithPaths() {
        Command rsync = new Command("rsync") {
        };

        rsync.flag("a");
        rsync.flag("z");
        rsync.positional(0, "user@localhost:/var/log/apache2");
        rsync.positional(1, "/home/user/Desktop/");
        assertEquals("rsync -az user@localhost:/var/log/apache2 /home/user/Desktop/", rsync.build());
    }

    @Test
    public void testGolangProgramParams() {
        Command go = new Command("go") {
        };

        Command run = new Command("run", go) {
        };

        Command golangProg = new Command("myprog.go", run) {
        };

        golangProg.flag("raddr", "localhost:8080");
        golangProg.flag("delay", "200ms");

        assertEquals("go run myprog.go -delay 200ms -raddr localhost:8080", golangProg.build());
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

        //
        ls.flag("l");
        ls.flag("ah");
        ls.positional(0, "~");

        Command pipe = new Command("|", ls) {
        };

        Command grep = new Command("grep", pipe) {
        };

        grep.flag("nri");
        grep.positional(0, "\'Desktop\'");

        assertEquals("ls -lah ~ | grep -nri \'Desktop\'", grep.build());
    }

    @Test
    public void testIPtablesBlockIP() {
        Command iptables = new Command("iptables") {
        };

        iptables.flag("A", "INPUT");
        iptables.flag("s", "\"10.23.32.21\"");
        iptables.flag("j", "DROP");
        assertEquals("iptables -A INPUT -s \"10.23.32.21\" -j DROP", iptables.build());
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
            named("os-username", username);
            named("os-password", password);
            named("os-tenant-name", tenantName);
            named("os-auth-url", authUrl);
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

        java.flag("Xms32M", "");
        java.flag("Xmx1024M", "");
        java.positional(0, "MyProgram");

        assertEquals("java -Xmx1024M -Xms32M MyProgram", java.build());
    }

    @Test
    public void testJavaMemoryConstraintsPositionalsFirst() {
        Command java = new Command("java") {
        };
        java.parameterOrder(Command.ParameterOrder.POSITIONAL_THEN_NAMED);

        java.flagPrefix("-X");
        java.flag("ms32M", "");
        java.flag("mx1024M", "");
        java.positional(0, "MyProgram");

        assertEquals("java MyProgram -Xmx1024M -Xms32M", java.build());
    }

    @Test
    public void testCommandWithCustomShortOptsPrefix() {
        Command docker = new Command("docker") {
        };

        Command dockerRun = new Command("run", docker) {
        };

        dockerRun.flagPrefix("#");
        dockerRun.flag("it");
        dockerRun.positional(0, "ubuntu");
        dockerRun.positional(1, "/bin/bash");


        assertEquals("docker run #it ubuntu /bin/bash", dockerRun.build());
    }

    @Test
    public void testCommandWithCustomShortOptsAndCustomLongOptPrefixWithLongGlue() {
        Command docker = new Command("docker") {
        };

        Command dockerRun = new Command("run", docker) {
        };

        dockerRun.flagPrefix("#");
        dockerRun.namedPrefix("--D");
        dockerRun.flag("it");

        dockerRun.namedGlue("=");
        dockerRun.named("network", "my_docker_network");

        dockerRun.positional(0, "ubuntu");
        dockerRun.positional(1, "/bin/bash");

        assertEquals("docker run #it --Dnetwork=my_docker_network ubuntu /bin/bash", dockerRun.build());
    }
}
