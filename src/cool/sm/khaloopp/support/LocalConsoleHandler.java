package cool.sm.khaloopp.support;

import java.util.logging.ConsoleHandler;

public class LocalConsoleHandler extends ConsoleHandler {
    public LocalConsoleHandler() {
        super();
        setOutputStream(System.out);
        setFormatter(new CustomFormatter());
    }
}
