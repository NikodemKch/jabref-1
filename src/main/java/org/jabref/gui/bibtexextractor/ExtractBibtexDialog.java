package org.jabref.gui.bibtexextractor;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;

import org.jabref.gui.StateManager;
import org.jabref.gui.entrybyplaintext.EntryByPlainTextAction;
import org.jabref.gui.entrybyplaintext.EntryByPlainTextViewModel;
import org.jabref.gui.util.BaseDialog;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;

import com.airhacks.afterburner.views.ViewLoader;

/**
 * GUI Dialog for the feature "Extract BibTeX from plain text".
 */
public class ExtractBibtexDialog extends BaseDialog<Void> {

    private final Button buttonExtract;
    private final Button buttonParse;
    @FXML private TextArea input;
    @FXML private ButtonType extractButtonType;
    @FXML private ButtonType parseButtonType;
    private BibtexExtractorViewModel viewModel;
    private EntryByPlainTextViewModel textViewModel;
    @Inject private StateManager stateManager;

    public ExtractBibtexDialog() {
        ViewLoader.view(this)
                  .load()
                  .setAsDialogPane(this);
        //TODO: hört sich mad scheiße an so
<<<<<<< HEAD
        this.setTitle(Localization.lang("Jabref Parser"));
=======
        this.setTitle(Localization.lang("Input text to parse"));
        input.setPromptText(Localization.lang("Please enter the text to extract from."));
        input.selectAll();
>>>>>>> 9e864419b06e63df298cc5d58ab973d155b66bda

        buttonExtract = (Button) getDialogPane().lookupButton(extractButtonType);
        buttonParse = (Button) getDialogPane().lookupButton(parseButtonType);
        buttonParse.setOnAction(event -> textViewModel.startParsing());
        buttonExtract.setOnAction(e -> viewModel.startExtraction());
        buttonParse.disableProperty().bind(viewModel.inputTextProperty().isEmpty());
        buttonExtract.disableProperty().bind(viewModel.inputTextProperty().isEmpty());
    }

    @FXML
    private void initialize() {
        BibDatabaseContext database = stateManager.getActiveDatabase().orElseThrow(() -> new NullPointerException("Database null"));
        this.viewModel = new BibtexExtractorViewModel(database);
        this.textViewModel = new EntryByPlainTextViewModel(database);
        input.textProperty().bindBidirectional(viewModel.inputTextProperty());
    }
}
