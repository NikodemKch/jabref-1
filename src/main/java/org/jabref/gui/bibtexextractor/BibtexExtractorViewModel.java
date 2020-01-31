package org.jabref.gui.bibtexextractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.swing.undo.UndoManager;
import org.jabref.Globals;
import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.externalfiles.ImportHandler;
import org.jabref.gui.externalfiletype.ExternalFileTypes;
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
    private DialogService dialogService;
    private GrobidCitationFetcher currentCitationfetcher;
    private TaskExecutor taskExecutor;
    private ImportHandler importHandler;

    public BibtexExtractorViewModel(BibDatabaseContext bibdatabaseContext, DialogService dialogService,
                                    JabRefPreferences jabRefPreferences, FileUpdateMonitor fileUpdateMonitor, TaskExecutor taskExecutor, UndoManager undoManager, StateManager stateManager) {
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
        BackgroundTask.wrap(() -> currentCitationfetcher.performSearch(inputTextProperty.getValue()))
                .onRunning(() -> dialogService.notify("Your text is being parsed..."))
                .onSuccess(parsedEntries -> {
                  dialogService.notify("GROBID could parse " + parsedEntries.size() + " Entries from your query.");
                  importHandler.importEntries(parsedEntries);
                  for (BibEntry bibEntry: parsedEntries) {
                      trackNewEntry(bibEntry);
                  }
                }).executeWith(taskExecutor);
    }

    private void trackNewEntry(BibEntry bibEntry) {
        Map<String, String> properties = new HashMap<>();
        properties.put("EntryType", bibEntry.typeProperty().getValue().getName());
        Globals.getTelemetryClient().ifPresent(client -> client.trackEvent("ParseWithGrobid", properties, new HashMap<>()));
    }
}
