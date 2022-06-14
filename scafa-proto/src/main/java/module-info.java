module com.github.apetrelli.scafa.proto {
	requires java.logging;
	requires lombok;
	exports com.github.apetrelli.scafa.proto;
	exports com.github.apetrelli.scafa.proto.client;
	exports com.github.apetrelli.scafa.proto.data;
	exports com.github.apetrelli.scafa.proto.data.impl;
	exports com.github.apetrelli.scafa.proto.processor;
	exports com.github.apetrelli.scafa.proto.util;
}