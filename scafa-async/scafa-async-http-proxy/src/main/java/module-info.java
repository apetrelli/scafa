module com.github.apetrelli.scafa.async.http.proxy {
	requires transitive com.github.apetrelli.scafa.async.http.gateway;
	requires transitive com.github.apetrelli.scafa.http.proxy;
	requires java.logging;
	requires lombok;
    requires jcifs;
	exports com.github.apetrelli.scafa.async.http.proxy;
	exports com.github.apetrelli.scafa.async.http.proxy.connection;
	exports com.github.apetrelli.scafa.async.http.proxy.handler;
	exports com.github.apetrelli.scafa.async.http.proxy.ntlm;
}