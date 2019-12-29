package org.jabref.gui.bibtexextractor;

import java.util.ArrayList;
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
import org.jabref.Globals;
import org.jabref.JabRefGUI;
import org.jabref.gui.DialogService;
import org.jabref.gui.util.BackgroundTask;
import org.jabref.logic.bibtexkeypattern.BibtexKeyGenerator;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.fetcher.GrobidCitationFetcher;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.Defaults;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.util.FileUpdateMonitor;
import org.jabref.preferences.JabRefPreferences;

import javax.xml.bind.annotation.XmlElementDecl;

public class BibtexExtractorViewModel {

    private final StringProperty inputTextProperty = new SimpleStringProperty("");
    private final BibDatabaseContext bibdatabaseContext;
    private List<BibEntry> extractedEntries;
    private final BibDatabaseContext newDatabaseContext;
    private boolean directAdd;
    private DialogService dialogService;
    private GrobidCitationFetcher currentCitationfetcher;
    private ProgressIndicator progressIndicator;
    private final BooleanProperty parsingInProgress = new SimpleBooleanProperty(false);

    public BibtexExtractorViewModel(BibDatabaseContext bibdatabaseContext, DialogService dialogService,
                                    JabRefPreferences jabRefPreferences, FileUpdateMonitor fileUpdateMonitor,
                                    ProgressIndicator progressIndicator) {
        this.bibdatabaseContext = bibdatabaseContext;
        newDatabaseContext = new BibDatabaseContext(new Defaults(BibDatabaseMode.BIBTEX));
        this.dialogService = dialogService;
        currentCitationfetcher = new GrobidCitationFetcher(
            jabRefPreferences,
            fileUpdateMonitor
        );
        this.progressIndicator = progressIndicator;
    }

    public StringProperty inputTextProperty() {
        return this.inputTextProperty;
    }

    public void startParsing(boolean directAdd) {
        this.directAdd = directAdd;
        this.extractedEntries = null;
        /*Task<Void> parseUsingGrobid = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    extractedEntries = currentCitationfetcher.performSearch(inputTextProperty.getValue());
                } catch (FetcherException e) {
                    extractedEntries = new ArrayList<>();
                }
                Platform.runLater(() -> executeParse());
                return null;
            }
        };*/
        //dialogService.showProgressDialogAndWait(Localization.lang("Your text is being parsed.."),Localization.lang( "Please wait while we are parsing your text"), task);
        BackgroundTask.wrap(() -> currentCitationfetcher.performSearch(inputTextProperty.getValue()))
                .onRunning(() -> parsingInProgress.setValue(true))
                .onFinished(() -> parsingInProgress.setValue(false))
                .onSuccess(parsedEntries -> {
                  //progressIndicator.setVisible(false);
                  extractedEntries = parsedEntries;
                  Platform.runLater(() -> executeParse());
                })
                .onFailure(exception -> {
                  //progressIndicator.setVisible(false);
                  extractedEntries = new ArrayList<>();
                  Platform.runLater(() -> executeParse());
                }).executeWith(Globals.TASK_EXECUTOR);
        //Globals.TASK_EXECUTOR.execute(task);
    }

    public void executeParse() {
        if (extractedEntries.size() > 0) {
            if (directAdd) {
                BibtexKeyGenerator bibtexKeyGenerator = new BibtexKeyGenerator(newDatabaseContext, Globals.prefs.getBibtexKeyPatternPreferences());
                for (BibEntry bibEntries: extractedEntries) {
                    bibtexKeyGenerator.generateAndSetKey(bibEntries);
                    newDatabaseContext.getDatabase().insertEntry(bibEntries);
                }
                newDatabaseContext.setMode(BibDatabaseMode.BIBTEX);
                JabRefGUI.getMainFrame().addTab(newDatabaseContext,true);
            } else {
                for (BibEntry bibEntry: extractedEntries) {
                    this.bibdatabaseContext.getDatabase().insertEntry(bibEntry);
                    JabRefGUI.getMainFrame().getCurrentBasePanel().showAndEdit(bibEntry);
                    trackNewEntry(StandardEntryType.Article);
                }
            }
            if (currentCitationfetcher.getFailedEntries().size() > 0) {
                dialogService.showWarningDialogAndWait(Localization.lang("Grobid failed to parse the following entries:"), String.join("\n;;\n", currentCitationfetcher.getFailedEntries()));
            }
        }
        dialogService.notify(Localization.lang("Successfully added a new entry."));
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

  public boolean isParsingInProgress() {
    return parsingInProgress.get();
  }
}
