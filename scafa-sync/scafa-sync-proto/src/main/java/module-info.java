module com.github.apetrelli.scafa.sync.proto {
	requires java.logging;
	requires lombok;
	requires transitive com.github.apetrelli.scafa.proto;
	exports com.github.apetrelli.scafa.sync.proto;
	exports com.github.apetrelli.scafa.sync.proto.client;
	exports com.github.apetrelli.scafa.sync.proto.processor;
	exports com.github.apetrelli.scafa.sync.proto.processor.impl;
}