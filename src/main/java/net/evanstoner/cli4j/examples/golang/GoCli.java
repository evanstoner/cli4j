package net.evanstoner.cli4j.examples.golang;

import net.evanstoner.cli4j.Command;
import net.evanstoner.cli4j.Result;

import java.io.IOException;
import java.security.InvalidParameterException;

public class GoCli extends Command {
    public GoCli() {
        super("go");
    }

    public Result version() throws IOException, InterruptedException {
        positional(0, "version");
        return this.exec();
    }

    public Result env() throws IOException, InterruptedException {
        positional(0, "env");
        return this.exec();
    }

    public GoBuild build() {
        return new GoBuild(this);
    }

    public GoRun run(String programFile) {
        positional(0, "run");
        return new GoRun(programFile, this);
    }

    public class GoBuild extends Command {
        public GoBuild(GoCli go) {
            super("build", go);
        }

        public GoBuild dir(String dir) {
            positional(0, dir);
            return this;
        }
    }

    public class GoRun extends Command {
        int pos = 1;

        public GoRun(String program, GoCli go) {
            super(program, go);
        }

        public GoRun args(String... args) {
            for (String arg : args) {
                positional(pos++, arg);
            }
            return this;
        }

        /**
         * Key Value pair program args
         * args must be even
         *
         * @param args
         * @return
         */
        public GoRun flags(String... args) {
            if (args.length % 2 != 0) {
                throw new InvalidParameterException("given args are not even");
            }

            for (int i = 0; i < args.length; i += 2) {
                flag(args[i], args[i + 1]);
            }
            return this;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GoCli go = new GoCli();

        System.out.println("Go version: " + go.version().getOutput());
        System.out.println("Go env: " + go.env().getOutput());

        Result res = go.run("main.go")
                .flags("delay", "200ms",
                        "routines", "5")
                .args("https://www.google.com", "https://www.youtube.com")
                .exec();

        if (!res.isSuccessful()) {
            System.err.println("ERROR: " + res.getErrorOutput());
        } else {
            System.out.println(res.getOutput());
        }
    }
}
