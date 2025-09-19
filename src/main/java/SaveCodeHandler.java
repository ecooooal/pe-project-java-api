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
        context.debug.add("SaveCodeHandler: Starting SaveCodeHandler.");

        if (context.action == null) {
            return new CodeResponse(false, new ArrayList<>(), List.of("Invalid action: " + context.action), "", new ArrayList<>()   );
        }

        // Save code
        String solutionPath = "src/main/java/";
        String testUnitPath = "src/test/java/";

        context.debug.add("SaveCodeHandler: Saving Codes in container.");
        try (FileWriter writer = new FileWriter(solutionPath + context.nameCode + ".java")) {
            writer.write(context.code);
        } catch (IOException e) {
            context.debug.add("SaveCodeHandler: 🔴 Saving user code threw exception.");
            return new CodeResponse(false, List.of(), List.of("Failed to save user code: " + e.getMessage()), "", context.debug);
        }


        try (FileWriter writer = new FileWriter(testUnitPath + context.nameTestUnit + ".java")) {
            writer.write(context.testUnit);
        } catch (IOException e) {
            context.debug.add("SaveCodeHandler: 🔴 Saving test code threw exception.");
            return new CodeResponse(false, List.of(), List.of("Failed to save test code: " + e.getMessage()), "", context.debug);
        }

        context.debug.add("SaveCodeHandler: 🟢 Saving success, now sending the context to CompileHandler.");

        // Then call next if exists
        return next != null ? next.handle(context) : null;
    }
}
