package org.jabref.gui.silverbullet;

import com.google.common.eventbus.Subscribe;
import org.jabref.gui.DialogService;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.event.EntryAddedEvent;
import org.jabref.model.entry.event.EntryChangedEvent;
import org.jabref.model.entry.field.StandardField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SilverBulletHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(SilverBulletHandler.class);
  private static DialogService dialogService;

  public SilverBulletHandler(DialogService dialogService){
    this.dialogService = dialogService;
  }

  public SilverBulletHandler(){

  }

  @Subscribe
  public void listen(EntryAddedEvent addedEvent){
    LOGGER.info("Entry Added");

    //System.out.println("Entry Added");
  }
  @Subscribe
  public void listen(EntryChangedEvent changedEvent){
    if (changedEvent.getBibEntry().getField(StandardField.TITLE).isPresent()){


      LOGGER.info(changedEvent.getBibEntry().getField(StandardField.TITLE).get());
      //System.out.println(changedEvent.getBibEntry().getField(StandardField.TITLE).get());
    }
    if(changedEvent.getBibEntry().getTitle().get().equals("no silver bullet")){
      dialogService.showInformationDialogAndWait(Localization.lang("no_silver_bullet"),Localization.lang("no_silver_bullet_content"));
    }
  }

}
