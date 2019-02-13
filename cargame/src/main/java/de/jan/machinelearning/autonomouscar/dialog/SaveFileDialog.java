package de.jan.machinelearning.autonomouscar.dialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.function.Consumer;

/**
 * Created by DevTastisch on 29.01.2019
 */
public class SaveFileDialog extends JFileChooser {


    public SaveFileDialog(String[] fileExtensions, String fileDescription, String title, Consumer<File> selectFileConsumer, Consumer<File> selectFileFailedConsumer) {
        this(new File("./"), fileExtensions, fileDescription, title, selectFileConsumer, selectFileFailedConsumer);
    }

    public SaveFileDialog(File startDirectory, String[] fileExtensions, String fileDescription, String title, Consumer<File> selectFileConsumer, Consumer<File> selectFileFailedConsumer) {
        super(startDirectory);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        this.setDialogTitle(title);
        this.setMultiSelectionEnabled(false);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.setFileFilter(new FileNameExtensionFilter(fileDescription, fileExtensions));
        int returnVal = this.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION){
            selectFileConsumer.accept(this.getSelectedFile());
        }else{
            selectFileFailedConsumer.accept(this.getSelectedFile());
        }

    }

}
