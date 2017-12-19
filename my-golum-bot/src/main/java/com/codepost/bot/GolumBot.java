package com.codepost.bot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.codepost.bot.search.ImageSearch;
import com.codepost.bot.search.ImageStore;

@Component
public class GolumBot extends TelegramLongPollingBot {
	private final Log log = LogFactory.getLog(getClass());
	
	public static final int COUNT = 30;
	
	@Value("${telegram.bot.token}")
	private String token;
	
	@Value("${telegram.bot.username}")
	private String username;
	
	@Autowired
	private ImageSearch imageSearch;
	
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
		log.debug("onUpdateReceived() called");
		
		if (update.hasInlineQuery() && update.getInlineQuery().hasQuery()) {
			log.debug("inline query called");
			
			InlineQuery query = update.getInlineQuery();
			
			AnswerInlineQuery answer;
			
			try {
				int offset = 1;
				
				if(query.getOffset() != null && !"".equals(query.getOffset())) {
					offset = Integer.parseInt(query.getOffset());
				}
				
				log.debug(query);
				
				answer = ImageStore.getInstance().search(imageSearch, query.getFrom(), query.getQuery(), offset, COUNT);
				answer.setInlineQueryId(query.getId());
				
	            execute(answer);
			} catch(Exception e) {
				try {
					answer = new AnswerInlineQuery();
					answer.setInlineQueryId(query.getId());
					
					InlineQueryResultArticle result = new InlineQueryResultArticle();
					result.setId("error");
					result.setTitle("Error");
					result.setDescription("Bot error message");
					result.setInputMessageContent(new InputTextMessageContent().setMessageText(makeExceptionMsg(e)));
					
					List<InlineQueryResult> list = new ArrayList<InlineQueryResult>();
					list.add(result);
					
					answer.setResults(list);
					
					execute(answer);
				} catch(TelegramApiException e1) {
					log.error("exception message response error - " + e1.getMessage(), e1);
				}
				
				log.warn(e.getMessage(), e);
			}
		}
		else if(update.hasMessage() && update.getMessage().hasText()) {
			log.debug("text message called");
			
			SendMessage message = new SendMessage();
			
			try {
				message.setChatId(update.getMessage().getChatId());
				message.setText("@my_golum_bot [검색어]");
				
				execute(message);
			} catch(Exception e) {
				try {
					message.setText(makeExceptionMsg(e));
					
					execute(message);
				} catch(TelegramApiException e1) {
					log.error("exception message response error - " + e1.getMessage(), e1);
				}
				
				log.warn(e.getMessage(), e);
			}
		}
	}
	
	private String makeExceptionMsg(Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.getMessage()).append("\n");
		
		StackTraceElement[] stacks = e.getStackTrace();
		StackTraceElement stack;
		
		for(int i=0; i<stacks.length; i++) {
			stack = stacks[i];
			
			sb.append("\tat ").append(stack.getClassName()).append(".").append(stack.getMethodName()).append("(").append(stack.getFileName()).append(":").append(stack.getLineNumber()).append(")\n");
		}
		
		return sb.toString();
	}
}
