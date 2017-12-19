package com.codepost.bot.search;

import org.telegram.telegrambots.api.methods.AnswerInlineQuery;

import com.fasterxml.jackson.databind.JsonNode;

public interface ImageSearch {
	public static String NAME = "ImageSearch";
	
	/**
	 * 이미지 검색
	 * @param query 검색어
	 * @param offset 검색결과 시작위치
	 * @param count 검색 개수
	 * @return JSONObject
	 */
	public JsonNode search(String query, int offset, int count);

	/**
	 * 검색 가능한 최대 건수 반환
	 * @return
	 */
	public int maxCount();
	
	/**
	 * ImageStore에 적재된 이미지를 telegram response(Answer)로 반환
	 * @param item
	 * @param offset
	 * @param count
	 * @return
	 */
	public AnswerInlineQuery getAnswer(StoredItem item, int offset, int count);
	
	/**
	 * 건수 반환
	 * @param obj
	 * @return
	 */
	public int count(JsonNode obj);
}
