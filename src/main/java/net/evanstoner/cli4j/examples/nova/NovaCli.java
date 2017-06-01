package net.evanstoner.cli4j.examples.nova;

import net.evanstoner.cli4j.Command;

public class NovaCli extends Command {

    public NovaCli(String username, String password, String tenantName, String authUrl) {
        super("nova");
        longOption("os-username", username);
        longOption("os-password", password);
        longOption("os-tenant-name", tenantName);
        longOption("os-auth-url", authUrl);
    }

    public AddFixedIpCommand addFixedIp() {
        return new AddFixedIpCommand(this);
    }

}
