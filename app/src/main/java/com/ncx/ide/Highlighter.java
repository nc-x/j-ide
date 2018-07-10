package com.ncx.ide;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.ArrayList;
import java.util.List;

public class Highlighter {
    private SpannableString source;
    private String sourceString;

    private int startIdx = 0;
    private int currentIdx = 0;

    private int multi_comment = 0;
    private int string_backslash = 0;

    private List<String> keywords = new ArrayList<>();
    private List<String> dataTypes = new ArrayList<>();

    Highlighter() {
        this.dataTypes.add("boolean");
        this.dataTypes.add("byte");
        this.dataTypes.add("char");
        this.dataTypes.add("double");
        this.dataTypes.add("float");
        this.dataTypes.add("long");
        this.dataTypes.add("int");
        this.dataTypes.add("short");
        this.dataTypes.add("String");
        this.dataTypes.add("Boolean");
        this.dataTypes.add("Byte");
        this.dataTypes.add("Character");
        this.dataTypes.add("Double");
        this.dataTypes.add("Float");
        this.dataTypes.add("Long");
        this.dataTypes.add("Integer");
        this.dataTypes.add("Short");
        this.dataTypes.add("void");

        this.keywords.add("abstract");
        this.keywords.add("assert");
        this.keywords.add("break");
        this.keywords.add("case");
        this.keywords.add("catch");
        this.keywords.add("class");
        this.keywords.add("const");
        this.keywords.add("continue");
        this.keywords.add("default");
        this.keywords.add("do");
        this.keywords.add("else");
        this.keywords.add("enum");
        this.keywords.add("extends");
        this.keywords.add("false");
        this.keywords.add("final");
        this.keywords.add("finally");
        this.keywords.add("for");
        this.keywords.add("goto");
        this.keywords.add("his");
        this.keywords.add("if");
        this.keywords.add("implements");
        this.keywords.add("import");
        this.keywords.add("instanceof");
        this.keywords.add("interface");
        this.keywords.add("native");
        this.keywords.add("new");
        this.keywords.add("null");
        this.keywords.add("package");
        this.keywords.add("private");
        this.keywords.add("protected");
        this.keywords.add("public");
        this.keywords.add("return");
        this.keywords.add("static");
        this.keywords.add("strictfp");
        this.keywords.add("super");
        this.keywords.add("switch");
        this.keywords.add("synchronized");
        this.keywords.add("throw");
        this.keywords.add("throws");
        this.keywords.add("transient");
        this.keywords.add("true");
        this.keywords.add("try");
        this.keywords.add("volatile");
        this.keywords.add("while");
    }

    public SpannableString highlight(String s) {
        this.currentIdx = 0;
        this.startIdx = 0;
        this.source = new SpannableString(s);
        this.sourceString = s;

        scanTokens();

        return source;
    }

    private void scanTokens() {
        while (!isAtEnd()) {
            startIdx = currentIdx;
            scanToken();
        }
    }

    private boolean isAtEnd() {
        return currentIdx >= sourceString.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '+':
            case '-':
            case '*':
            case '%':
            case '^':
            case '&':
            case '|':
            case '!':
            case '~':
            case '<':
            case '>':
            case '?':
            case ':':
            case '=':
                source.setSpan(new ForegroundColorSpan(Color.rgb(249, 38, 114)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '.':
            case ',':
            case ';':
                source.setSpan(new ForegroundColorSpan(Color.rgb(248, 248, 242)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case '"':
                string();
                break;
            case '\'':
                character();
                break;
            case '/':
                comment_or_division();
                break;
            case '@':
                annotations();
                break;
            case ' ':
            case '\r':
            case '\n':
            case '\t':
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    source.setSpan(new ForegroundColorSpan(Color.rgb(248, 248, 240)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    source.setSpan(new BackgroundColorSpan(Color.rgb(249, 38, 114)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
        }
    }

    private char advance() {
        currentIdx++;
        return source.charAt(currentIdx - 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.') {
            advance();
        }
        source.setSpan(new ForegroundColorSpan(Color.rgb(174, 129, 255)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(currentIdx);
    }

    private boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c == '_';
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = sourceString.substring(startIdx, currentIdx);

        if (keywords.contains(text)) {
            StyleSpan[] italics = source.getSpans(startIdx, currentIdx, StyleSpan.class);
            for (StyleSpan s : italics) {
                source.removeSpan(s);
            }
            source.setSpan(new ForegroundColorSpan(Color.rgb(249, 38, 114)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (dataTypes.contains(text)) {
            source.setSpan(new StyleSpan(Typeface.ITALIC), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            source.setSpan(new ForegroundColorSpan(Color.rgb(102, 217, 239)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            StyleSpan[] italics = source.getSpans(startIdx, currentIdx, StyleSpan.class);
            for (StyleSpan s : italics) {
                source.removeSpan(s);
            }
            source.setSpan(new ForegroundColorSpan(Color.rgb(248, 248, 242)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void comment_or_division() {
        if (peek() == '/') {
            while (!isAtEnd() && peek() != '\n') advance();
            source.setSpan(new ForegroundColorSpan(Color.rgb(117, 113, 94)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (peek() == '*') {
            advance();
            source.setSpan(new ForegroundColorSpan(Color.rgb(117, 113, 94)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            while (currentIdx < (source.length())) {
                if (peek() == '*') {
                    multi_comment = 1;
                    advance();
                } else if (peek() == '/') {
                    advance();
                    if (multi_comment == 1) {
                        source.setSpan(new ForegroundColorSpan(Color.rgb(117, 113, 94)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                    multi_comment = 0;
                } else {
                    multi_comment = 0;
                    advance();
                }
                source.setSpan(new ForegroundColorSpan(Color.rgb(117, 113, 94)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            source.setSpan(new ForegroundColorSpan(Color.rgb(249, 38, 114)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void string() {
        while (!isAtEnd()) {
            if (peek() == '\\' && string_backslash == 0) {
                string_backslash = 1;
                advance();
            } else if (peek() == 't' || peek() == 'b' || peek() == 'n' || peek() == 'r' || peek() == 'f' || peek() == '\'' || peek() == '"' || peek() == '\\') {
                if (string_backslash == 1) {
                    advance();
                    string_backslash = 0;
                } else {
                    break;
                }
            } else {
                string_backslash = 0;
                advance();
            }
        }
        while (peek() != '"' && !isAtEnd()) advance();
        if (!isAtEnd()) advance();
        source.setSpan(new ForegroundColorSpan(Color.rgb(230, 219, 116)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void character() {
        while (!isAtEnd()) {
            if (peek() == '\\' && string_backslash == 0) {
                string_backslash = 1;
                advance();
            } else if (peek() == 't' || peek() == 'b' || peek() == 'n' || peek() == 'r' || peek() == 'f' || peek() == '\'' || peek() == '"' || peek() == '\\') {
                if (string_backslash == 1) {
                    string_backslash = 0;
                    advance();
                } else {
                    break;
                }
            } else {
                string_backslash = 0;
                advance();
            }
        }
        while (peek() != '\'' && !isAtEnd()) advance();
        if (!isAtEnd()) advance();
        source.setSpan(new ForegroundColorSpan(Color.rgb(230, 219, 116)), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void annotations() {
        while (!isAtEnd() && peek() != ' ') advance();
        source.setSpan(new ForegroundColorSpan(Color.CYAN), startIdx, currentIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
