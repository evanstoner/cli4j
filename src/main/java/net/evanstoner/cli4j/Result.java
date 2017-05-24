package net.evanstoner.cli4j;

public class Result {

    private int _exitCode;
    private String _output;
    private String _errOutput;

    public Result(int exitCode, String output) {
        this(exitCode, output, null);
    }

    public Result(int exitCode, String output, String errOutput) {
        _exitCode = exitCode;
        _output = output;
        _errOutput = errOutput;
    }

    public int getExitCode() {
        return _exitCode;
    }

    public String getOutput() {
        return _output;
    }

    public String getErrorOutput() {
        return _errOutput;
    }

    public boolean isSuccessful() {
        return _exitCode == 0;
    }

    public boolean hasOutput() {
        return _output != null && _output.length() > 0;
    }

    public boolean hasErrorOutput() {
        return _errOutput != null && _errOutput.length() > 0;
    }
}
