package net.evanstoner.cli4j;

public class Result {

    private int _exitCode;
    private String _output;

    public Result(int exitCode, String output) {
        _exitCode = exitCode;
        _output = output;
    }

    public int getExitCode() {
        return _exitCode;
    }

    public String getOutput() {
        return _output;
    }

    public boolean isSuccessful() {
        return _exitCode == 0;
    }

    public boolean hasOutput() {
        return _output != null && _output.length() > 0;
    }
}
