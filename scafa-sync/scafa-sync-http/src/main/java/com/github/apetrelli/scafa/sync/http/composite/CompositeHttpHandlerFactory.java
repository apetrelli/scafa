package com.github.apetrelli.scafa.sync.http.composite;

import java.util.ArrayList;
import java.util.List;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.composite.CompositeHttpHandler.PatternHandlerPair;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompositeHttpHandlerFactory implements HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> {

	public static class CompositeHttpHandlerFactoryBuilder {

		private List<PatternHandlerFactoryPair> pairs = new ArrayList<>();
		
		private HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> defaultHandlerFactory;
		
		public CompositeHttpHandlerFactoryBuilder withDefaultHandlerFactory(
				HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> defaultHandlerFactory) {
			this.defaultHandlerFactory = defaultHandlerFactory;
			return this;
		}
		
		public CompositeHttpHandlerFactoryBuilder withPattern(String pattern, HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> handlerFactory) {
			pairs.add(new PatternHandlerFactoryPair(pattern, handlerFactory));
			return this;
		}
		
		public CompositeHttpHandlerFactory build() {
			return new CompositeHttpHandlerFactory(defaultHandlerFactory,
					pairs.toArray(new PatternHandlerFactoryPair[pairs.size()]));
		}
	}
	
	private static class PatternHandlerFactoryPair {
		private String pattern;

		private HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> handlerFactory;

		public PatternHandlerFactoryPair(String pattern,
				HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> handlerFactory) {
			this.pattern = pattern;
			this.handlerFactory = handlerFactory;
		}
	}
	
	private final HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> defaultHandlerFactory;

	private final PatternHandlerFactoryPair[] pairs;

	@Override
	public HttpHandler create(HttpSyncSocket<HttpResponse> sourceChannel) {
		PatternHandlerPair[] handlerPairs = new PatternHandlerPair[pairs.length];
		for (int i=0; i < pairs.length; i++) {
			handlerPairs[i] = new PatternHandlerPair(pairs[i].pattern, pairs[i].handlerFactory.create(sourceChannel));
		}
		return new CompositeHttpHandler(defaultHandlerFactory.create(sourceChannel), handlerPairs);
	}

}
