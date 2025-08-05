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
        context.debug.add("RunTimeHandler: RunTimeHandler Starting.");

        // Check Runtime errors in code
        try {
            deleteDirectory(new File("reports"));

            context.debug.add("RunTimeHandler: Running the code against the test code.");
            Process run = runCode(context.nameTestUnit);

            context.debug.add("RunTimeHandler: Code is running... will threw exception if it exceeds 10 seconds.");
            context.output = read(run.getInputStream());
            // if CHECK and runtime errors then deduct from runtime_points and set test case points to 0 then send response
            // KILL IF EXCEEDS 10 SECONDS
            boolean finished = run.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                context.debug.add("RunTimeHandler: Runtime exceeded 10 seconds and now will be destroyed.");
                run.destroyForcibly();
                return new CodeResponse(false, List.of(), List.of("Runtime timed out."), "", context.debug);
            }
        } catch (Exception e) {
            context.debug.add("RunTimeHandler: try code block threw exception.");
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "", context.debug);
        }

        context.debug.add("RunTimeHandler: Running code did not throw any exceptions.");

        context.debug.add("RunTimeHandler: Now sending the context to TestEvaluationHandler.");

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

    public static boolean deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return false;
        }

        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirectory(child)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete(); // delete file or empty dir
    }
}

