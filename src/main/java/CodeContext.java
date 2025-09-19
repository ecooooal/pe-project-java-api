import java.util.ArrayList;
import java.util.List;
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
    Integer syntax_points_deduction;
    Integer runtime_points_deduction;
    Integer test_case_points_deduction;

    protected String nameCode;
    protected String nameTestUnit;
    protected List<String> debug = new ArrayList<>();
    protected Action action;

    public Action getParsedAction() {
        return Action.fromString(request_action);
    }

    public void assignClassNames() {
        if (syntax_points == null) syntax_points = 0;
        if (runtime_points == null) runtime_points = 0;
        if (test_case_points == null) test_case_points = 0;
        if (syntax_points_deduction == null) syntax_points_deduction = 1;
        if (runtime_points_deduction == null) runtime_points_deduction = 1;
        if (test_case_points_deduction == null) test_case_points_deduction = 1;

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
}
