package com.github.apetrelli.scafa.async.proto.netty;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerSocketContextHolder {
	
	private CompletableFuture<AsyncSocket> completableFutureForAccept = new CompletableFuture<>();
}
