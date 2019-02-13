package de.jan.machinelearning.autonomouscar.terminal;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by DevTastisch on 29.01.2019
 */
public class Form {

    private final Set<FormEntry> formEntries = ConcurrentHashMap.newKeySet();
    private final String question;
    private final int defaultId;

    public Form(String question, int defaultId) {
        this.question = question;
        this.defaultId = defaultId;
    }

    public Form addEntry(String answer, Runnable runnable) {
        this.formEntries.add(new FormEntry(this.formEntries.size() + 1, answer, runnable));
        return this;
    }

    public Set<FormEntry> getFormEntries() {
        return Collections.unmodifiableSet(this.formEntries);
    }

    public void print(PrintStream printStream) {

        printStream.println("--------------------------------------------------------------");
        printStream.println(String.format("|%61s", "|"));
        String[] questionParts = this.question.split("(?<=\\G........................................)");
        for (String questionPart : questionParts) {
            printStream.println(String.format("%-10s %-50s%s", "|", questionPart, "|"));
        }
        printStream.println(String.format("|%61s", "|"));
        printStream.println("|------------------------------------------------------------|");
        printStream.println(String.format("|%61s", "|"));
        List<FormEntry> collect = this.getFormEntries().stream().sorted(Comparator.comparingInt(FormEntry::getId)).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            collect.get(i).print(System.out);
            if(i != collect.size() - 1){
                printStream.println("|                                                            |");
            }
        }
        printStream.println(String.format("|%61s", "|"));
        printStream.println("--------------------------------------------------------------");
        printStream.println();
        printStream.print(" > (default: " + this.defaultId + "):");

        FormEntry selectedEntry = null;

        while (selectedEntry == null) {
            try {
                String line = System.console().readLine();
                int id = line.isEmpty() ? this.getDefaultId() : Integer.valueOf(line);
                selectedEntry = this.formEntries.stream()
                        .filter(formEntry -> formEntry.getId() == id)
                        .findAny()
                        .orElse(null);
                selectedEntry.getRunnable().run();
            } catch (Exception ex) {
                System.out.println("Untültige id");
                printStream.print(" > (default: " + this.defaultId + "):");
            }
        }
    }

    public int getDefaultId() {
        return defaultId;
    }
}
