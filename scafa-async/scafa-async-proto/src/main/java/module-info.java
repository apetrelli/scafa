module com.github.apetrelli.scafa.async.proto {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.proto;
	exports com.github.apetrelli.scafa.async.proto;
	exports com.github.apetrelli.scafa.async.proto.client;
	exports com.github.apetrelli.scafa.async.proto.processor;
	exports com.github.apetrelli.scafa.async.proto.processor.impl;
	exports com.github.apetrelli.scafa.async.proto.socket;
	exports com.github.apetrelli.scafa.async.proto.util;
}