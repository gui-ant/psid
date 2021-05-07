package common;

import java.io.PrintStream;

public class SysOutLogger extends CustomLogger {
    @Override
    protected PrintStream getLogComponent() {
        return System.out;
    }

    @Override
    protected void writeToComponent(String text) {
        getLogComponent().println(text);
    }
}
