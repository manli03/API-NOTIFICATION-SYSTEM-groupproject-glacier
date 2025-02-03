package my.uum;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class TelegramBots extends TelegramLongPollingBot {
    private CqlSession session;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> periodicTask;
    private final Set<Long> subscribedUsers = new HashSet<>();
    private final Map<String, Row> currentDeletedRows = new HashMap<>();
    private final Map<String, Row> currentChangedRows = new HashMap<>();
    private final Map<String, Row> currentNewDataRows = new HashMap<>();
    private final Map<String, String> lastSentMessageHashes = new HashMap<>();
    private String lastNotifyMessage = "No update for now";
    private static final Logger logger = Logger.getLogger(TelegramBots.class);

    public TelegramBots() {
        // Load subscribed users and last notify message from persistent storage
        loadSubscribedUsers();
        loadLastNotifyMessage();

        // Retry logic for Cassandra connection
        connectToCassandra();

        // Initialize the scheduled task for checking updates
        startPeriodicCheck();
    }

    private void connectToCassandra() {
        while (true) {
            try {
                session = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                        .withLocalDatacenter("datacenter1")
                        .withKeyspace("sparkdata")
                        .build();
                logger.info("Cassandra session initialized successfully.");
                break;
            } catch (Exception e) {
                logger.error("Error initializing Cassandra session: " + e.getMessage(), e);
                logger.info("Retrying connection to Cassandra...");
                try {
                    Thread.sleep(5000); // Wait for 5 seconds before retrying
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry wait", interruptedException);
                }
            }
        }
    }

    private void startPeriodicCheck() {
        // Schedule a task to run every 3 seconds to check for updates
        periodicTask = scheduler.scheduleAtFixedRate(this::checkForUpdatesPeriodically, 0, 3, TimeUnit.SECONDS);
    }

    private void stopPeriodicCheck() {
        if (periodicTask != null && !periodicTask.isCancelled()) {
            periodicTask.cancel(false);
        }
    }

    private void restartPeriodicCheck() {
        startPeriodicCheck();
    }

    private void checkForUpdatesPeriodically() {
        try {
            List<Row> updatedRows = fetchUpdatedShows();
            if (!updatedRows.isEmpty()) {
                classifyAndStoreUpdatedRows(updatedRows);
                notifyAllSubscribers();
            }
        } catch (Exception e) {
            logger.error("Error during periodic check: " + e.getMessage(), e);
        }
    }

    private List<Row> fetchUpdatedShows() {
        String query = "SELECT * FROM cust_data WHERE logg IN ('deleted', 'updated', 'new data') ALLOW FILTERING";
        ResultSet resultSet = session.execute(query);
        return resultSet.all();
    }

    private void classifyAndStoreUpdatedRows(List<Row> updatedRows) {
        for (Row row : updatedRows) {
            String logg = row.getString("logg");
            String showId = row.getString("show_id");

            if ("deleted".equals(logg)) {
                currentDeletedRows.put(showId, row);
            } else if ("updated".equals(logg)) {
                currentChangedRows.put(showId, row);
            } else if ("new data".equals(logg)) {
                currentNewDataRows.put(showId, row);
            }
        }
    }

    private void notifyAllSubscribers() {
        for (Long chatId : subscribedUsers) {
            if (!currentNewDataRows.isEmpty()) {
                sendNotifications(chatId, new ArrayList<>(currentNewDataRows.values()), "New Data", "✨");
            }
            if (!currentChangedRows.isEmpty()) {
                sendNotifications(chatId, new ArrayList<>(currentChangedRows.values()), "Changed Data", "🔄");
            }
            if (!currentDeletedRows.isEmpty()) {
                sendNotifications(chatId, new ArrayList<>(currentDeletedRows.values()), "Deleted Data", "❌");
            }
        }
        // Clear the maps after notifications have been sent
        currentDeletedRows.clear();
        currentChangedRows.clear();
        currentNewDataRows.clear();
    }

    private void sendNotifications(Long chatId, List<Row> rows, String updateType, String emoji) {
        for (Row row : rows) {
            StringBuilder message = new StringBuilder(emoji + " *" + updateType + ":*\n\n");
            message.append(buildNotificationMessage(row));
            String messageHash = message.toString().hashCode() + "";

            // Use a unique key for each user and show_id to track last sent message
            String userShowKey = chatId + "_" + row.getString("show_id");

            if (!messageHash.equals(lastSentMessageHashes.get(userShowKey))) {
                sendTelegramMessage(chatId, message.toString());
                lastSentMessageHashes.put(userShowKey, messageHash);
                lastNotifyMessage = message.toString();
                saveLastNotifyMessage();  // Save the last notify message after it is updated
                updateLoggColumn(row.getString("show_id"));

                // If the row is deleted, remove it from Cassandra after notification
                if ("❌".equals(emoji)) {
                    deleteRowFromCassandra(row.getString("show_id"));
                }
            }
        }
    }

    private StringBuilder buildNotificationMessage(Row row) {
        StringBuilder message = new StringBuilder();
        row.getColumnDefinitions().forEach(def -> {
            if (!def.getName().toString().equals("logg")) {
                message.append("*").append(def.getName().toString()).append(":* ")
                        .append(row.getObject(def.getName().toString())).append("\n");
            }
        });
        return message;
    }

    private void sendTelegramMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending Telegram message: " + e.getMessage(), e);
        }
    }

    private void updateLoggColumn(String showId) {
        String query = "UPDATE cust_data SET logg = 'no changes' WHERE show_id = ?";
        try {
            session.execute(session.prepare(query).bind(showId));
        } catch (Exception e) {
            logger.error("Error updating logg column: " + e.getMessage(), e);
        }
    }

    private void deleteRowFromCassandra(String showId) {
        String query = "DELETE FROM cust_data WHERE show_id = ?";
        try {
            session.execute(session.prepare(query).bind(showId));
        } catch (Exception e) {
            logger.error("Error deleting row from Cassandra: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        CompletableFuture.runAsync(() -> {
            try {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    Long chatId = update.getMessage().getChatId();
                    String inputText = update.getMessage().getText();

                    if (inputText.equals("/start")) {
                        sendStartMessage(chatId);
                    } else if (inputText.equals("/getlatest")) {
                        handleGetLatestCommand(chatId);
                    } else {
                        handleDefaultCommand(chatId);
                    }
                } else if (update.hasCallbackQuery()) {
                    handleCallback(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getMessage().getMessageId());
                }
            } catch (Exception e) {
                logger.error("Error processing update: " + e.getMessage(), e);
            }
        });
    }

    private void handleGetLatestCommand(Long chatId) {
        CompletableFuture.runAsync(() -> {
            try {
                stopPeriodicCheck();
                sendTelegramMessage(chatId, lastNotifyMessage);
                restartPeriodicCheck();
            } catch (Exception e) {
                logger.error("Error handling /getlatest command: " + e.getMessage(), e);
            }
        });
    }

    private void handleDefaultCommand(Long chatId) {
        sendTelegramMessage(chatId, "Unknown command. Please use /start to subscribe or /getlatest to get the latest update.");
    }

    private void handleCallback(String callData, Long chatId, Integer messageId) {
        if ("AGREE".equals(callData)) {
            removeAgreeButton(chatId, messageId);
            if (subscribedUsers.add(chatId)) {
                sendTelegramMessage(chatId, "You have successfully subscribed to receive updates!");
                saveSubscribedUsers();
            } else {
                sendTelegramMessage(chatId, "You are already subscribed.");
            }
        }
    }

    private void removeAgreeButton(Long chatId, Integer messageId) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId.toString());
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(null); // Clear the inline keyboard
        try {
            execute(editMessageReplyMarkup);
            logger.info("Agree button removed for chatId: " + chatId + ", messageId: " + messageId);
        } catch (TelegramApiException e) {
            logger.error("Error removing agree button: " + e.getMessage(), e);
        }
    }

    private void sendStartMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Welcome to the Update Notification Bot. Click 'Agree' to receive updates on new data.");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton agreeButton = new InlineKeyboardButton();
        agreeButton.setText("Agree");
        agreeButton.setCallbackData("AGREE");
        rowInline.add(agreeButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending start message: " + e.getMessage(), e);
        }
    }

    private void saveSubscribedUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("telegrambot/src/main/resources/subscribedUsers.txt"))) {
            for (Long userId : subscribedUsers) {
                writer.write(userId.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Error saving subscribed users: " + e.getMessage(), e);
        }
    }

    private void loadSubscribedUsers() {
        Path filePath = Paths.get("telegrambot/src/main/resources/subscribedUsers.txt");
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    subscribedUsers.add(Long.parseLong(line));
                }
            } catch (IOException e) {
                logger.error("Error loading subscribed users: " + e.getMessage(), e);
            }
        }
    }

    private void saveLastNotifyMessage() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("telegrambot/src/main/resources/lastNotifyMessage.txt"))) {
            writer.write(lastNotifyMessage);
        } catch (IOException e) {
            logger.error("Error saving last notify message: " + e.getMessage(), e);
        }
    }

    private void loadLastNotifyMessage() {
        Path filePath = Paths.get("telegrambot/src/main/resources/lastNotifyMessage.txt");
        if (Files.exists(filePath)) {
            try {
                lastNotifyMessage = new String(Files.readAllBytes(filePath));
            } catch (IOException e) {
                logger.error("Error loading last notify message: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onClosing() {
        try {
            if (session != null) {
                session.close();
            }
            scheduler.shutdown();
        } catch (Exception e) {
            logger.error("Error during shutdown: " + e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return "nam66_bot";  // Replace with your bot's username
    }

    @Override
    public String getBotToken() {
        return "5904903089:AAGDoHhmhgtjk7Sn4O86yjUpqM7VSf9422I";  // Replace with your bot's token
    }
}
