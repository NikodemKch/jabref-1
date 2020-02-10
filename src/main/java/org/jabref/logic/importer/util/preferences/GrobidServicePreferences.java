package org.jabref.logic.importer.util.preferences;

public class GrobidServicePreferences {

    private final boolean useCustomGrobidServer;
    private final String customGrobidServer;

    public GrobidServicePreferences(boolean useCustomGrobidServer, String customGrobidServer) {
        this.useCustomGrobidServer = useCustomGrobidServer;
        this.customGrobidServer = customGrobidServer;
    }

    public final boolean isUseCustomGrobidServer() {
        return useCustomGrobidServer;
    }

    public final String getCustomGrobidServer() {
        return customGrobidServer;
    }

}
