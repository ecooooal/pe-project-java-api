    import org.w3c.dom.Document;
    import org.w3c.dom.Element;
    import org.w3c.dom.Node;
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
            context.debug.add("TestEvaluationHandler: TestEvaluationHandler Starting.");
            // Check Runtime errors in code
            try {
                context.debug.add("TestEvaluationHandler: Starting to read xml JUnit reports.");
                int failedTestCount = 0;
                int runtimeErrorCount = 0;
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

                            TestStatus status = TestStatus.PASSED;
                            String message = "";


                            NodeList children = testCase.getChildNodes();
                            boolean hasFailureOrError = false;

                            context.debug.add("TestEvaluationHandler: " + xml.getName() + " : Reading class " + className + " with method " + methodName + " children.");
                            for (int j = 0; j < children.getLength(); j++) {
                                Node node = children.item(j);
                                if (node.getNodeType() == Node.ELEMENT_NODE) {
                                    Element child = (Element) node;
                                    String tagName = child.getTagName();

                                    if ("failure".equals(tagName)) {
                                        status = TestStatus.FAILED;
                                        failedTestCount++;
                                        message = child.getAttribute("message");
                                        if (message == null || message.isEmpty()) {
                                            message = child.getTextContent();
                                        }
                                        hasFailureOrError = true;
                                        break;
                                    } else if ("error".equals(tagName)) {
                                        status = TestStatus.ERROR;
                                        failedTestCount++;
                                        runtimeErrorCount++;
                                        message = child.getAttribute("message");
                                        if (message == null || message.isEmpty()) {
                                            message = child.getTextContent();
                                        }
                                        hasFailureOrError = true;
                                        break;
                                    }
                                    context.debug.add("TestEvaluationHandler: " + xml.getName() + " : Reading class " + className + " with method " + methodName + " child data: " + " Name " + child.getNodeName() + " status " + status);
                                }
                            }

                            if (!hasFailureOrError) {
                                status = TestStatus.PASSED;
                            }


                            resultMap.putIfAbsent(className, new CodeResponse.TestResult(className));
                            resultMap.get(className).addMethod(methodName, status, message.trim());
                        }
                    }
                }
                context.debug.add("TestEvaluationHandler: READING RESULTS");

                resultMap.values().stream()
                        .flatMap(r -> r.methods.stream())
                        .forEach(m -> context.debug.add("method: " + m.methodName + " (status: " + m.status + ")"));

                boolean allPassed = !resultMap.isEmpty() && resultMap.values().stream()
                        .flatMap(r -> r.methods.stream())
                        .allMatch(m -> m.status == TestStatus.PASSED);

                context.debug.add("TestEvaluationHandler: Did all test cases passed? " + allPassed);

                // If CHECK then for every FAILED test case deduct from test_case_points
                if (context.action == CodeContext.Action.CHECK) {

                    int remainingTestCase = 0;
                    if (runtimeErrorCount == 0){
                        int deductedTestCasePoints = Math.min(context.test_case_points, failedTestCount);
                        remainingTestCase = Math.max(0, context.test_case_points - deductedTestCasePoints);
                    }
                    int deductedRunTimePoints = Math.min(context.runtime_points, runtimeErrorCount);
                    int remainingRunTimePoints = Math.max(0, context.runtime_points - deductedRunTimePoints);

                    context.debug.add("CodeResponse: success=" + allPassed +
                            ", runtime=" + context.runtime_points +
                            ", test_case=" + context.test_case_points +
                            ", syntax=" + context.syntax_points);

                    context.debug.add("TestEvaluationHandler: Now sending the response back.");

                    return new CodeResponse(
                            allPassed,
                            new ArrayList<>(resultMap.values()),
                            new ArrayList<>(),
                            context.output,
                            List.of(new PointInfo(
                                    String.valueOf(context.syntax_points),
                                    String.valueOf(remainingRunTimePoints),
                                    String.valueOf(remainingTestCase)
                            )),
                            context.debug
                    );
                }

                context.debug.add("TestEvaluationHandler: End of TestEvaluationHandler.");
                CodeResponse response = new CodeResponse(allPassed, new ArrayList<>(resultMap.values()), new ArrayList<>(), context.output, context.debug);
                response.evaluateSuccess();
                return response;
            } catch (Exception e) {
                context.debug.add("TestEvaluationHandler: An Exception is thrown");
                return new CodeResponse(false, new ArrayList<>(), List.of(e.toString()), "", context.debug);
            }
        }
    }
