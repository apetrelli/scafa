module com.github.apetrelli.scafa.http.proxy {
	requires transitive com.github.apetrelli.scafa.async.http.gateway;
	requires transitive com.github.apetrelli.scafa.sync.http.gateway;
	requires java.logging;
    requires jcifs;
	exports com.github.apetrelli.scafa.http.proxy;
	exports com.github.apetrelli.scafa.http.proxy.impl;
	exports com.github.apetrelli.scafa.http.proxy.ntlm;
	exports com.github.apetrelli.scafa.http.proxy.sync;
	exports com.github.apetrelli.scafa.http.proxy.sync.connection;
	exports com.github.apetrelli.scafa.http.proxy.sync.handler;
	exports com.github.apetrelli.scafa.http.proxy.sync.ntlm;
}