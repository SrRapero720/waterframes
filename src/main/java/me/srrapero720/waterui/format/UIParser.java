package me.srrapero720.waterui.format;

import me.srrapero720.waterui.format.UILexer.Kind;
import me.srrapero720.waterui.format.UILexer.Token;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Recursive-descent parser turning a {@link UILexer} token stream into a {@link UIDocument}. It
 * enforces the strict statement order of UI-SPEC.md §6 (theme, imports, declarations, root, tree)
 * and produces the {@link UIValue} tree for every value; typed decoding is the builder's job.
 */
public final class UIParser {
    // STATEMENT RANKS FOR THE STRICT §6 ORDER; A STATEMENT WHOSE RANK IS BELOW THE CURRENT PHASE IS OUT OF ORDER
    private static final int THEME = 0, IMPORT = 1, DECL = 2, ROOT = 3, TREE = 4;

    private static final Set<String> TYPES = Set.of("boolean", "int", "long", "float", "double", "String", "Icon", "ItemStack", "Atlas");

    private final List<Token> tokens;
    private final ResourceLocation source;
    private final String file;
    private final String namespace;
    private int pos;

    private UIParser(List<Token> tokens, ResourceLocation source) {
        this.tokens = tokens;
        this.source = source;
        this.file = source.toString();
        this.namespace = source.getNamespace();
    }

    /** Parses a full {@code .ui} token stream into its document model. */
    public static UIDocument parse(List<Token> tokens, ResourceLocation source) {
        return new UIParser(tokens, source).document(source);
    }

    private UIDocument document(ResourceLocation source) {
        ResourceLocation themeId = null;
        List<UIImport> imports = new ArrayList<>();
        List<UIDecl> decls = new ArrayList<>();
        LinkedHashMap<String, UIValue> rootProps = new LinkedHashMap<>();
        List<UINode> tree = new ArrayList<>();
        boolean themeSeen = false, rootSeen = false;
        int phase = THEME;

        while (!checkKind(Kind.EOF)) {
            Token t = peek();
            if (t.kind() != Kind.IDENT) throw new UIFormatException(file, t.line(), "expected a statement or element tag, found '" + t.text() + "'");

            switch (t.text()) {
                case "theme" -> {
                    requireOrder(phase, THEME, t, "theme");
                    if (themeSeen) throw new UIFormatException(file, t.line(), "theme declared more than once");
                    themeSeen = true;
                    advance();
                    themeId = themeRef(expect(Kind.STRING, "a theme reference string"));
                    phase = THEME;
                }
                case "import" -> {
                    requireOrder(phase, IMPORT, t, "import");
                    imports.add(importStatement());
                    phase = IMPORT;
                }
                case "var" -> {
                    requireOrder(phase, DECL, t, "declaration");
                    advance();
                    decls.add(declaration(UIDecl.Mode.LIVE, t));
                    phase = DECL;
                }
                case "final" -> {
                    requireOrder(phase, DECL, t, "declaration");
                    advance();
                    expectIdent("var", "'var' after 'final'");
                    decls.add(declaration(UIDecl.Mode.BAKED, t));
                    phase = DECL;
                }
                case "root" -> {
                    requireOrder(phase, ROOT, t, "root");
                    if (rootSeen) throw new UIFormatException(file, t.line(), "root declared more than once");
                    rootSeen = true;
                    advance();
                    rootProps = bag();
                    phase = ROOT;
                }
                default -> {
                    requireOrder(phase, TREE, t, "element '" + t.text() + "'");
                    tree.add(node());
                    phase = TREE;
                }
            }
        }
        return new UIDocument(source, themeId, imports, decls, rootProps, tree);
    }

    // §6 ORDER: A STATEMENT MAY NOT APPEAR ONCE A LATER-RANKED ONE ALREADY DID
    private void requireOrder(int phase, int rank, Token t, String what) {
        if (phase > rank) throw new UIFormatException(file, t.line(), what + " out of order (UI-SPEC.md §6: theme, imports, declarations, root, elements)");
    }

