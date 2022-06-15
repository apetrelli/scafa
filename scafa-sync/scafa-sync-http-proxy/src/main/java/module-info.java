module com.github.apetrelli.scafa.sync.http.proxy {
	requires transitive com.github.apetrelli.scafa.sync.http.gateway;
	requires transitive com.github.apetrelli.scafa.http.proxy;
	requires java.logging;
	requires lombok;
    requires jcifs;
	exports com.github.apetrelli.scafa.sync.http.proxy;
	exports com.github.apetrelli.scafa.sync.http.proxy.connection;
	exports com.github.apetrelli.scafa.sync.http.proxy.handler;
	exports com.github.apetrelli.scafa.sync.http.proxy.ntlm;
}