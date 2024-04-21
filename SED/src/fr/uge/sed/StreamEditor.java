package fr.uge.sed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class StreamEditor {

    @FunctionalInterface
    public interface Rule {
        Optional<String> rewrite(String line);

        static Rule andThen(Rule first, Rule second) {
            Objects.requireNonNull(first);
            Objects.requireNonNull(second);
            return line -> first.rewrite(line).flatMap(second::rewrite);
        }

        default Rule andThen(Rule rule) {
            Objects.requireNonNull(rule);
            return line -> this.rewrite(line).flatMap(rule::rewrite);
        }
    }

    private final Rule rule;

    public StreamEditor(Rule rule){
        this.rule = Objects.requireNonNull(rule);
    }

    public void rewrite(BufferedReader reader, Writer writer) throws IOException {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(writer);
        String currentLine;
        while((currentLine = reader.readLine()) != null){
            var modifiedLine = rule.rewrite(currentLine);
            if(modifiedLine.isPresent()) {
                writer.write(modifiedLine.get());
                writer.append('\n');
            }
        }
    }

    public void rewrite(Path input, Path output) throws IOException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        try(var reader = Files.newBufferedReader(input); var writer = Files.newBufferedWriter(output)){
            rewrite(reader, writer);
        }
    }

    private static Rule evaluate(int c){
        return switch (c){
            case 's' -> line -> Optional.of(line.replaceAll("\\s+", ""));
            case 'u' -> line -> Optional.of(line.toUpperCase(Locale.ROOT));
            case 'l' -> line -> Optional.of(line.toLowerCase(Locale.ROOT));
            case 'd' -> _ -> Optional.empty();
            default -> throw new IllegalArgumentException(STR."Unexpected value: \{c}");
        };
    }

    public static Rule createRules(String rule){
        return Objects.requireNonNull(rule).chars()
                .mapToObj(StreamEditor::evaluate)
                .reduce((rule1, rule2) -> rule1.andThen(rule2)) // Rule::andThen if we remove the static method
                .orElse(Optional::of);
    }



}
