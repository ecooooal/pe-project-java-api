import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
public class CodeValidator {
    String completeSolution;
    String testUnit;

    protected String nameCompleteSolution;
    protected String nameTestUnit;
    
    @Override
    public String toString() {
        return String.format("UserCode {\n\tComplete Solution='%s', \n\tTest Unit='%s \n} Class Names are {\n\tComplete Solution='%s', \n\tTest Unit='%s'\n} ", completeSolution, testUnit, nameCompleteSolution, nameTestUnit );
    }

    public CodeResponse validate(){
        try{
            System.out.println("Saving The Codes");
            saveCodes(completeSolution, testUnit);
            
            System.out.println("Compiling The Codes");

            Process compile = compileCode(nameCompleteSolution, nameTestUnit);

            String compileOutput = read(compile.getInputStream());
            compile.waitFor();
            if (!compileOutput.isBlank()) {
                System.out.println("Compiling returns Blank");
                return new CodeResponse(false, new ArrayList<>(), List.of(compileOutput), "");
            }

            System.out.println("Validating the Complete Solution Code");
            Process run = runCode(nameTestUnit);

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

    protected Process compileCode(String nameCompleteSolution, String nameTestUnit) throws IOException {
        Process compile = new ProcessBuilder(
                "javac",
                "-cp", "lib/junit-platform-console-standalone-1.13.1.jar",
                "-d", "out",
                "src/main/java/" + nameCompleteSolution + ".java",
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

    protected void saveCodes(String completeSolution, String testUnit){
        String solutionPath = "src/main/java/";
        String testUnitPath = "src/test/java/";

        System.out.println("Saving Complete Solution");
        try (FileWriter writer = new FileWriter(solutionPath + this.nameCompleteSolution + ".java")) {
            writer.write(completeSolution);
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
        System.out.println("Extracting from completeSolution: " + completeSolution);
        this.nameCompleteSolution = getClassName(this.completeSolution);
        System.out.println("Extracted nameCompleteSolution: " + nameCompleteSolution);

        this.nameTestUnit = getClassName(this.testUnit);
        System.out.println("Extracted nameTestUnit: " + nameTestUnit);
    }

}
