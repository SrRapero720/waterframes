package me.srrapero720.waterui.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-rolled single-pass scanner turning raw {@code .ui} source text into a flat token stream
 * (UI-SPEC.md §3). Comments are stripped here and never reach the parser. Structural symbols are
 * {@code { } [ ] ( ) , = |}; a {@code :} stays inside atoms so an explicit-namespace id
 * ({@code other:name}) survives as one token; dots live only inside strings now.
 */
public final class UILexer {
    public enum Kind { IDENT, NUMBER, STRING, COLOR, SYMBOL, EOF }

    public record Token(String text, Kind kind, int line) {}

    private static final String STRUCTURAL = "{}[](),=|";
    private static final String NUMBER_PATTERN = "-?[0-9]+(\\.[0-9]+)?";

    private final String source;
    private final String file;
    private final List<Token> tokens = new ArrayList<>();
    private int pos;
    private int line = 1;

    private UILexer(String source, String file) {
        this.source = source;
        this.file = file;
    }

    /** Tokenizes {@code source}; {@code file} only labels errors (unterminated string/comment). */
    public static List<Token> tokenize(String source, String file) {
        UILexer lexer = new UILexer(source, file);
        lexer.scan();
        return lexer.tokens;
    }

    private void scan() {
        while (pos < source.length()) {
            char c = source.charAt(pos);

            // DISPATCH ORDER IS LOAD-BEARING: '\n' BEFORE THE GENERIC WHITESPACE SKIP (LINE COUNT),
            // COMMENTS BEFORE ATOMS ('/' IS NOT AN ATOM CHAR), STRINGS BEFORE ANY SEPARATOR SPLIT (§3.4)
            if (c == '\n') { line++; pos++; continue; }
            if (Character.isWhitespace(c)) { pos++; continue; }

            // COMMENTS ARE STRIPPED HERE AND NEVER REACH THE TOKEN LIST
            if (c == '/' && peek(1) == '/') { skipLineComment(); continue; }
            if (c == '/' && peek(1) == '*') { skipBlockComment(); continue; }

            if (c == '"') { readString(); continue; }
            if (c == '#') { readColor(); continue; }
            if (STRUCTURAL.indexOf(c) >= 0) {
                tokens.add(new Token(String.valueOf(c), Kind.SYMBOL, line));
                pos++;
                continue;
            }

            readAtom();
        }
        tokens.add(new Token("", Kind.EOF, line));
    }

    private char peek(int offset) {
        int i = pos + offset;
        return i < source.length() ? source.charAt(i) : '\0';
    }

    private void skipLineComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') pos++;
    }

    private void skipBlockComment() {
        int startLine = line;
        pos += 2;
        while (pos < source.length() && !(source.charAt(pos) == '*' && peek(1) == '/')) {
            if (source.charAt(pos) == '\n') line++;
            pos++;
        }
        if (pos >= source.length()) throw new UIFormatException(file, startLine, "unterminated block comment");
        pos += 2;
    }

    private void readString() {
        int startLine = line;
        pos++;
        StringBuilder text = new StringBuilder();
        while (true) {
            if (pos >= source.length()) throw new UIFormatException(file, startLine, "unterminated string");
            char c = source.charAt(pos);
            if (c == '"') { pos++; break; }
            if (c == '\\') {
                pos++;
                if (pos >= source.length()) throw new UIFormatException(file, startLine, "unterminated string");
                text.append(source.charAt(pos));
                pos++;
                continue;
            }
            if (c == '\n') line++;
            text.append(c);
            pos++;
        }
        // TEXT IS THE UNESCAPED LITERAL CONTENT; BRACES SURVIVE INTACT FOR {name} INTERPOLATION LATER (§5.4)
        tokens.add(new Token(text.toString(), Kind.STRING, startLine));
    }

    private void readColor() {
        int start = pos;
        pos++;
        while (pos < source.length() && isHexDigit(source.charAt(pos))) pos++;
        // ANY '#'+HEX RUN IS ONE TOKEN; THE 6/8-DIGIT ARITY IS THE PARSER'S CHECK, WHICH HAS FILE+LINE
        tokens.add(new Token(source.substring(start, pos), Kind.COLOR, line));
    }

    private void readAtom() {
        int start = pos;
        while (pos < source.length() && isAtomChar(source.charAt(pos))) pos++;
        // A LONE UNHANDLED CHAR (e.g. '/', '@', '!') MATCHES NO RULE: FAIL LOUD INSTEAD OF SPINNING FOREVER
        if (pos == start) throw new UIFormatException(file, line, "unexpected character '" + source.charAt(pos) + "'");
        // ONE CHARSET SCANS IDENTS, NUMBERS AND ns:name IDS ALIKE; THE KIND IS DECIDED AFTER THE FACT.
        // NUMBER-ISH GARBAGE ("1.2.3", "5-3") DELIBERATELY FALLS TO IDENT SO THE PARSER REJECTS IT IN CONTEXT
        String text = source.substring(start, pos);
        tokens.add(new Token(text, text.matches(NUMBER_PATTERN) ? Kind.NUMBER : Kind.IDENT, line));
    }

    // IDENTIFIERS, KEYWORDS AND NUMBERS; ':' STAYS FOR ns:name IDS, '-'/'.' FOR SIGNED DECIMALS (§3.3)
    private static boolean isAtomChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '.' || c == '-';
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
