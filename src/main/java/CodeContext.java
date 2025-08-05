import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeContext {
    public enum Action {
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
    String request_action;
    String output;

    Integer syntax_points;
    Integer runtime_points;
    Integer test_case_points;

    protected String nameCode;
    protected String nameTestUnit;
    protected Action action;

    public Action getParsedAction() {
        return Action.fromString(request_action);
    }

    public void assignClassNames() {
        if (syntax_points == null) syntax_points = 0;
        if (runtime_points == null) runtime_points = 0;
        if (test_case_points == null) test_case_points = 0;

        this.nameCode = getClassName(this.code);
        this.nameTestUnit = getClassName(this.testUnit);
        this.action = getParsedAction();
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
    // ... maybe result holders, etc.
}
