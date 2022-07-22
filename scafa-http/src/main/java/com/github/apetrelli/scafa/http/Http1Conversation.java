package com.github.apetrelli.scafa.http;

import com.github.apetrelli.scafa.proto.io.OutputFlow;

public interface Http1Conversation extends HttpConversation {

	@Override
	default void fill(OutputFlow out) {
		headers().getHeaders().entrySet().stream().forEach(t -> {
			HeaderName key = t.getKey();
			t.getValue().forEach(u -> {
				out.write(key.getArray(), key.getFrom(), key.length()).write(HttpChars.COLON).write(HttpChars.SPACE)
						.write(u.getArray(), u.getFrom(), u.length()).write(HttpChars.CR).write(HttpChars.LF);
			});
		});
		out.write(HttpChars.CR).write(HttpChars.LF).flush();
	}
}
