package com.github.apetrelli.scafa.async.proto.netty;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocketContextHolder {
	
	private UUID cid;

	private ByteBuffer byteBuffer;
	
	private CompletableFuture<Integer> completableFutureForRead;
//	
//	private CompletableFuture<Void> completableFutureForNextRead;
}
