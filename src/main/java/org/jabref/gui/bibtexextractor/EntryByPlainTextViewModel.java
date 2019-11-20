package org.jabref.gui.bibtexextractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


import javafx.concurrent.Task;
import org.jabref.Globals;
import org.jabref.JabRefGUI;
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

public class EntryByPlainTextViewModel {

  private final StringProperty inputText = new SimpleStringProperty("");
  private final BibDatabaseContext bibDatabaseContext;
  private boolean directAdd;
  private final BibDatabaseContext newDatabaseContext;
  private List<BibEntry> extractedEntries;
  private boolean waitingForParser;


  public EntryByPlainTextViewModel(BibDatabaseContext bibDatabaseContext){

    this.bibDatabaseContext = bibDatabaseContext;
    newDatabaseContext = new BibDatabaseContext(new Defaults(BibDatabaseMode.BIBTEX));

  }

  public StringProperty inputTextProperty(){

    return this.inputText;

  }

  public void startParsing(boolean directAdd){
    this.directAdd = directAdd;
    this.extractedEntries = null;
    Task<List<BibEntry>> parsingTask = new Task<List<BibEntry>>() {

        @Override
        protected List<BibEntry> call() {
            try {
                extractedEntries = new GrobidCitationFetcher(
                        JabRefPreferences.getInstance().getImportFormatPreferences(),
                        Globals.getFileUpdateMonitor()
                ).performSearch(inputText.getValue());
            } catch (FetcherException e) {
                //TODO: make this a new seperate class just busy with the parsing
            }
            return extractedEntries;
        }
    };
      JabRefGUI.getMainFrame().getDialogService().showProgressDialogAndWait("Parsing entries", "", parsingTask);

  }

  public void executeParse(){
      if(extractedEntries.size() > 0){
          BibtexKeyGenerator bibtexKeyGenerator = new BibtexKeyGenerator(newDatabaseContext, Globals.prefs.getBibtexKeyPatternPreferences());
          for (BibEntry bibEntry: extractedEntries) {
              parsingSuccess(bibEntry);
              bibtexKeyGenerator.generateAndSetKey(bibEntry);
          }
      } else{
          parsingFail(inputText.getValue());
      }
  }

  public void parsingSuccess(BibEntry bibEntry){
      if(directAdd) {

          newDatabaseContext.getDatabase().insertEntry(bibEntry);
          newDatabaseContext.setMode(BibDatabaseMode.BIBTEX);
          JabRefGUI.getMainFrame().addTab(newDatabaseContext,true);
      }else{
          this.bibDatabaseContext.getDatabase().insertEntry(bibEntry);
          JabRefGUI.getMainFrame().getCurrentBasePanel().showAndEdit(bibEntry);
          trackNewEntry(StandardEntryType.Article);


      }
      JabRefGUI.getMainFrame().getDialogService().notify("Successfully added a new entry.");
  }

  public void parsingFail(String input){
      FailedToParseDialog dlg = new FailedToParseDialog(input);
      dlg.showAndWait();
  }



  private void trackNewEntry(EntryType type) {
    Map<String, String> properties = new HashMap<>();
    properties.put("EntryType", type.getName());

    Globals.getTelemetryClient().ifPresent(client -> client.trackEvent("NewEntry", properties, new HashMap<>()));
  }

  public void setWaitingForParser(boolean bool){
      this.waitingForParser = bool;
  }
}