    // "waterui" -> <docNs>:waterui, "othermod:dark" -> othermod:dark, NO .ui.json SUFFIX (§2)
    private ResourceLocation themeRef(Token ref) {
        String raw = ref.text();
        int colon = raw.indexOf(':');
        String ns = colon >= 0 ? raw.substring(0, colon) : namespace;
        String stem = colon >= 0 ? raw.substring(colon + 1) : raw;
        try {
            return ResourceLocation.fromNamespaceAndPath(ns, stem);
        } catch (RuntimeException e) {
            throw new UIFormatException(file, ref.line(), "invalid theme reference '" + raw + "'");
        }
    }

    // import "<ref>" [as <alias>]; THE FORM PICKS THE KIND: ':' CROSS-MOD, '.' JAVA, ELSE LOCAL (§7)
    private UIImport importStatement() {
        Token kw = advance(); // "import"
        Token ref = expect(Kind.STRING, "an import reference string");
        String raw = ref.text();
        String alias = null;
        if (checkIdent("as")) { advance(); alias = expect(Kind.IDENT, "an import alias").text(); }

        UIImport.Kind kind = raw.indexOf(':') >= 0 ? UIImport.Kind.CROSSMOD
                : raw.indexOf('.') >= 0 ? UIImport.Kind.JAVA
                : UIImport.Kind.LOCAL;

        if (alias == null) {
            switch (kind) {
                // A CROSS-MOD IMPORT ASSUMES NOTHING ABOUT A FOREIGN PATH LAYOUT, SO IT MUST NAME ITSELF (§7.1)
                case CROSSMOD -> throw new UIFormatException(file, kw.line(), "cross-mod import '" + raw + "' requires 'as <alias>'");
                case JAVA -> { int dot = raw.lastIndexOf('.'); alias = dot >= 0 ? raw.substring(dot + 1) : raw; }
                case LOCAL -> alias = raw.replace('/', '_');
            }
        }
        return new UIImport(kind, raw, alias, kw.line());
    }

    // var [type] name  |  final var [type] name [= value]; THE CALLER ALREADY CONSUMED THE KEYWORDS
    private UIDecl declaration(UIDecl.Mode mode, Token kw) {
        String type = null;
        Token first = expect(Kind.IDENT, "a variable name or type");
        // A LEADING TYPE KEYWORD IS ONE ONLY WHEN A NAME FOLLOWS IT; 'var String' ALONE NAMES A VAR "String"
        if (TYPES.contains(first.text()) && checkKind(Kind.IDENT)) {
            type = first.text();
            first = advance();
        }
        String name = first.text();

        UIValue inline = null;
        if (checkSymbol("=")) {
            if (mode == UIDecl.Mode.LIVE) throw new UIFormatException(file, kw.line(), "live variable '" + name + "' cannot carry an inline value (drop 'final' or the '=')");
            advance();
            inline = value();
        }
        return new UIDecl(mode, type, name, inline, source, kw.line());
    }

    // TAG [OPTIONAL [...] BAG] [OPTIONAL {...} BLOCK]; A BARE TAG IS VALID (Blank, Spacer) (§6, §11)
    private UINode node() {
        Token tag = expect(Kind.IDENT, "element tag");
        LinkedHashMap<String, UIValue> props = checkSymbol("[") ? bag() : new LinkedHashMap<>();
        List<UINode> children = checkSymbol("{") ? block() : List.of();
        return new UINode(tag.text(), tag.line(), props, children, source);
    }

    private List<UINode> block() {
        expectSymbol("{");
        List<UINode> children = new ArrayList<>();
        while (!checkSymbol("}")) {
            if (checkKind(Kind.EOF)) throw new UIFormatException(file, peek().line(), "unterminated block");
            children.add(node());
        }
        advance(); // "}"
        return children;
    }

    // [ key = value , key = value , bareKey ]; ',' SEPARATES, FIRST-DEFINED KEY WINS, A BARE KEY IS IGNORED (§3.5, §4)
    private LinkedHashMap<String, UIValue> bag() {
        expectSymbol("[");
        LinkedHashMap<String, UIValue> result = new LinkedHashMap<>();
        if (checkSymbol("]")) { advance(); return result; }
        while (true) {
            Token key = expect(Kind.IDENT, "a property key");
            if (checkSymbol("=")) {
                advance();
                UIValue v = value();
                // §12.1: AN on<Event> HANDLER MUST BE A CALL; A BARE NAME (value IS AN Id/anything ELSE) IS A COMPILE ERROR
                if (isEvent(key.text()) && !(v instanceof UIValue.Call)) {
                    throw new UIFormatException(file, key.line(), "event '" + key.text() + "' needs parentheses, e.g. " + key.text() + "=handler(...)");
                }
                result.putIfAbsent(key.text(), v);
            }
            // ELSE: BARE KEY, TOLERATED NOISE (§3.5)
            if (checkSymbol(",")) { advance(); continue; }
            break;
        }
        expectSymbol("]");
        return result;
    }

