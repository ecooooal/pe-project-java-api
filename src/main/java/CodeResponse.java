import java.util.ArrayList;
import java.util.List;

class PointInfo {
    public String syntax;
    public String runtime;
    public String testcase;

    public PointInfo(String syntax, String runtime, String testcase) {
        this.syntax = syntax;
        this.runtime = runtime;
        this.testcase = testcase;
    }
}


public class CodeResponse {

    public boolean success;
    public List<TestResult> testResults;
    public List<String> failures;
    public List<String> debug;
    public String output;
    public List<PointInfo> points = new ArrayList<>();


    public CodeResponse(boolean success, List<TestResult> testResults, List<String> failures, String output, List<String> debug) {
        this.success = success;
        this.testResults = testResults;
        this.failures = failures;
        this.output = output;
        this.debug = debug;
    }

    public CodeResponse(boolean success, List<TestResult> testResults, List<String> failures, String output, List<PointInfo> points, List<String> debug) {
        this(success, testResults, failures, output, debug);
        this.points = points;
    }


    public static class TestResult {
        public String className;
        public List<TestMethodResult> methods;

        public TestResult(String className) {
            this.className = className;
            this.methods = new ArrayList<>();
        }

        public void addMethod(String methodName, TestStatus status, String message) {
            methods.add(new TestMethodResult(methodName, status, message));
        }
    }

    public static class TestMethodResult {
        public String methodName;
        public TestStatus status;
        public String message;

        public TestMethodResult(String methodName, TestStatus status, String message) {
            this.methodName = methodName;
            this.status = status;
            this.message = message;
        }
    }
}
