package edu.upvictoria.sqlcodeeditor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLEditorController {
    private WebSocketClient webSocketClient;

    @FXML
    private Button runButton;

    @FXML
    private CodeArea codeArea;

    @FXML
    private TextArea consoleArea;

    private static final String[] KEYWORDS = {
            "SELECT", "SET", "FROM", "DELETE", "NUMBER", "REFERENCES", "WHERE", "INSERT INTO", "UPDATE", "DELETE FROM", "CREATE", "ALTER", "DROP", "TABLE", "JOIN", "ON", "VARCHAR", "NOT NULL", "PRIMARY KEY", "BOOLEAN", "FOREIGN KEY", "UNIQUE", "USE DATABASE"
    };

    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b", Pattern.CASE_INSENSITIVE);

    @FXML
    public void initialize() {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                });

        runButton.setOnAction(event -> handleRunSQL());

        try {
            URI serverUri = new URI("ws://localhost:7777");
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onMessage(String message) {
                    consoleArea.appendText("Received: \n" + message + "\n");
                }

                @Override
                public void onOpen(ServerHandshake handShakeData) {
                    consoleArea.appendText("Connected to server\n");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    consoleArea.appendText("Connection closed: " + reason + "\n");
                }

                @Override
                public void onError(Exception ex) {
                    consoleArea.appendText("Error: " + ex.getMessage() + "\n");
                }
            };
            webSocketClient.connectBlocking();
        } catch (Exception e) {
            consoleArea.appendText("Error: " + e.getMessage() + "\n");
        }
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = KEYWORD_PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastEnd = 0;
        while (matcher.find()) {
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
            spansBuilder.add(Collections.singleton("keyword"), matcher.end() - matcher.start());
            lastEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        return spansBuilder.create();
    }

    private void handleRunSQL() {
        String selectedText = codeArea.getSelectedText();
        if (selectedText.isEmpty())
            selectedText = codeArea.getText();

        List<String> sqlCommands = parseSQLCommands(selectedText);

        for (String command : sqlCommands)
            sendMessage(command);
    }

    private List<String> parseSQLCommands(String text) {
        String[] commands = text.split(";");
        List<String> sqlCommands = new ArrayList<>();
        for (String command : commands) {
            command = command.trim();
            if (!command.isEmpty())
                sqlCommands.add(command + ";");
        }
        return sqlCommands;
    }

    public void sendMessage(String message) {
        if (webSocketClient == null || !webSocketClient.isOpen()) {
            consoleArea.appendText("There is not connection to the database");
            return;
        }

        webSocketClient.send(message);
        consoleArea.appendText("Sent: " + message + "\n");
    }
}
