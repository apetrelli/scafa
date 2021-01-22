module com.github.apetrelli.scafa.async.proto {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.proto;
	exports com.github.apetrelli.scafa.proto.async;
	exports com.github.apetrelli.scafa.proto.async.buffer;
	exports com.github.apetrelli.scafa.proto.async.client;
	exports com.github.apetrelli.scafa.proto.async.processor;
	exports com.github.apetrelli.scafa.proto.async.processor.impl;
	exports com.github.apetrelli.scafa.proto.async.socket;
	exports com.github.apetrelli.scafa.proto.async.util;
}