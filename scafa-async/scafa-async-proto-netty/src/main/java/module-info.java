module com.github.apetrelli.scafa.async.proto.netty {
	requires java.logging;
	requires lombok;
	requires transitive com.github.apetrelli.scafa.async.proto;
	requires transitive io.netty.transport;
	requires io.netty.common;
	requires io.netty.buffer;
	exports com.github.apetrelli.scafa.async.proto.netty;
}