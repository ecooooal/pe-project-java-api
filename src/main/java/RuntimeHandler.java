import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RuntimeHandler implements CodeHandler {
    private CodeHandler next;

    @Override
    public void setNext(CodeHandler next) {
        this.next = next;
    }

    @Override
    public CodeResponse handle(CodeContext context) {
        // Check Runtime errors in code
        try {
            System.out.println("Validating the Complete Solution Code");
            Process run = runCode(context.nameTestUnit);
            int failedTestCount = 0;



            context.output = read(run.getInputStream());
            // if CHECK and runtime errors then deduct from runtime_points and set test case points to 0 then send response
            // KILL IF EXCEEDS 10 SECONDS
            boolean finished = run.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                run.destroyForcibly();
                System.out.println("Runtime timeout.");
                return new CodeResponse(false, List.of(), List.of("Compilation timed out."), "");
            }

            int runStatus = run.exitValue(); // now it's safe to read

            if (runStatus != 0 && context.action == CodeContext.Action.CHECK) {
                int deductedRuntime = Math.min(5, context.runtime_points);
                int remainingRuntime = Math.max(0, context.runtime_points - deductedRuntime);

                return new CodeResponse(
                        false,
                        new ArrayList<>(),
                        List.of("Runtime Error:\n" + context.output),
                        context.output,
                        List.of(new PointInfo(
                                String.valueOf(context.syntax_points),
                                String.valueOf(remainingRuntime),
                                "0"
                        ))
                );
            }
        } catch (Exception e) {
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "");
        }

        // Then call next if exists
        return next != null ? next.handle(context) : null;
    }

    protected String read(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line).append("\n");
        return builder.toString();
    }

    protected Process runCode(String nameTestUnit) throws IOException {
        Process run = new ProcessBuilder(
                "java",
                "-jar", "lib/junit-platform-console-standalone-1.13.1.jar",
                "--class-path", "out",
                "--select-class", nameTestUnit,
                "--reports-dir", "reports")
                .redirectErrorStream(true)
                .start();
        return run;
    }
}
