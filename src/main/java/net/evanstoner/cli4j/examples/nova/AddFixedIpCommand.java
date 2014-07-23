package net.evanstoner.cli4j.examples.nova;

import net.evanstoner.cli4j.Command;

class AddFixedIpCommand extends Command {

    public AddFixedIpCommand(NovaCli nova) {
        super("add-fixed-ip", nova);
    }

    public AddFixedIpCommand server(String serverNameOrId) {
        positional(0, serverNameOrId);
        return this;
    }

    public AddFixedIpCommand networkId(String networkId) {
        positional(1, networkId);
        return this;
    }

}
