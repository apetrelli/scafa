module com.github.apetrelli.scafa.web {
	requires com.github.apetrelli.scafa.proto;
	requires java.logging;
    requires transitive ini4j;
	exports com.github.apetrelli.scafa.web;
	exports com.github.apetrelli.scafa.web.config;
	exports com.github.apetrelli.scafa.web.config.ini;
}