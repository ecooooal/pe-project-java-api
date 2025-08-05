import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompileHandler implements CodeHandler {
    private CodeHandler next;

    @Override
    public void setNext(CodeHandler next) {
        this.next = next;
    }

    @Override
    public CodeResponse handle(CodeContext context) {
        try {
            // Compile code
            Process compile = compileCode(context.nameCode, context.nameTestUnit);
            String compileOutput = read(compile.getInputStream());
            // KILL IF EXCEEDS 10 SECONDS
            boolean finished = compile.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                compile.destroyForcibly(); 
                System.out.println("Compilation timeout.");
                return new CodeResponse(false, List.of(), List.of("Compilation timed out."), "");
            }

            int compileStatus = compile.exitValue(); // now it's safe to read

            if (compileStatus  != 0) {
                // if CHECK then deduct from syntax_points and set runtime and test case points to 0 then send response
                if (context.action == CodeContext.Action.COMPILE || context.action == CodeContext.Action.CHECK) {

                    Set<String> errorLines = new HashSet<>();
                    Pattern errorPattern = Pattern.compile("\\.java:(\\d+): error");

                    for (String line : compileOutput.split("\n")) {
                        Matcher matcher = errorPattern.matcher(line);
                        if (matcher.find()) {
                            errorLines.add(matcher.group(1));
                        }
                    }

                    int deducted = Math.min(context.syntax_points, errorLines.size());
                    int remainingSyntax = Math.max(0, context.syntax_points - deducted);

                    // Set others to 0 if syntax fails
                    int remainingRuntime = 0;
                    int remainingTestcase = 0;

                    CodeResponse response = new CodeResponse(
                            false,
                            new ArrayList<>(),
                            List.of(compileOutput),
                            "",
                            List.of(new PointInfo(String.valueOf(remainingSyntax), String.valueOf(remainingRuntime), String.valueOf(remainingTestcase)))
                    );

                    return response;

                } else {
                    System.out.println("Compiling returns Blank");
                    return new CodeResponse(false, new ArrayList<>(), List.of(compileOutput), "");
                }
            }

            if (context.action == CodeContext.Action.COMPILE) {
                return new CodeResponse(
                        false,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "",
                        List.of(new PointInfo(String.valueOf(context.syntax_points), "0", "0"))
                );
            }
        } catch (Exception e) {
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "");
        }

        // Then call next if exists
        return next != null ? next.handle(context) : null;
    }

    protected Process compileCode(String nameCode, String nameTestUnit) throws IOException {
        Process compile = new ProcessBuilder(
                "javac",
                "-cp", "lib/junit-platform-console-standalone-1.13.1.jar",
                "-d", "out",
                "src/main/java/" + nameCode + ".java",
                "src/test/java/" + nameTestUnit + ".java")
                .redirectErrorStream(true)
                .start();
        return compile;
    }

    protected String read(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line).append("\n");
        return builder.toString();
    }
}
