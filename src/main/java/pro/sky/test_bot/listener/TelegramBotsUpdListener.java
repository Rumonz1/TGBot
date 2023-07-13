package pro.sky.test_bot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.test_bot.entity.NotificationTask;
import pro.sky.test_bot.service.NotificationTaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotsUpdListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotsUpdListener.class);
    private final Pattern pattern = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})\\s+([А-я\\d\\s.,!?:;]+)");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    public TelegramBotsUpdListener(TelegramBot telegramBot,NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService= notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message() !=null)
                    .forEach(update -> {
                logger.info("Handles update: {}", update);
                Message message = update.message();
                Long chatId = message.chat().id();
                String text = message.text();

                if ("/start".equals(text)) {
                   sendMessage(chatId, """
                            Привет!
                            Я бот-помощник! Помогу запланировать задачу. 
                            Отправь её в формате: ДД.ММ.ГГГГ ЧЧ:ММ Пример задачи
                            """);

                } else if (text != null) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        LocalDateTime dateTime = parse(matcher.group(1));
                        if (Objects.isNull(dateTime)) {
                            sendMessage(chatId, "Некорректный формат даты и/или времени!");
                        }else{
                        String txt = matcher.group(2);
                            NotificationTask norificationTask = new NotificationTask();
                            norificationTask.setChatId(chatId);
                            norificationTask.setMessage(txt);
                            norificationTask.setNotificationDateTime(dateTime);
                            notificationTaskService.save(norificationTask);
                            sendMessage(chatId, "Задача запланирована!");
                        }

                    }else {
                        sendMessage(chatId, "Некорректный формат сообщения!");

                    }
                }


            });
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
@Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error during sending message: {}", sendResponse.description());
        }

    }
}
