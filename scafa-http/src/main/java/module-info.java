module com.github.apetrelli.scafa.http {
	requires transitive com.github.apetrelli.scafa.proto;
	requires java.logging;
	exports com.github.apetrelli.scafa.http;
	exports com.github.apetrelli.scafa.http.impl;
	exports com.github.apetrelli.scafa.http.output;
	exports com.github.apetrelli.scafa.http.util;
}