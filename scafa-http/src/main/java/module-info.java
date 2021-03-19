module com.github.apetrelli.scafa.http {
	requires transitive com.github.apetrelli.scafa.async.proto;
	requires transitive com.github.apetrelli.scafa.sync.proto;
	requires java.logging;
	exports com.github.apetrelli.scafa.http;
	exports com.github.apetrelli.scafa.http.impl;
	exports com.github.apetrelli.scafa.http.async;
	exports com.github.apetrelli.scafa.http.async.composite;
	exports com.github.apetrelli.scafa.http.async.direct;
	exports com.github.apetrelli.scafa.http.async.impl;
	exports com.github.apetrelli.scafa.http.async.output;
	exports com.github.apetrelli.scafa.http.async.output.impl;
	exports com.github.apetrelli.scafa.http.async.server;
	exports com.github.apetrelli.scafa.http.sync;
	exports com.github.apetrelli.scafa.http.sync.composite;
	exports com.github.apetrelli.scafa.http.sync.direct;
	exports com.github.apetrelli.scafa.http.sync.impl;
	exports com.github.apetrelli.scafa.http.sync.output;
	exports com.github.apetrelli.scafa.http.sync.output.impl;
	exports com.github.apetrelli.scafa.http.sync.server;
}