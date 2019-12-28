package org.jabref.gui.bibtexextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import javafx.scene.control.ProgressIndicator;
import javax.inject.Inject;
import org.jabref.Globals;
import org.jabref.JabRefGUI;
import org.jabref.gui.DialogService;
import org.jabref.gui.externalfiles.ImportHandler;
import org.jabref.logic.bibtexkeypattern.BibtexKeyGenerator;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.fetcher.GrobidCitationFetcher;
import org.jabref.logic.importer.fileformat.bibtexml.File;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.Defaults;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.util.FileUpdateMonitor;
import org.jabref.preferences.JabRefPreferences;
import org.jabref.gui.util.BackgroundTask;


public class BibtexExtractorViewModel {

    private final StringProperty inputTextProperty = new SimpleStringProperty("");
    private final BibDatabaseContext bibdatabaseContext;
    private List<BibEntry> extractedEntries;
    private final BibDatabaseContext newDatabaseContext;
    private boolean directAdd;
    @Inject private DialogService dialogService;
    private GrobidCitationFetcher currentCitationfetcher;
    @Inject private ImportHandler importHandler;
    public JabRefPreferences jabRefPreferences;



    public BibtexExtractorViewModel(BibDatabaseContext bibdatabaseContext,FileUpdateMonitor fileUpdateMonitor, JabRefPreferences jabRefPreferences) {
        this.bibdatabaseContext = bibdatabaseContext;
        this.jabRefPreferences = jabRefPreferences;
        newDatabaseContext = new BibDatabaseContext(new Defaults(BibDatabaseMode.BIBTEX));
        currentCitationfetcher = new GrobidCitationFetcher(jabRefPreferences.getImportFormatPreferences(),fileUpdateMonitor,jabRefPreferences);
    }

    public StringProperty inputTextProperty() {
        return this.inputTextProperty;
    }

    public void startParsing(boolean directAdd) {
        this.directAdd = directAdd;
        this.extractedEntries = null;
        ProgressIndicator progressIndicator = new ProgressIndicator();
        BackgroundTask.
            wrap(() -> extractedEntries = currentCitationfetcher.performSearch(inputTextProperty.getValue()))
            .onRunning(() ->{progressIndicator.setVisible(true);
            })
            .onSuccess(extractedEntries -> executeParse())
            .onFailure(e -> dialogService.showErrorDialogAndWait("JabRef could not execute your query."));
    }

    public void executeParse() {
        if (extractedEntries.size() > 0) {
            if (directAdd) {
              newDatabaseContext.setMode(BibDatabaseMode.BIBTEX);
              JabRefGUI.getMainFrame().addTab(newDatabaseContext,true);
              importHandler.importEntries(extractedEntries);
              trackNewEntry(StandardEntryType.Article);


            } else {
              importHandler.importEntries(extractedEntries);
              trackNewEntry(StandardEntryType.Article);

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
}
