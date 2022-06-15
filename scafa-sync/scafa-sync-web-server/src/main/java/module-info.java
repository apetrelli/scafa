module com.github.apetrelli.scafa.sync.web {
	requires transitive com.github.apetrelli.scafa.sync.http.gateway;
	requires transitive com.github.apetrelli.scafa.sync.http.server;
	requires transitive com.github.apetrelli.scafa.web;
	requires com.github.apetrelli.scafa.sync.proto.jnet;
	requires com.github.apetrelli.scafa.sync.proto.thread;
	requires java.logging;
	requires lombok;
    requires transitive ini4j;
}