    // value ::= scalar | tuple | call | flags (§4)
    private UIValue value() {
        if (checkSymbol("{")) return tuple();
        Token t = peek();
        switch (t.kind()) {
            case NUMBER -> { advance(); return new UIValue.Num(Double.parseDouble(t.text()), t.text()); }
            case COLOR -> {
                advance();
                // GIVE A MALFORMED COLOUR (WRONG DIGIT COUNT) THE FILE AND LINE UIValues.color CANNOT KNOW (§13.4)
                try { return new UIValue.Hex(UIValues.color(t.text())); }
                catch (UIFormatException e) { throw new UIFormatException(file, t.line(), e.getMessage()); }
            }
            case STRING -> { advance(); return new UIValue.Str(t.text()); }
            case IDENT -> {
                advance();
                if (checkSymbol("(")) return call(t.text());
                if (checkSymbol("|")) return flags(t.text());
                return new UIValue.Id(t.text());
            }
            default -> throw new UIFormatException(file, t.line(), "expected a value, found '" + (t.kind() == Kind.EOF ? "<eof>" : t.text()) + "'");
        }
    }

    private UIValue tuple() {
        expectSymbol("{");
        List<UIValue> items = new ArrayList<>();
        if (checkSymbol("}")) { advance(); return new UIValue.Tuple(items); }
        while (true) {
            items.add(value());
            if (checkSymbol(",")) { advance(); continue; }
            break;
        }
        expectSymbol("}");
        return new UIValue.Tuple(items);
    }

    private UIValue call(String name) {
        expectSymbol("(");
        List<UIValue> args = new ArrayList<>();
        if (checkSymbol(")")) { advance(); return new UIValue.Call(name, args); }
        while (true) {
            args.add(value());
            if (checkSymbol(",")) { advance(); continue; }
            break;
        }
        expectSymbol(")");
        return new UIValue.Call(name, args);
    }

    private UIValue flags(String first) {
        List<String> names = new ArrayList<>();
        names.add(first);
        while (checkSymbol("|")) {
            advance();
            names.add(expect(Kind.IDENT, "a flag keyword").text());
        }
        return new UIValue.Flags(names);
    }

    /** True for an {@code on<Event>} property key; the one home the parser and builder both use (§12). */
    public static boolean isEvent(String name) {
        return name.length() > 2 && name.startsWith("on") && Character.isUpperCase(name.charAt(2));
    }

    private boolean checkKind(Kind kind) { return peek().kind() == kind; }
    private boolean checkSymbol(String s) { Token t = peek(); return t.kind() == Kind.SYMBOL && t.text().equals(s); }
    private boolean checkIdent(String s) { Token t = peek(); return t.kind() == Kind.IDENT && t.text().equals(s); }
    private Token peek() { return tokens.get(pos); }
    private Token advance() { return tokens.get(pos++); }

    private Token expect(Kind kind, String what) {
        Token t = peek();
        if (t.kind() != kind) throw new UIFormatException(file, t.line(), "expected " + what + ", found '" + (t.kind() == Kind.EOF ? "<eof>" : t.text()) + "'");
        return advance();
    }

    private void expectIdent(String text, String what) {
        Token t = peek();
        if (t.kind() != Kind.IDENT || !t.text().equals(text)) throw new UIFormatException(file, t.line(), "expected " + what + ", found '" + (t.kind() == Kind.EOF ? "<eof>" : t.text()) + "'");
        advance();
    }

    private void expectSymbol(String s) {
        Token t = peek();
        if (t.kind() != Kind.SYMBOL || !t.text().equals(s)) throw new UIFormatException(file, t.line(), "expected '" + s + "', found '" + (t.kind() == Kind.EOF ? "<eof>" : t.text()) + "'");
        advance();
    }
}
