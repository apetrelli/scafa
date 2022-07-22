package com.github.apetrelli.scafa.http2;

public enum StreamState {

	IDLE, RESERVED_LOCAL, RESERVED_REMOTE, HALF_CLOSED_LOCAL, HALF_CLOSED_REMOTE, CLOSED;
}
