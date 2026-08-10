package me.srrapero720.waterui.format;

/** Raised anywhere in the lexer/parser/builder chain; carries the offending file and line. */
public class UIFormatException extends RuntimeException {
    public final String file;
    public final int line;

    // FILE IS NULL WHERE THE CALLER HAS NO SOURCE CONTEXT YET (e.g. UIValues); KEEP THE MESSAGE PLAIN THEN
    public UIFormatException(String file, int line, String message) {
        super(file != null ? file + ":" + line + " " + message : message);
        this.file = file;
        this.line = line;
    }
}
