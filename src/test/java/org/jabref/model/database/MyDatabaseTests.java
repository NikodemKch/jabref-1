package org.jabref.model.database;


import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.OpenDatabase;
import org.jabref.logic.importer.ParseException;
import org.jabref.logic.importer.fileformat.BibtexParser;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.util.DummyFileUpdateMonitor;
import org.jabref.model.util.FileUpdateMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class MyDatabaseTests {

  private ImportFormatPreferences importFormatDatabasePreferences;
  private BibDatabase myDatabase;
  private final File loadDatabase;
  private final FileUpdateMonitor fileUpdateMonitor = new DummyFileUpdateMonitor();

  public MyDatabaseTests() {
    loadDatabase = Paths.get("src/test/resources/testbib/testjabref.bib").toFile();
  }

  @BeforeEach
  public void setup() throws IOException {
    importFormatDatabasePreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
    myDatabase = OpenDatabase.loadDatabase(loadDatabase, importFormatDatabasePreferences, fileUpdateMonitor).getDatabase();
  }

  @Test
  public void correctlyCountsNumberOfEntries() {
    assertEquals(6, myDatabase.getEntries().size());
    assertEquals(6, myDatabase.getEntryCount());
  }

  @Test
  public void getEntriesSortedOnFieldYear() {
    List<BibEntry> list = myDatabase.getEntriesSorted(new Comparator<BibEntry>() {
      @Override
      public int compare(BibEntry bibEntryOne, BibEntry bibEntryTwo) {
        return bibEntryOne.getField(StandardField.YEAR).get()
            .compareTo(bibEntryTwo.getField(StandardField.YEAR).get()) ;
      }
    });
    assertTrue(list.get(0).getField(StandardField.YEAR).isPresent());
    assertEquals(1996, Integer.parseInt(list.get(0).getField(StandardField.YEAR).get()));
    assertTrue(list.get(1).getField(StandardField.YEAR).isPresent());
    assertEquals(1997, Integer.parseInt(list.get(1).getField(StandardField.YEAR).get()));
    assertTrue(list.get(2).getField(StandardField.YEAR).isPresent());
    assertEquals(1998, Integer.parseInt(list.get(2).getField(StandardField.YEAR).get()));
    assertTrue(list.get(3).getField(StandardField.YEAR).isPresent());
    assertEquals(2008, Integer.parseInt(list.get(3).getField(StandardField.YEAR).get()));
    assertTrue(list.get(4).getField(StandardField.YEAR).isPresent());
    assertEquals(2009, Integer.parseInt(list.get(4).getField(StandardField.YEAR).get()));
    assertTrue(list.get(5).getField(StandardField.YEAR).isPresent());
    assertEquals(2009, Integer.parseInt(list.get(5).getField(StandardField.YEAR).get()));
  }

  @Test
  public void getEntryByCitationKeyFindsEntry() {
    assertTrue(myDatabase.getEntryByKey("Brewer1997Isolationandcultureofadultrathippocampalneurons.JNeurosciMethods").isPresent());
    assertEquals("Isolation and culture of adult rat hippocampal neurons.",
        myDatabase.getEntryByKey("Brewer1997Isolationandcultureofadultrathippocampalneurons.JNeurosciMethods").get().getField(StandardField.TITLE).get());
  }

  @Test
  public void getEntryByKeyDoesNotFindInexistentEntry() {
    assertFalse(myDatabase.getEntryByKey("ThisIsNotAKeySoTheEntryIsNotPresent").isPresent());
  }

  @Test
  public void newlyInsertedArticleCanBeRetrievedByCitationKey() throws ParseException {
    Optional<BibEntry> bibEntries = BibtexParser.singleFromString("@article{lamport1978time,\n" +
        "  title={Time, clocks, and the ordering of events in a distributed system},\n" +
        "  author={Lamport, Leslie},\n" +
        "  journal={Communications of the ACM},\n" +
        "  volume={21},\n" +
        "  number={7},\n" +
        "  pages={558--565},\n" +
        "  year={1978},\n" +
        "  publisher={ACM}\n" +
        "}", importFormatDatabasePreferences, fileUpdateMonitor);
    assertTrue(bibEntries.isPresent());
    myDatabase.insertEntry(bibEntries.get());
    assertTrue(myDatabase.getEntryByKey("lamport1978time").isPresent());
    assertEquals("Time, clocks, and the ordering of events in a distributed system", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.TITLE).get());
    assertEquals("Lamport, Leslie", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.AUTHOR).get());
    assertEquals("Communications of the ACM", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.JOURNAL).get());
    assertEquals("21", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.VOLUME).get());
    assertEquals("7", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.NUMBER).get());
    assertEquals("558--565", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.PAGES).get());
    assertEquals("1978", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.YEAR).get());
    assertEquals("ACM", myDatabase.getEntryByKey("lamport1978time").get().getField(StandardField.PUBLISHER).get());
  }

}
