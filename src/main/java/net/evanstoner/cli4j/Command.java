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
    private ParameterOrder _paramOrder;
    private Command _parentCommmand = null;
    private String _parentCommandGlue = " ";
    private String _namedPrefix = "--";
    private String _flagPrefix = "-";

    private HashMap<String, String> _named = new HashMap<String, String>();
    private HashMap<Integer, String> _positional = new HashMap<Integer, String>();
    private ArrayList<String> _flags = new ArrayList<String>();


    /**
     * Construct a command with the specified base command and no parent.
     * @param baseCommand The executable command (e.g. "nova", "virsh", "vboxmanage").
     */
    protected Command(String baseCommand) {
        this(baseCommand, null);
    }

    /**
     * Construct a command with the specified base command and the specified parent.
     * @param baseCommand The executable command (e.g. "nova", "virsh", "vboxmanage").
     * @param parent The parent command which will be built and prepended to this command before
     *               execution.
     */
    protected Command(String baseCommand, Command parent) {
        _baseCommand = baseCommand;
        _parentCommmand = parent;
    }



    /**
     * Set the glue used to combine the parent command and this command when building. This defaults
     * to a space (" ").
     * @param glue The glue string.
     * @return this
     */
    protected Command parentCommandGlue(String glue) {
        _parentCommandGlue = glue;
        return this;
    }

    /**
     * Set the prefix to place before named arguments. Defaults to two dashes ("--").
     * @param prefix The prefix string.
     * @return this
     */
    protected Command namedPrefix(String prefix) {
        _namedPrefix = prefix;
        return this;
    }

    /**
     * Set the prefix to place before flag arguments. Defaults to a dash ("-").
     * @param prefix The prefix string.
     * @return this
     */
    protected Command flagPrefix(String prefix) {
        _flagPrefix = prefix;
        return this;
    }


    /**
     * Set a named argument.
     * @param name The name of the argument as it should appear in the command, but without the prefix.
     * @param value The value of the argument.
     */
    protected void named(String name, String value) {
        _named.put(name, value);
    }

    /**
     * Set a positional argument.
     * @param index The index of the argument.
     * @param value The value of the argument.
     */
    protected void positional(int index, String value) {
        _positional.put(index, value);
    }

    /**
     *
     * @param name
     */
    protected void flag(String name) {
        _flags.add("name");
    }



    public Result exec() throws IOException, InterruptedException {
        Process p;
        int exitCode;
        StringBuilder outputSb = new StringBuilder();
        String command;

        command = build();

        // we must prepend the parent command's build, if there is one
        if (_parentCommmand != null) {
            command = _parentCommmand.build() + " " + command;
        }

        // execute the command, capturing the exit code and output
        p = Runtime.getRuntime().exec(command);
        exitCode = p.waitFor();

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine())!= null) {
            outputSb.append(line + "\n");
        }

        return new Result(exitCode, outputSb.toString());
    }

    private String build() {
        StringBuilder namedSb = new StringBuilder();
        StringBuilder positionalSb = new StringBuilder();
        List<Integer> positionalIndices;

        for (String name : _named.keySet()) {
            //TODO: missing named arguments
            namedSb.append(_namedPrefix + name + " " + _named.get(name) + " ");
        }

        positionalIndices = new ArrayList<Integer>(_positional.keySet());
        Collections.sort(positionalIndices);

        for (Integer i : positionalIndices) {
            //TODO: missing positional arguments
            positionalSb.append(_positional.get(i) + " ");
        }

        if (_paramOrder == ParameterOrder.NAMED_THEN_POSITIONAL) {
            return namedSb.toString().trim() + " " + positionalSb.toString().trim();
        } else {
            return positionalSb.toString().trim() + " " + namedSb.toString().trim();
        }
    }
}
