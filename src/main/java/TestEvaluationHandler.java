import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestEvaluationHandler implements CodeHandler {
    private CodeHandler next;

    @Override
    public void setNext(CodeHandler next) {
        this.next = next;
    }

    @Override
    public CodeResponse handle(CodeContext context) {
        // Check Runtime errors in code
        try {
            int failedTestCount = 0;
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
            if (context.action == CodeContext.Action.CHECK) {
                int deducted = Math.min(context.test_case_points, failedTestCount);
                int remainingTestCase = Math.max(0, context.test_case_points - deducted);

                return new CodeResponse(
                        allPassed,
                        new ArrayList<>(resultMap.values()),
                        new ArrayList<>(),
                        context.output,
                        List.of(new PointInfo(
                                String.valueOf(context.syntax_points),
                                String.valueOf(context.runtime_points),
                                String.valueOf(remainingTestCase)
                        ))
                );
            }

            return new CodeResponse(allPassed, new ArrayList<>(resultMap.values()), new ArrayList<>(), context.output);
        } catch (Exception e) {
            return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "");
        }
    }
}
