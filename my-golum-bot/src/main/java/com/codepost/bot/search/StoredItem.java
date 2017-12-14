package com.codepost.bot.search;

import com.fasterxml.jackson.databind.JsonNode;

public class StoredItem {
	private String query;
	private int offset;
	private int round;
	private int count;
	private JsonNode obj;
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public JsonNode getObj() {
		return obj;
	}
	public void setObj(JsonNode obj) {
		this.obj = obj;
	}
}
