package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;

public abstract class AbstractHttpConnection implements HttpConnection {

	private static final Logger LOG = Logger.getLogger(AbstractHttpConnection.class.getName());

	protected AsynchronousSocketChannel channel;

	protected HostPort socketAddress;

    public AbstractHttpConnection(HostPort socketAddress) {
		this.socketAddress = socketAddress;
	}

	@Override
    public void ensureConnected(CompletionHandler<Void, Void> handler) {

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress.toString() });
        }
        try {
            channel = AsynchronousSocketChannel.open();
            bindChannel();
        } catch (IOException e1) {
            handler.failed(e1, null);
        }
        establishConnection(new DelegateFailureCompletionHandler<Void, Void>(handler) {

            @Override
            public void completed(Void result, Void attachment) {
                if (LOG.isLoggable(Level.INFO)) {
                    try {
                        LOG.log(Level.INFO, "Connected thread {0} to port {1}",
                                new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString() });
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "Cannot obtain local address", e);
                    }
                }

                prepareChannel();
                handler.completed(result, attachment);
            }
        });
    }

	@Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		HttpUtils.flushBuffer(buffer, channel, completionHandler);
	}

    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

	protected void bindChannel() throws IOException {
		// It does nothing here, but it can be overridden.
	}

	protected abstract void establishConnection(CompletionHandler<Void, Void> handler);

	protected abstract void prepareChannel();

}
