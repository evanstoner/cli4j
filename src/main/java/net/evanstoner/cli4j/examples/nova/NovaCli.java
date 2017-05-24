package net.evanstoner.cli4j.examples.nova;

import net.evanstoner.cli4j.Command;

public class NovaCli extends Command {

    public NovaCli(String username, String password, String tenantName, String authUrl) {
        super("nova");
        named("os-username", username);
        named("os-password", password);
        named("os-tenant-name", tenantName);
        named("os-auth-url", authUrl);
    }

    public AddFixedIpCommand addFixedIp() {
        return new AddFixedIpCommand(this);
    }
}
