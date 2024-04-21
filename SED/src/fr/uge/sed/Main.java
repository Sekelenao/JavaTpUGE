package fr.uge.sed;

import java.io.IOException;
import java.nio.file.Path;

public final class Main {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("""
        stream-editor rules input.txt output.txt

          rules:
            s          strip whitespaces
            u          upper case
            l          lower case
            d          delete
            su         strip and upper case
            i=;d       if is empty delete
            i=foo;d    if equals "foo" delete
            iu=FOO;d   if upper case equals "FOO" delete
            isu=FOO;d  if strip upper case equals "FOO" delete
            i=h.*;d    if starts with "h" delete
            i=a|b;d    if "a" or "b" delete
            is=;d      if strip is empty delete
        """);
            System.exit(1);
            return;
        }
        var rule = StreamEditor.createRules(args[0]);
        var inputPath = Path.of(args[1]);
        var outputPath = Path.of(args[2]);

        var editor = new StreamEditor(rule);
        try {
            editor.rewrite(inputPath, outputPath);
        } catch (IOException e) {
            System.err.println(STR."error \{e.getMessage()}");
            System.exit(2);
        }
    }

}
