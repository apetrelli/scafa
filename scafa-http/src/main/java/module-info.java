module com.github.apetrelli.scafa.http {
	requires transitive com.github.apetrelli.scafa.proto;
	requires java.logging;
	requires lombok;
	exports com.github.apetrelli.scafa.http;
	exports com.github.apetrelli.scafa.http.impl;
}