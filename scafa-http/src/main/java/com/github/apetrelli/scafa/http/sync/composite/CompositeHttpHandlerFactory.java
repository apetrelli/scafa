package com.github.apetrelli.scafa.http.sync.composite;

import java.util.ArrayList;
import java.util.List;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.composite.CompositeHttpHandler.PatternHandlerPair;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

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

	private PatternHandlerFactoryPair[] pairs;
	
	private HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> defaultHandlerFactory;

	private CompositeHttpHandlerFactory(HandlerFactory<HttpHandler, ? super HttpSyncSocket<HttpResponse>> defaultHandlerFactory, PatternHandlerFactoryPair[] pairs) {
		this.pairs = pairs;
		this.defaultHandlerFactory = defaultHandlerFactory;
	}

	@Override
	public HttpHandler create(HttpSyncSocket<HttpResponse> sourceChannel) {
		PatternHandlerPair[] handlerPairs = new PatternHandlerPair[pairs.length];
		for (int i=0; i < pairs.length; i++) {
			handlerPairs[i] = new PatternHandlerPair(pairs[i].pattern, pairs[i].handlerFactory.create(sourceChannel));
		}
		return new CompositeHttpHandler(defaultHandlerFactory.create(sourceChannel), handlerPairs);
	}

}
