# cli4j

[![Build Status](https://travis-ci.org/evanstoner/cli4j.svg?branch=master)](https://travis-ci.org/evanstoner/cli4j)

**A tiny library for turning CLIs into Java APIs.**

cli4j allows you to quickly create a Java API for any command-line tool. It is itself a fluent API and excels at helping you create a fluent API for a command-line tool.

## Example

Say you want to create an API for OpenStack's Nova service (since none of the SDKs you've found have all the features that are available from the command-line -- some of which you need). Start by creating a `NovaCli` class that extends the `Command` abstract class.

```java
import net.evanstoner.cli4j.Command;

public class NovaCli extends Command {

  public NovaCli(String username, String password, String tenantName, String authUrl) {
    super("nova");
    longOption("os-username", username);
    longOption("os-password", password);
    longOption("os-tenant-name", tenantName);
    longOption("os-auth-url", authUrl);
  }
  
}
```

Now you can declare a Nova client like this:

```java
NovaCli nova = new NovaCli("user", "passw0rd", "tenant", "http://openstack-server:5000");
```

To add the `add-fixed-ip` command, you need to create another `Command` subclass:

```java
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

```

Then create a method to get an instance of this command in `NovaCli`:

```java
public AddFixedIpCommand addFixedIp() {
  return new AddFixedIpCommand(this);
}
```

And finally, use it:

```java
Result r = nova.addFixedIp().server("my-server").networkId("some-uuid").exec();
if (!r.isSuccessful()) {
  System.out.println("ERROR: " + r.getErrorOutput());
}
```

You can see this example and more in the [examples package](https://github.com/evanstoner/cli4j/tree/master/src/main/java/net/evanstoner/cli4j/examples).
