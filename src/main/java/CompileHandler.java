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
        context.debug.add("CompileHandler: Starting CompileHandler.");

        try {
            // Compile code
            context.debug.add("CompileHandler: Starting compilation.");

            Process compileSolution = compileSolutionCode(context.nameCode);
            CodeResponse errorResponse = executeCompilationProcess(
                    compileSolution,
                    "Solution Code",
                    context,
                    true
            );
            if (errorResponse != null) { return errorResponse;}

            // Compile Test Unit
            if (!context.syntax_coding_question_only) {
                context.debug.add("CompileHandler: Question is not Syntax Only, proceeding to compile a TestUnit. " + context.nameTestUnit);
                Process compileTest = compileTestUnit(context.nameTestUnit);
                CodeResponse errorTestUnitResponse = executeCompilationProcess(
                        compileTest,
                        "Test Unit",
                        context,
                        false
                );
            if (errorTestUnitResponse != null) {return errorTestUnitResponse;}

            } else {
                context.debug.add("CompileHandler: No compiled Test Unit because Question is Syntax only.");
            }
        } catch (Exception e) {
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "", context.debug);
        }

        context.debug.add("CompileHandler: 🟢 Compilation Success.");

        if (context.syntax_coding_question_only){
            context.debug.add("CompileHandler: 🟢 Coding Question is Syntax only.");

            return new CodeResponse(
                    true,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "",
                    List.of(new PointInfo(String.valueOf(context.syntax_points), "0", "0")),
                    context.debug
            );
        }

        context.debug.add("CompileHandler: 🟢 Now sending the context to RunTimeHandler.");

        // Then call next if exists
        return next != null ? next.handle(context) : null;
    }

    private CodeResponse executeCompilationProcess(Process process, String processName, CodeContext context, Boolean isSolution) throws IOException {
        context.debug.add("CompileHandler: Compiling " + processName + "... will throw exception if it exceeds 10 seconds.");

        boolean finished;
        try {
            finished = process.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            context.debug.add("CompileHandler: 🔴 " + processName + " Compilation interrupted.");
            process.destroyForcibly();
            return new CodeResponse(false, List.of(), List.of("Compilation interrupted."), "", context.debug);
        }

        if (!finished) {
            context.debug.add("CompileHandler: 🔴 " + processName + " Compilation exceeded 10 seconds and now will be destroyed.");
            process.destroyForcibly();
            return new CodeResponse(
                    false,
                    List.of(),
                    List.of("Compilation timed out for " + processName + ". Exceeded 10 seconds compiling"),
                    "",
                    context.debug
            );
        }
        String compileOutput = read(process.getInputStream());

        // Check the compilation exit status
        int compileStatus = process.exitValue();
        if (compileStatus != 0) {
            context.debug.add("CompileHandler: 🔴 " + processName + " Compilation Unsuccessful (Exit Code: " + compileStatus + ").");

            Set<String> errorLines = new HashSet<>();
            Pattern errorPattern = Pattern.compile("\\.java:(\\d+): error");

            for (String line : compileOutput.split("\n")) {
                Matcher matcher = errorPattern.matcher(line);
                if (matcher.find()) {
                    errorLines.add(matcher.group(1));
                }
            }
            int syntaxPointsToDeduct =  errorLines.size() * context.syntax_points_deduction;
            int deducted = Math.min(context.syntax_points, syntaxPointsToDeduct);
            int remainingSyntax = Math.max(0, context.syntax_points - deducted);

            // Set others to 0 if syntax fails
            int remainingRuntime = 0;
            int remainingTestcase = 0;

            if (isSolution){
                return new CodeResponse(
                        false,
                        new ArrayList<>(),
                        List.of(compileOutput),
                        "",
                        List.of(new PointInfo(String.valueOf(remainingSyntax), String.valueOf(remainingRuntime), String.valueOf(remainingTestcase))),
                        context.debug
                );
            } else if (context.action == CodeContext.Action.COMPILE) {
                context.debug.add("CompileHandler: 🟠 Request is to compile only.");

                return new CodeResponse(
                        false,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "",
                        List.of(new PointInfo(String.valueOf(context.syntax_points), "0", "0")),
                        context.debug
                );
            } else {
                return new CodeResponse(
                        false,
                        List.of(),
                        List.of(compileOutput),
                        "",
                        context.debug
                );
            }
        }
        context.debug.add("CompileHandler: 🟢 " + processName + " Compilation Success.");

        return null;
    }

    protected Process compileSolutionCode(String nameCode) throws IOException {
        Process compile = new ProcessBuilder(
                "javac",
                "-cp", "lib/junit-platform-console-standalone-1.13.1.jar",
                "-d", "out",
                "src/main/java/" + nameCode + ".java")
                .redirectErrorStream(true)
                .start();
        return compile;
    }
    protected Process compileTestUnit(String nameTestUnit) throws IOException {
        Process compile = new ProcessBuilder(
                "javac",
                "-cp", "lib/junit-platform-console-standalone-1.13.1.jar" +
                File.pathSeparator + "out",
                "-d", "out",
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
