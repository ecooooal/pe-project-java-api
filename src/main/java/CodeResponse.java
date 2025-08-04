import java.util.ArrayList;
import java.util.List;

public class CodeResponse {
    public boolean success;
    public List<TestResult> testResults;
    public List<String> failures;
    public String output;

    public CodeResponse(boolean success, List<TestResult> testResults, List<String> failures, String output) {
        this.success = success;
        this.testResults = testResults;
        this.failures = failures;
        this.output = output;
    }

    public static class TestResult {
        public String className;
        public List<TestMethodResult> methods;

        public TestResult(String className) {
            this.className = className;
            this.methods = new ArrayList<>();
        }

        public void addMethod(String methodName, String status, String message) {
            methods.add(new TestMethodResult(methodName, status, message));
        }
    }

    public static class TestMethodResult {
        public String methodName;
        public String status;
        public String message;

        public TestMethodResult(String methodName, String status, String message) {
            this.methodName = methodName;
            this.status = status;
            this.message = message;
        }
    }
}
