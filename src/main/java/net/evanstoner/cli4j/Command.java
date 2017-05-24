package net.evanstoner.cli4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeMap;

public abstract class Command {


    public enum ParameterOrder {
        NAMED_THEN_POSITIONAL,
        POSITIONAL_THEN_NAMED
    }

    private ParameterOrder _paramOrder = ParameterOrder.NAMED_THEN_POSITIONAL;

    private String _baseCommand;
    private Command _parentCommmand = null;
    private String _parentCommandGlue = " ";

    private String _longOptsGlue = " ";
    private String _longOptsPrefix = "--";
    private String _shortOptsPrefix = "-";

    private HashMap<String, String> _longOpts = new HashMap<>();

    /**
     * Short opts support options with values or treated as pure flags
     * <p>
     * flags will have a value of `null`
     */
    private HashMap<String, String> _shortOpts = new HashMap<>();

    // Sorted map
    private TreeMap<Integer, String> _positional = new TreeMap<>();


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
     * Set the glue used to combine long option with their value
     * Default: ' ' (space)
     *
     * @param glue The glue string.
     * @return this
     */
    protected Command longOptsGlue(String glue) {
        _longOptsGlue = glue;
        return this;
    }

    /**
     * Set the prefix to place before long options.
     * <p>
     * Defaults to two dashes ("--").
     *
     * @param prefix The prefix string.
     * @return this
     */
    protected Command longOptsPrefix(String prefix) {
        _longOptsPrefix = prefix;
        return this;
    }

    /**
     * Set the prefix to place before short options.
     * <p>
     * Defaults to a dash ("-").
     *
     * @param prefix The prefix string.
     * @return this
     */
    protected Command shortOptsPrefix(String prefix) {
        _shortOptsPrefix = prefix;
        return this;
    }

    /**
     * Set the order of options and arguments when building the command
     *
     * @param order ParameterOrder enum
     * @return this
     */
    protected Command parameterOrder(ParameterOrder order) {
        _paramOrder = order;
        return this;
    }


    /**
     * Set a long option.
     *
     * @param opt   The name of the option as it should appear in the command, but without the prefix.
     * @param value The value of the argument.
     */
    protected void longOption(String opt, String value) {
        _longOpts.put(opt, value);
    }

    /**
     * Set a long option as a flag
     *
     * @param name The name of the argument as it should appear in the command, but without the prefix.
     */
    protected void longOption(String name) {
        longOption(name, null);
    }

    /**
     * Set a short option.
     *
     * @param opt   The name of the option as it should appear in the command, but without the prefix.
     * @param value The value of the argument.
     */
    protected void shortOption(String opt, String value) {
        _shortOpts.put(opt, value);
    }

    /**
     * Set a short option as a flag
     *
     * @param opt The name of the argument as it should appear in the command, but without the prefix.
     */
    protected void shortOption(String opt) {
        shortOption(opt, null);
    }

    /**
     * Set a short option as a flag
     *
     * @param opt The name of the argument as it should appear in the command, but without the prefix.
     */
    protected void shortOption(char opt) {
        shortOption(opt, null);
    }

    protected void shortOption(char opt, String value) {
        shortOption(String.valueOf(opt), value);
    }

    /**
     * Set an argument.
     *
     * @param index The index of the argument.
     * @param value The value of the argument.
     */
    protected void positional(int index, String value) {
        _positional.put(index, value);
    }


    public Result exec() throws IOException, InterruptedException {
        Process p;
        int exitCode;
        String command;

        command = build();

        // TODO use ProcessBuilder

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

        StringBuilder flagsSb = new StringBuilder(_shortOptsPrefix);
        StringBuilder shortSb = new StringBuilder();
        StringBuilder longSb = new StringBuilder();

        StringBuilder positionalSb = new StringBuilder();

        _shortOpts.forEach((key, value) -> {
            if (value == null) {
                flagsSb.append(key);
            } else {
                shortSb.append(_shortOptsPrefix).append(key).append(" ");
                if (!value.trim().isEmpty()) {
                    shortSb.append(value).append(" ");
                }
            }
        });

        // if there are no flags, remove the flags prefix
        if (flagsSb.length() <= _shortOptsPrefix.length()) {
            flagsSb.delete(0, flagsSb.length());
        }

        _longOpts.forEach((key, value) -> {
            if (value == null) {
                longSb.append(_longOptsPrefix).append(key).append(" ");
            } else {
                longSb.append(_longOptsPrefix).append(key).append(_longOptsGlue).append(value).append(" ");
            }
        });

        // _positional is a TreeMap, which has sorted Keys in natural ordering
        for (String arg : _positional.values()) {
            positionalSb.append(arg).append(" ");
        }

        base += _baseCommand;

        if (_paramOrder == ParameterOrder.NAMED_THEN_POSITIONAL) {
            base += emptyOrString(flagsSb) + emptyOrString(shortSb) + emptyOrString(longSb) + emptyOrString(positionalSb);
        } else if (_paramOrder == ParameterOrder.POSITIONAL_THEN_NAMED) {
            base += emptyOrString(positionalSb) + emptyOrString(flagsSb) + emptyOrString(shortSb) + emptyOrString(longSb);
        }

        return base;
    }

    /**
     * Get an empty string or string with prepended space char
     * when building commands
     * <p>
     * Useful when building commands and only want 1 or 0 spaces between an argument
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
