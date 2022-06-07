module com.github.apetrelli.scafa.http.proxy {
	requires transitive com.github.apetrelli.scafa.http;
	requires java.logging;
	exports com.github.apetrelli.scafa.http.proxy;
	exports com.github.apetrelli.scafa.http.proxy.ntlm;
}