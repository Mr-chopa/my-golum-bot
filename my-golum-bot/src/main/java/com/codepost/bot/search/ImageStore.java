package com.codepost.bot.search;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.objects.User;

import com.fasterxml.jackson.databind.JsonNode;

public class ImageStore {
	private final Log log = LogFactory.getLog(getClass());
	
	private Map<Integer, StoredItem> map;
	
	private static final ImageStore instance;
	
	static {
		instance = new ImageStore();
	}
	
	private ImageStore() {
		map = new HashMap<Integer, StoredItem>();
	}
	
	public static ImageStore getInstance() {
		return instance;
	}
	
	public AnswerInlineQuery search(ImageSearch is, User user, String query, int offset, int count) {
		StoredItem item = map.get(user.getId());
		
		// 유저 신규 조회 or 신규 쿼리 or 다음페이지
		if(item == null 
				|| !item.getQuery().equals(query)
				|| (item.getQuery().equals(query) && item.getCount()==is.maxCount() && item.getCount()*item.getRound()<item.getOffset())) {
			log.debug("search api called");
			
			JsonNode obj = is.search(query, offset, is.maxCount());
			
			StoredItem newItem = new StoredItem();
			newItem.setCount(is.count(obj));
			newItem.setRound(item == null ? 1 : item.getQuery().equals(query) ? item.getRound()+1 : 1);
			newItem.setObj(obj);
			newItem.setOffset(offset);
			newItem.setQuery(query);
			
			log.debug("new stored item : " + newItem.toString());
			
			map.put(user.getId(), newItem);
			
			item = newItem;
		}
		
		return is.getAnswer(item, offset, count);
	}
}
