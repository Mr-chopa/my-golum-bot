package com.codepost.bot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
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
		System.out.println("호출됨");
		if (update.hasInlineQuery() && update.getInlineQuery().hasQuery()) {
			InlineQuery query = update.getInlineQuery();
			
			AnswerInlineQuery answer;
			
			try {
				int offset = 1;
				
				if(query.getOffset() != null && !"".equals(query.getOffset())) {
					offset = Integer.parseInt(query.getOffset());
				}
				
				answer = ImageStore.getInstance().search(imageSearch, query.getFrom(), query.getQuery(), offset, COUNT);
				answer.setInlineQueryId(query.getId());
				
	            execute(answer);
			} catch(Exception e) {
				try {
					answer = new AnswerInlineQuery();
					
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
					e.printStackTrace();
				}
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
