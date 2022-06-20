module com.github.apetrelli.scafa.headless {
	requires transitive com.github.apetrelli.scafa.async.proxy;
	requires transitive com.github.apetrelli.scafa.async.proto.netty;
	requires java.logging;
	requires lombok;
	requires com.github.apetrelli.scafa.async.proto.aio;
	requires io.netty.transport;
}