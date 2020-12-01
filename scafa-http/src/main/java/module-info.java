module com.github.apetrelli.scafa.http {
	requires transitive com.github.apetrelli.scafa.proto;
	requires java.logging;
	exports com.github.apetrelli.scafa.http;
	exports com.github.apetrelli.scafa.http.impl;
	exports com.github.apetrelli.scafa.http.output;
	exports com.github.apetrelli.scafa.http.output.impl;
	exports com.github.apetrelli.scafa.http.sync;
	exports com.github.apetrelli.scafa.http.sync.composite;
	exports com.github.apetrelli.scafa.http.sync.direct;
	exports com.github.apetrelli.scafa.http.sync.impl;
	exports com.github.apetrelli.scafa.http.sync.output;
	exports com.github.apetrelli.scafa.http.sync.output.impl;
}