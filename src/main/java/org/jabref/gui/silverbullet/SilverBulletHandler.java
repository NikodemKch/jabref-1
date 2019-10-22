package org.jabref.gui.silverbullet;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jabref.model.database.event.EntryAddedEvent;

import org.jabref.model.entry.event.EntryChangedEvent;
import org.jabref.model.entry.field.StandardField;

public class SilverBulletHandler {

  @Subscribe
  public void listen(EntryAddedEvent addedEvent){
    System.out.println("Entry Added");
  }
  @Subscribe
  public void listen(EntryChangedEvent changedEvent){
    if (changedEvent.getBibEntry().getField(StandardField.TITLE).isPresent()){
      System.out.println(changedEvent.getBibEntry().getField(StandardField.TITLE).get());
    }
  }
}
