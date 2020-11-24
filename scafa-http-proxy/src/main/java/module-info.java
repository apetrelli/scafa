module com.github.apetrelli.scafa.http.proxy {
	requires transitive com.github.apetrelli.scafa.http.gateway;
	requires java.logging;
    requires jcifs;
	exports com.github.apetrelli.scafa.http.proxy;
	exports com.github.apetrelli.scafa.http.proxy.impl;
	exports com.github.apetrelli.scafa.http.proxy.ntlm;
}