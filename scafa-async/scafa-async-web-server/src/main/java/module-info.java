module com.github.apetrelli.scafa.async.web {
	requires transitive com.github.apetrelli.scafa.async.http.gateway;
	requires transitive com.github.apetrelli.scafa.async.http.server;
	requires transitive com.github.apetrelli.scafa.web;
	requires com.github.apetrelli.scafa.async.file.aio;
	requires com.github.apetrelli.scafa.async.file.nio;
	requires com.github.apetrelli.scafa.async.proto.aio;
	requires com.github.apetrelli.scafa.async.proto.netty;
	requires java.logging;
	requires lombok;
    requires transitive ini4j;
}