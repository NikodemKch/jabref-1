package org.jabref.gui.bibtexextractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import javafx.scene.control.ProgressIndicator;
import javax.inject.Inject;
import javax.swing.undo.UndoManager;
import org.jabref.Globals;
import org.jabref.JabRefGUI;
import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.externalfiles.ImportHandler;
import org.jabref.gui.externalfiletype.ExternalFileTypes;
import org.jabref.gui.undo.CountingUndoManager;
import org.jabref.gui.util.BackgroundTask;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.importer.fetcher.GrobidCitationFetcher;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.util.FileUpdateMonitor;
import org.jabref.preferences.JabRefPreferences;


public class BibtexExtractorViewModel {

    private final StringProperty inputTextProperty = new SimpleStringProperty("");
    private final BibDatabaseContext bibdatabaseContext;
    private List<BibEntry> extractedEntries;
    private DialogService dialogService;
    private GrobidCitationFetcher currentCitationfetcher;
    private TaskExecutor taskExecutor;
    private ImportHandler importHandler;

    public BibtexExtractorViewModel(BibDatabaseContext bibdatabaseContext, DialogService dialogService,
                                    JabRefPreferences jabRefPreferences, FileUpdateMonitor fileUpdateMonitor, TaskExecutor taskExecutor, UndoManager undoManager, StateManager stateManager) {
        this.bibdatabaseContext = bibdatabaseContext;
        this.dialogService = dialogService;
        currentCitationfetcher = new GrobidCitationFetcher(
            jabRefPreferences,
            fileUpdateMonitor
        );
        this.taskExecutor = taskExecutor;
        this.importHandler = new ImportHandler(dialogService,bibdatabaseContext, ExternalFileTypes.getInstance(),jabRefPreferences.getFilePreferences(),jabRefPreferences.getImportFormatPreferences(),jabRefPreferences.getUpdateFieldPreferences(),fileUpdateMonitor,undoManager,stateManager);

    }

    public StringProperty inputTextProperty() {
        return this.inputTextProperty;
    }

    public void startParsing() {
        this.extractedEntries = null;
        /*Task<Void> parseUsingGrobid = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    extractedEntries = currentCitationfetcher.performSearch(inputTextProperty.getValue());
                } catch (FetcherException e) {
                    extractedEntries = Collections.emptyList();
                }
                Platform.runLater(() -> executeParse());
            }
        };*/
        //dialogService.showProgressDialogAndWait(Localization.lang("Your text is being parsed.."),Localization.lang( "Please wait while we are parsing your text"), task);
        BackgroundTask.wrap(() -> currentCitationfetcher.performSearch(inputTextProperty.getValue()))
                .onRunning(() -> dialogService.notify("Your text is being parsed"))
                .onSuccess(parsedEntries -> {
                  extractedEntries = parsedEntries;
                  executeParse();
                })
                .onFailure(exception -> {
                  extractedEntries = Collections.emptyList();
                  executeParse();
                }).executeWith(taskExecutor);
        //Globals.TASK_EXECUTOR.execute(task);
    }

    public void executeParse() {
        if (!extractedEntries.isEmpty()) {
            if (currentCitationfetcher.getFailedEntries().size() > 0) {
              dialogService.showWarningDialogAndWait(
                  Localization.lang("Grobid failed to parse the following entries:"),
                  String.join("\n;;\n", currentCitationfetcher.getFailedEntries()));
            } else {
              importHandler.importEntries(extractedEntries);
              dialogService.notify(Localization.lang("Successfully added a new entry."));
            }
        }
    }

    public void startExtraction() {
        BibtexExtractor extractor = new BibtexExtractor();
        BibEntry entity = extractor.extract(inputTextProperty.getValue());
        this.bibdatabaseContext.getDatabase().insertEntry(entity);
        trackNewEntry(StandardEntryType.Article);
    }

    private void trackNewEntry(EntryType type) {
        Map<String, String> properties = new HashMap<>();
        properties.put("EntryType", type.getName());

        Globals.getTelemetryClient().ifPresent(client -> client.trackEvent("NewEntry", properties, new HashMap<>()));
    }
}
