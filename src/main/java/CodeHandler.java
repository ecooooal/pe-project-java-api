public interface CodeHandler {
    void setNext(CodeHandler next);
    CodeResponse handle(CodeContext context);
}
