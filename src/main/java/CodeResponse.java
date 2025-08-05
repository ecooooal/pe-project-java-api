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
    public String output;
    public List<PointInfo> points = new ArrayList<>();


    public CodeResponse(boolean success, List<TestResult> testResults, List<String> failures, String output) {
        this.success = success;
        this.testResults = testResults;
        this.failures = failures;
        this.output = output;
    }

    public CodeResponse(boolean success, List<TestResult> testResults, List<String> failures, String output, List<PointInfo> points) {
        this(success, testResults, failures, output);
        this.points = points;
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

    public void addPoint(String syntax, String runtime, String testcase) {
        points.add(new PointInfo(syntax, runtime, testcase));
    }

}
