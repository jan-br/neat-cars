package de.jan.machinelearning.autonomouscar.terminal;

import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * Created by DevTastisch on 29.01.2019
 */
public class FormEntry {

    private final int id;
    private final String answer;
    private final Runnable runnable;

    public FormEntry(int id, String answer, Runnable runnable) {
        this.id = id;
        this.answer = answer;
        this.runnable = runnable;
    }


    public int getId() {
        return id;
    }

    public String getAnswer() {
        return answer;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void print(PrintStream printStream) {
        String[] answerParts = this.answer.split("(?<=\\G........................................)");
        for (int i = 0; i < answerParts.length; i++) {
            printStream.println(String.format("%-10s %-50s%s", "|", (i == 0 ? (this.id + ": ") : "") + answerParts[i], "|"));
        }
    }

}
