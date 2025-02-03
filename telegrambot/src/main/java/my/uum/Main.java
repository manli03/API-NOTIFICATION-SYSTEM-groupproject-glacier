package my.uum;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * The my.uum.Main class is the entry point for the application.
 * @author Aiman Norazli
 */
public class Main {

    /**
     * The main method initializes and starts the Telegram bot.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            // Initialize the Telegram bot using TelegramBotsApi
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBots());
        } catch (TelegramApiException e) {
            // Handle any errors that occur during bot initialization
            System.err.println("An error occurred while initializing the bot: " + e.getMessage());
        }
    }
}
