package com.codepost.bot.search;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultPhoto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(ImageSearch.NAME)
@ConditionalOnExpression("'${telegram.bot.api}' eq 'naver'")
public class NaverImageSearch implements ImageSearch {
	private final Log log = LogFactory.getLog(getClass());
	
	/** 최대 조회 건수(Naver API 제한) */
	@Value("${naver.api.search.maxCount}")
	private int maxCount;
	
	@Value("${naver.api.search.clientId}")
	private String clientId;
	
	@Value("${naver.api.search.clientSecret}")
	private String clientSecret;
	
	@Value("${naver.api.search.query}")
	private String searchQuery;

	@Override
	public JsonNode search(String query, int offset, int count) {
		BufferedReader br = null;
		HttpURLConnection conn = null;
		
		String searchUrl = null;
		
		try {
			searchUrl = searchQuery.replace("{query}", URLEncoder.encode(query, "UTF-8"))
					.replace("{display}", Integer.toString(maxCount))
					.replace("{start}", Integer.toString(offset));
			
			log.debug("searchUrl : " + searchUrl);
			
			URL url = new URL(searchUrl);
			conn = (HttpURLConnection)url.openConnection();
			
			conn.setRequestMethod("GET");
			conn.setRequestProperty("X-NAVER-Client-Id", clientId);
			conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);
			
			int responseCode = conn.getResponseCode();
			
			ObjectMapper mapper = new ObjectMapper();
			
			if(responseCode == 200) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				
				StringBuilder sb = new StringBuilder();
				String s = null;
				while((s = br.readLine()) != null) {
					sb.append(s).append("\n");
				}
				
				throw new RuntimeException(sb.toString());
			}
			
			return mapper.readTree(br);
		} catch(Exception e) {
			log.warn("searchUrl : " + searchUrl);
			log.warn("X-NVAER-Client-Id : [" + clientId + "]");
			log.warn("X-Naver-Client-Secret : [" + clientSecret + "]");
			
			throw new RuntimeException(e);
		} finally {
			try { if(br != null) br.close(); } catch(Exception ex) {}
			try { if(conn != null) conn.disconnect(); } catch(Exception ex) {}
		}
	}

	@Override
	public int maxCount() {
		return maxCount;
	}

	@Override
	public AnswerInlineQuery getAnswer(StoredItem item, int offset, int count) {
		JsonNode obj = item.getObj();
		
		JsonNode arr = obj.get("items");
		
		AnswerInlineQuery answer = new AnswerInlineQuery();
		List<InlineQueryResult> list = new ArrayList<InlineQueryResult>();
		InlineQueryResultPhoto result = null;
		JsonNode tmp;
		
		int startOffset = offset > arr.size() ? offset-((item.getRound()-1)*item.getCount()) : offset;
		int to = arr.size() < startOffset+count-1 ? arr.size() : startOffset+count-1;
		
		int cnt = 0;
		
		for(int i=startOffset-1; i<to; i++, cnt++) {
			try {
				tmp = arr.get(i);
			
				result = new InlineQueryResultPhoto();
				result.setId(Integer.toString(offset+i));
				result.setPhotoUrl(tmp.get("link").asText());
				result.setThumbUrl(tmp.get("thumbnail").asText());
				
				list.add(result);
			} catch(Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		
		int nextOffset = offset+cnt;
		
		answer.setResults(list);
		answer.setNextOffset(Integer.toString(nextOffset));
		item.setOffset(nextOffset);
		
		if(log.isDebugEnabled()) {
			log.debug("answer cnt : " + list.size());
			log.debug("next offset : " + nextOffset);
		}
		
		return answer;
	}

	@Override
	public int count(JsonNode obj) {
		return obj.get("display").asInt();
	}
}
