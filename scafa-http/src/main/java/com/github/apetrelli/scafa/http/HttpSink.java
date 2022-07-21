package com.github.apetrelli.scafa.http;

public interface HttpSink<H> {
	
	void onStart(H handler);
	
	void endHeader(HttpProcessingContext context, H handler);
	
	void endHeaderAndRequest(HttpProcessingContext context, H handler);
	
	boolean data(HttpProcessingContext context, H handler);
	
	void chunkData(HttpProcessingContext context, H handler);
	
	boolean endChunkCount(HttpProcessingContext context, H handler);
	
	void onChunkEnd(H handler);
	
	void onDataToPassAlong(HttpProcessingContext context, H handler);
}
