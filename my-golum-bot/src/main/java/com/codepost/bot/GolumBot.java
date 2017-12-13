package com.codepost.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

@Component
public class GolumBot extends TelegramLongPollingBot {

	@Value("${telegram.bot.token}")
	private String token;
	
	@Value("${telegram.bot.username}")
	private String username;
	
	@Override
	public String getBotUsername() {
		return username;
	}
	
	@Override
	public String getBotToken() {
		return token;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText(update.getMessage().getText());
            
            SendPhoto sendPhotoRequest = new SendPhoto();
            sendPhotoRequest.setChatId(update.getMessage().getChatId());
            sendPhotoRequest.setPhoto("https://www.google.co.kr/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0ahUKEwj3gJr9n4bYAhWENJQKHW2zBGEQjRwIBw&url=https%3A%2F%2Fko.wikipedia.org%2Fwiki%2F%25ED%2595%2598%25ED%258A%25B8_(%25EA%25B8%25B0%25ED%2598%25B8)&psig=AOvVaw1lR-XvIA0isROY4rDiB8-I&ust=1513229138712009");
            try {
            		sendPhoto(sendPhotoRequest);
                //execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
	}
}
