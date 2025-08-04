import java.io.File;
import java.util.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CodeChecker extends CodeValidator{
    int points;

    @Override
    public CodeResponse validate() {
        try{
            System.out.println("Writing Test Unit");
            super.saveCodes(completeSolution, testUnit);


            System.out.println("Validating the Complete Solution Code");

            Process compile = new ProcessBuilder(
                    "javac",
                    "-cp", "lib/junit-platform-console-standalone-1.13.1.jar",
                    "-d", "out",
                    "src/main/java/" + nameCompleteSolution + ".java",
                    "src/test/java/" + nameTestUnit + ".java")
                    .redirectErrorStream(true)
                    .start();

            String compileOutput = super.read(compile.getInputStream());
            compile.waitFor();
            if (!compileOutput.isBlank()) {
                return new CodeResponse(false, new ArrayList<>(), List.of(compileOutput), "");
            }

            Process run = new ProcessBuilder(
                    "java",
                    "-jar", "lib/junit-platform-console-standalone-1.13.1.jar",
                    "--class-path", "out",
                    "--scan-class-path",
                    "--reports-dir", "reports")
                    .redirectErrorStream(true)
                    .start();

            String output = read(run.getInputStream());
            run.waitFor();

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

            return new CodeResponse(allPassed, new ArrayList<>(resultMap.values()), new ArrayList<>(), output);

        } catch (Exception e) {
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "");
        }
    }
}
