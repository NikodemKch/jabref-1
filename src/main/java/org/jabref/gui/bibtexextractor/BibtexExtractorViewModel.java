package org.jabref.gui.bibtexextractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javafx.concurrent.Task;
import org.jabref.Globals;
import org.jabref.JabRefExecutorService;
import org.jabref.JabRefGUI;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.bibtexkeypattern.BibtexKeyGenerator;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.fetcher.GrobidCitationFetcher;
import org.jabref.model.Defaults;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.preferences.JabRefPreferences;

public class BibtexExtractorViewModel {

    private final StringProperty inputTextProperty = new SimpleStringProperty("");
    private final BibDatabaseContext bibdatabaseContext;
    private List<BibEntry> extractedEntries;
    private final BibDatabaseContext newDatabaseContext;
    private boolean directAdd;
    private final TaskExecutor taskExecutor;

    public BibtexExtractorViewModel(BibDatabaseContext bibdatabaseContext) {
        this.bibdatabaseContext = bibdatabaseContext;
        newDatabaseContext = new BibDatabaseContext(new Defaults(BibDatabaseMode.BIBTEX));
        taskExecutor = Globals.TASK_EXECUTOR;
    }

    public StringProperty inputTextProperty() {
        return this.inputTextProperty;
    }

    public void parse() {
        Task<Void> extract = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    extractedEntries = new GrobidCitationFetcher(
                            JabRefPreferences.getInstance().getImportFormatPreferences(),
                            Globals.getFileUpdateMonitor()
                    ).performSearch(inputTextProperty.getValue());
                } catch (FetcherException e) {
                    //TODO
                }

                return null;
            }
        };
        extract.setOnSucceeded(value -> executeParse());

        JabRefGUI.getMainFrame().getDialogService().showProgressDialogAndWait("Parsing", "Please wait while we parse your input.", extract);
        taskExecutor.execute(extract);
    }


  public void startParsing(){
        this.extractedEntries = null;
        parse();
        directAdd = false;
        Platform.runLater(this::executeParse);
  }

  public void startParsingToNewLibrary(){
      this.extractedEntries = null;
      parse();
      directAdd = true;
      Platform.runLater(this::executeParse);
  }

  public void executeParse(){

    if(directAdd){
      if(extractedEntries.size() > 0){
        BibtexKeyGenerator bibtexKeyGenerator = new BibtexKeyGenerator(newDatabaseContext, Globals.prefs.getBibtexKeyPatternPreferences());
        for(BibEntry bibEntries: extractedEntries) {
          bibtexKeyGenerator.generateAndSetKey(bibEntries);
          newDatabaseContext.getDatabase().insertEntry(bibEntries);
        }
        newDatabaseContext.setMode(BibDatabaseMode.BIBTEX);
        JabRefGUI.getMainFrame().addTab(newDatabaseContext,true);
        JabRefGUI.getMainFrame().getDialogService().notify("Successfully added a new entry.");

      } else{
        //TODO implement parsing fail
      }
    } else{

      for(BibEntry bibEntry: extractedEntries) {
        this.bibdatabaseContext.getDatabase().insertEntry(bibEntry);
        JabRefGUI.getMainFrame().getCurrentBasePanel().showAndEdit(bibEntry);
        trackNewEntry(StandardEntryType.Article);
      }
      JabRefGUI.getMainFrame().getDialogService().notify("Successfully added a new entry.");
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
