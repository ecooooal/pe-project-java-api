import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
public class CodeExecutor {

    enum Action {
        COMPILE, VALIDATE, CHECK;

        public static Action fromString(String value) {
            try {
                return Action.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    String code;
    String testUnit;
    String action;
    Integer syntax_points;
    Integer runtime_points;
    Integer test_case_points;

    protected String nameCode;
    protected String nameTestUnit;
    
    @Override
    public String toString() {
        return String.format("UserCode {\n\tCode='%s', \n\tTest Unit='%s \n} Class Names are {\n\tCode='%s', \n\tTest Unit='%s'\n} ", code, testUnit, nameCode, nameTestUnit );
    }

    public CodeResponse validate(){
        try{
            Action execute_action = Action.fromString(action);

            if (execute_action == null) {
                return new CodeResponse(false, new ArrayList<>(), List.of("Invalid action: " + this.action), "");
            }

            System.out.println("Saving The Codes");
            saveCodes(code, testUnit);
            
            System.out.println("Compiling The Codes");
            Process compile = compileCode(nameCode, nameTestUnit);
            String compileOutput = read(compile.getInputStream());
            compile.waitFor();

            if (compile.waitFor() != 0) {
                // if CHECK then deduct from syntax_points and set runtime and test case points to 0 then send response
                if (execute_action == Action.COMPILE || execute_action == Action.CHECK) {

                    Set<String> errorLines = new HashSet<>();
                    Pattern errorPattern = Pattern.compile("\\.java:(\\d+): error");

                    for (String line : compileOutput.split("\n")) {
                        Matcher matcher = errorPattern.matcher(line);
                        if (matcher.find()) {
                            errorLines.add(matcher.group(1));
                        }
                    }

                    int deducted = Math.min(syntax_points, errorLines.size());
                    int remainingSyntax = Math.max(0, syntax_points - deducted);

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

            if (execute_action == Action.COMPILE){
                return new CodeResponse(
                        false,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        "",
                        List.of(new PointInfo(String.valueOf(syntax_points), "0", "0"))
                );
            } else {
                System.out.println("Validating the Complete Solution Code");
                Process run = runCode(nameTestUnit);
                int failedTestCount = 0;



                String output = read(run.getInputStream());
                // if CHECK and runtime errors then deduct from runtime_points and set test case points to 0 then send response
                int runExit = run.waitFor();

                if (runExit != 0 && execute_action == Action.CHECK) {
                    int deductedRuntime = Math.min(5, runtime_points);
                    int remainingRuntime = Math.max(0, runtime_points - deductedRuntime);

                    return new CodeResponse(
                            false,
                            new ArrayList<>(),
                            List.of("Runtime Error:\n" + output),
                            output,
                            List.of(new PointInfo(
                                    String.valueOf(syntax_points),
                                    String.valueOf(remainingRuntime),
                                    "0"
                            ))
                    );
                }

                File reportDir = new File("reports");
                File[] xmlFiles = reportDir.listFiles((dir, name) -> name.endsWith(".xml"));

                Map<String, CodeResponse.TestResult> resultMap = new LinkedHashMap<>();

                if (xmlFiles != null) {
                    for (File xml : xmlFiles) {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(xml);

                        NodeList testCases = doc.getElementsByTagName("testcase");
                        for (int i = 0; i < testCases.getLength(); i++) {
                            Element testCase = (Element) testCases.item(i);

                            String className = testCase.getAttribute("classname");
                            String methodName = testCase.getAttribute("name");

                            String status = "PASSED";
                            String message = "";

                            NodeList failureNodes = testCase.getElementsByTagName("failure");
                            if (failureNodes.getLength() > 0) {
                                status = "FAILED";
                                failedTestCount++;
                                Element failure = (Element) failureNodes.item(0);
                                message = failure.getAttribute("message");
                                if (message == null || message.isEmpty()) {
                                    message = failure.getTextContent();
                                }
                            }

                            resultMap.putIfAbsent(className, new CodeResponse.TestResult(className));
                            resultMap.get(className).addMethod(methodName, status, message.trim());
                        }
                    }
                }


                boolean allPassed = resultMap.values().stream()
                        .flatMap(r -> r.methods.stream())
                        .allMatch(m -> m.status.equals("PASSED"));

                // If CHECK then for every FAILED test case deduct from test_case_points
                if (execute_action == Action.CHECK) {
                    int deducted = Math.min(test_case_points, failedTestCount);
                    int remainingTestCase = Math.max(0, test_case_points - deducted);

                    return new CodeResponse(
                            allPassed,
                            new ArrayList<>(resultMap.values()),
                            new ArrayList<>(),
                            output,
                            List.of(new PointInfo(
                                    String.valueOf(syntax_points),
                                    String.valueOf(runtime_points),
                                    String.valueOf(remainingTestCase)
                            ))
                    );
                }

                return new CodeResponse(allPassed, new ArrayList<>(resultMap.values()), new ArrayList<>(), output);
            }

        } catch (Exception e) {
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "");
        }
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

    protected String read(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line).append("\n");
        return builder.toString();
    }

    protected void saveCodes(String code, String testUnit){
        String solutionPath = "src/main/java/";
        String testUnitPath = "src/test/java/";

        System.out.println("Saving Complete Solution");
        try (FileWriter writer = new FileWriter(solutionPath + this.nameCode + ".java")) {
            writer.write(code);
            System.out.println("Complete Solution is saved.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Saving Test Unit");
        try (FileWriter writer = new FileWriter(testUnitPath + this.nameTestUnit + ".java")) {
            writer.write(testUnit);
            System.out.println("Test Unit is saved.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String getClassName(String code){
        Pattern pattern = Pattern.compile("\\bclass\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        String className = "tmp";

        if (matcher.find()) {
            className = matcher.group(1);
        }

        return className;
    }

    public void assignClassNames(){
        if (syntax_points == null) syntax_points = 0;
        if (runtime_points == null) runtime_points = 0;
        if (test_case_points == null) test_case_points = 0;

        System.out.println("Extracting from code: " + code);
        this.nameCode = getClassName(this.code);
        System.out.println("Extracted nameCode: " + nameCode);

        this.nameTestUnit = getClassName(this.testUnit);
        System.out.println("Extracted nameTestUnit: " + nameTestUnit);
    }

}
