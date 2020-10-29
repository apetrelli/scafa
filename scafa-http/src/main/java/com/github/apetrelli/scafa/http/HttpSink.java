package com.github.apetrelli.scafa.http;

public interface HttpSink<H, R> {
	
	void onStart(H handler);
	
	R endHeader(HttpProcessingContext context, H handler);
	
	R endHeaderAndRequest(HttpProcessingContext context, H handler);
	
	R data(HttpProcessingContext context, H handler);
	
	R chunkData(HttpProcessingContext context, H handler);
	
	R endChunkCount(HttpProcessingContext context, H handler);
	
	R onChunkEnd(H handler);
	
	R onDataToPassAlong(HttpProcessingContext context, H handler);
	
	R completed();
}
