package net.evanstoner.cli4j.examples.nova;

import net.evanstoner.cli4j.Result;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        NovaCli nova = new NovaCli("user", "passw0rd", "tenant", "http://openstack-server:5000");

        Result r = nova.addFixedIp().server("my-server").networkId("some-uuid").exec();
        if (!r.isSuccessful()) {
            System.out.println("ERROR: " + r.getOutput());
        }
    }
}
