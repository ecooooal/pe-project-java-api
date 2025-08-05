import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SaveCodeHandler implements CodeHandler {
    private CodeHandler next;

    @Override
    public void setNext(CodeHandler next) {
        this.next = next;
    }

    @Override
    public CodeResponse handle(CodeContext context) {

        if (context.action == null) {
            return new CodeResponse(false, new ArrayList<>(), List.of("Invalid action: " + context.action), "");
        }

        // Save code
        String solutionPath = "src/main/java/";
        String testUnitPath = "src/test/java/";

        System.out.println("Saving Complete Solution");
        try (FileWriter writer = new FileWriter(solutionPath + context.nameCode + ".java")) {
            writer.write(context.code);
            System.out.println("Complete Solution is saved.\n");
        } catch (IOException e) {
            return new CodeResponse(false, List.of(), List.of("Failed to save code: " + e.getMessage()), "");
        }

        System.out.println("Saving Test Unit");
        try (FileWriter writer = new FileWriter(testUnitPath + context.nameTestUnit + ".java")) {
            writer.write(context.testUnit);
            System.out.println("Test Unit is saved.\n");
        } catch (IOException e) {
            return new CodeResponse(false, List.of(), List.of("Failed to save code: " + e.getMessage()), "");
        }

        // Then call next if exists
        return next != null ? next.handle(context) : null;
    }
}
