package Entrypoint;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.Locale;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class CountingSyntaxErrors {

    public static void main(String[] args) throws IOException {
        int points = 5;
        System.out.println("Current Question Points for this coding problem: " + points);

        String code = loadCode("SimleCalculator.java");
        String code2 =
                """
                class SimpleCalculator {
                    public static int addTwoNumbers(int num1, int num2){
                        return num1 + num2;
                    }
                                
                }
                """;
        if (code == null) {
            System.out.println("Failed to load the code.");
            return;
        }

        System.out.println("Java code loaded:\n" + code);

        int success = compileCode(code);
        System.out.println(success);

        if (success != 0) {
            int errorCount = success;
            int syntaxPoints = calculatePoints(points, errorCount);
            System.out.println("Total syntax errors: " + errorCount);
            System.out.println("Remaining Question Points after syntax checking: " + syntaxPoints);
        } else {
            System.out.println("Compilation succeeded!");
            System.out.println("Current points: " + points);
            // Run the code against test cases here

        }
    }

    public static String loadCode(String filePath) {
        try {
            Path path = Paths.get("src/main/java/" + filePath);
            if (Files.exists(path)) {
                return Files.readString(path);
            } else {
                System.err.println("File not found: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return null;
    }

    public static int compileCode(String code) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileObject file = new JavaSourceFromString("Tests", code);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, null, diagnostics, null, null, compilationUnits
        );
        boolean no_errors = task.call();

        if (!no_errors) {
            return handleCompilationErrors(diagnostics);
        }
        else {
            return 0;
        }
    }

    public static int handleCompilationErrors(DiagnosticCollector<JavaFileObject> diagnostics) {

        int errorCount = 0;
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                System.out.printf("- Line %d: %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getMessage(Locale.ENGLISH));
                errorCount++;
            }
        }
        return errorCount;
    }

    public static int calculatePoints(int points, int errorCount) {
        return points - errorCount;
    }

    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        public JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
