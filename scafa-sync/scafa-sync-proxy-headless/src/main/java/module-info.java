module com.github.apetrelli.scafa.headless {
	requires transitive com.github.apetrelli.scafa.sync.proxy;
	requires java.logging;
	requires com.github.apetrelli.scafa.sync.proto.jnet;
	requires com.github.apetrelli.scafa.sync.proto.loom;
	requires com.github.apetrelli.scafa.sync.proto.thread;
}