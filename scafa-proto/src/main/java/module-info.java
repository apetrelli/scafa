module com.github.apetrelli.scafa.proto {
	requires transitive com.github.apetrelli.scafa.tls;
	requires java.logging;
	exports com.github.apetrelli.scafa.proto.aio;
	exports com.github.apetrelli.scafa.proto.aio.sync;
	exports com.github.apetrelli.scafa.proto.aio.impl;
	exports com.github.apetrelli.scafa.proto.client;
	exports com.github.apetrelli.scafa.proto.client.impl;
	exports com.github.apetrelli.scafa.proto.processor;
	exports com.github.apetrelli.scafa.proto.processor.impl;
	exports com.github.apetrelli.scafa.proto.sync;
	exports com.github.apetrelli.scafa.proto.sync.client;
	exports com.github.apetrelli.scafa.proto.sync.processor;
	exports com.github.apetrelli.scafa.proto.sync.processor.impl;
	exports com.github.apetrelli.scafa.proto.sync.socket;
	exports com.github.apetrelli.scafa.proto.util;
}