package org.jabref.gui.bibtexextractor;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.util.BaseDialog;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;

import com.airhacks.afterburner.views.ViewLoader;
import org.jabref.model.util.FileUpdateMonitor;
import org.jabref.preferences.JabRefPreferences;

/**
 * GUI Dialog for the feature "Extract BibTeX from plain text".
 */
public class ExtractBibtexDialog extends BaseDialog<Void> {

    private final Button buttonParse;
    @FXML private TextArea input;
    @FXML private ButtonType parseButtonType;
    private BibtexExtractorViewModel viewModel;
    private boolean directAdd;
    @Inject private StateManager stateManager;
    @Inject private DialogService dialogService;
    @Inject private FileUpdateMonitor fileUpdateMonitor;
    @Inject private TaskExecutor taskExecutor;

    public ExtractBibtexDialog() {
        ViewLoader.view(this)
                  .load()
                  .setAsDialogPane(this);
        this.setTitle(Localization.lang("JabRef Parser"));
        input.setPromptText(Localization.lang("Please enter the text to extract from."));
        input.selectAll();

        buttonParse = (Button) getDialogPane().lookupButton(parseButtonType);
        buttonParse.setOnAction(event -> {
          directAdd = false;
          //progressIndicator.setVisible(true);
          viewModel.startParsing();
        });
        buttonParse.disableProperty().bind(viewModel.inputTextProperty().isEmpty());

      //progressIndicator = new ProgressIndicator();
      //progressIndicator.setVisible(true);
      //getDialogPane().getChildren().add(progressIndicator);
    }

  public BibtexExtractorViewModel getViewModel() {
    return viewModel;
  }

  @FXML
    private void initialize() {
      //progressIndicator.setVisible(true);
        BibDatabaseContext database = stateManager.getActiveDatabase().orElseThrow(() -> new NullPointerException("Database null"));
        this.viewModel = new BibtexExtractorViewModel(database, dialogService, JabRefPreferences.getInstance(), fileUpdateMonitor, taskExecutor);
        input.textProperty().bindBidirectional(viewModel.inputTextProperty());
    }
}
