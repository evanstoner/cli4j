package net.evanstoner.cli4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class Command {

    public static enum ParameterOrder {
        NAMED_THEN_POSITIONAL,
        POSITIONAL_THEN_NAMED
    }

    private String _baseCommand;
    private ParameterOrder _paramOrder = ParameterOrder.NAMED_THEN_POSITIONAL;
    private Command _parentCommmand = null;
    private String _parentCommandGlue = " ";
    private String _namedPrefix = "--";
    private String _flagPrefix = "-";

    private HashMap<String, String> _named = new HashMap<String, String>();
    private HashMap<Integer, String> _positional = new HashMap<Integer, String>();
    private ArrayList<String> _flags = new ArrayList<String>();


    /**
     * Construct a command with the specified base command and no parent.
     *
     * @param baseCommand The executable command (e.g. "nova", "virsh", "vboxmanage").
     */
    protected Command(String baseCommand) {
        this(baseCommand, null);
    }

    /**
     * Construct a command with the specified base command and the specified parent.
     *
     * @param baseCommand The executable command (e.g. "nova", "virsh", "vboxmanage").
     * @param parent      The parent command which will be built and prepended to this command before
     *                    execution.
     */
    protected Command(String baseCommand, Command parent) {
        _baseCommand = baseCommand;
        _parentCommmand = parent;
    }


    /**
     * Set the glue used to combine the parent command and this command when building. This defaults
     * to a space (" ").
     *
     * @param glue The glue string.
     * @return this
     */
    protected Command parentCommandGlue(String glue) {
        _parentCommandGlue = glue;
        return this;
    }

    /**
     * Set the prefix to place before named arguments. Defaults to two dashes ("--").
     *
     * @param prefix The prefix string.
     * @return this
     */
    protected Command namedPrefix(String prefix) {
        _namedPrefix = prefix;
        return this;
    }

    /**
     * Set the prefix to place before flag arguments. Defaults to a dash ("-").
     *
     * @param prefix The prefix string.
     * @return this
     */
    protected Command flagPrefix(String prefix) {
        _flagPrefix = prefix;
        return this;
    }

    protected Command parameterOrder(ParameterOrder order) {
        _paramOrder = order;
        return this;
    }


    /**
     * Set a named argument.
     *
     * @param name  The name of the argument as it should appear in the command, but without the prefix.
     * @param value The value of the argument.
     */
    protected void named(String name, String value) {
        _named.put(name, value);
    }

    /**
     * Set a positional argument.
     *
     * @param index The index of the argument.
     * @param value The value of the argument.
     */
    protected void positional(int index, String value) {
        _positional.put(index, value);
    }

    /**
     * @param name
     */
    protected void flag(String name) {
        _flags.add(name);
    }


    public Result exec() throws IOException, InterruptedException {
        Process p;
        int exitCode;
        String command;

        command = build();

        // execute the command, capturing the exit code and output
        p = Runtime.getRuntime().exec(command);
        exitCode = p.waitFor();

        StringBuilder outputSb = new StringBuilder();
        StringBuilder errSb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            outputSb.append(line).append(System.lineSeparator());
        }

        br.close();
        br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = br.readLine()) != null) {
            errSb.append(line).append(System.lineSeparator());
        }
        br.close();

        return new Result(exitCode, outputSb.toString(), errSb.toString());
    }

    String build() {

        String base = "";

        // Moved _parentCommmand processing to build() to be able to test commands when
        // chaining with other extended Command objects

        // we must prepend the parent command's build, if there is one
        if (_parentCommmand != null) {
            base = _parentCommmand.build() + _parentCommandGlue;
        }

        StringBuilder namedSb = new StringBuilder();
        StringBuilder positionalSb = new StringBuilder();
        List<Integer> positionalIndices;

        for (String name : _named.keySet()) {
            //TODO: missing named arguments
            namedSb.append(_namedPrefix + name + " " + _named.get(name) + " ");
        }

        positionalIndices = new ArrayList<>(_positional.keySet());
        Collections.sort(positionalIndices);

        for (Integer i : positionalIndices) {
            //TODO: missing positional arguments
            positionalSb.append(_positional.get(i) + " ");
        }

        base += _baseCommand;

        if (_paramOrder == ParameterOrder.NAMED_THEN_POSITIONAL) {
            base += emptyOrString(namedSb) + emptyOrString(positionalSb);
        } else if (_paramOrder == ParameterOrder.POSITIONAL_THEN_NAMED) {
            base += emptyOrString(positionalSb) + emptyOrString(namedSb);
        }

        return base;
    }

    /**
     * Get an empty string or string with prepended space char
     * when building commands
     *
     * @param sb string builder to check if empty
     * @return empty string if string build is empty,
     * prepended string with space char is not empty
     */
    String emptyOrString(StringBuilder sb) {
        if (sb != null && sb.length() > 0) {
            return " " + sb.toString().trim();
        }

        return "";
    }
}
