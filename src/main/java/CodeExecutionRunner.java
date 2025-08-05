public class CodeExecutionRunner {
    public CodeResponse run(CodeContext context) {
        CodeHandler save = new SaveCodeHandler();
        CodeHandler compile = new CompileHandler();
        CodeHandler runtime = new RuntimeHandler();
        CodeHandler testEval = new TestEvaluationHandler();

        save.setNext(compile);
        compile.setNext(runtime);
        runtime.setNext(testEval);

        return save.handle(context);
    }
}
