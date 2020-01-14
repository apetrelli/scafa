package com.github.apetrelli.scafa.tls;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import com.github.apetrelli.scafa.tls.util.AIOUtils;

public class AsynchronousTlsChannel extends AsynchronousSocketChannel {
	
	private static final Logger LOG = Logger.getLogger(AsynchronousTlsChannel.class.getName());

	private AsynchronousSocketChannel wrapped;

	private SSLEngine engine;

	public AsynchronousTlsChannel(AsynchronousSocketChannel channel, SSLEngine engine) {
		super(channel.provider());
		this.wrapped = channel;
		this.engine = engine;
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
	}

	@Override
	public boolean isOpen() {
		return wrapped.isOpen();
	}

	@Override
	public <T> T getOption(SocketOption<T> name) throws IOException {
		return wrapped.getOption(name);
	}

	@Override
	public Set<SocketOption<?>> supportedOptions() {
		return wrapped.supportedOptions();
	}

	@Override
	public AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
		return wrapped.bind(local);
	}

	@Override
	public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
		return wrapped.setOption(name, value);
	}

	@Override
	public AsynchronousSocketChannel shutdownInput() throws IOException {
		return wrapped.shutdownInput();
	}

	@Override
	public AsynchronousSocketChannel shutdownOutput() throws IOException {
		return wrapped.shutdownOutput();
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return wrapped.getRemoteAddress();
	}

	@Override
	public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
		wrapped.connect(remote, attachment, new CompletionHandler<Void, A>() {

			@Override
			public void completed(Void result, A attachment) {
				try {
					engine.beginHandshake();
					
				} catch (SSLException e) {
					handler.failed(e, attachment);
				}
				
			}

			@Override
			public void failed(Throwable exc, A attachment) {
				handler.failed(exc, attachment);
			}
		});
	}

	@Override
	public Future<Void> connect(SocketAddress remote) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
			CompletionHandler<Integer, ? super A> handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public Future<Integer> read(ByteBuffer dst) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment,
			CompletionHandler<Long, ? super A> handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment,
			CompletionHandler<Integer, ? super A> handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public Future<Integer> write(ByteBuffer src) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment,
			CompletionHandler<Long, ? super A> handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return wrapped.getLocalAddress();
	}
    /**
     * Implements the handshake protocol between two peers, required for the establishment of the SSL/TLS connection.
     * During the handshake, encryption configuration information - such as the list of available cipher suites - will be exchanged
     * and if the handshake is successful will lead to an established SSL/TLS session.
     *
     * <p/>
     * A typical handshake will usually contain the following steps:
     *
     * <ul>
     *   <li>1. wrap:     ClientHello</li>
     *   <li>2. unwrap:   ServerHello/Cert/ServerHelloDone</li>
     *   <li>3. wrap:     ClientKeyExchange</li>
     *   <li>4. wrap:     ChangeCipherSpec</li>
     *   <li>5. wrap:     Finished</li>
     *   <li>6. unwrap:   ChangeCipherSpec</li>
     *   <li>7. unwrap:   Finished</li>
     * </ul>
     * <p/>
     * Handshake is also used during the end of the session, in order to properly close the connection between the two peers.
     * A proper connection close will typically include the one peer sending a CLOSE message to another, and then wait for
     * the other's CLOSE message to close the transport link. The other peer from his perspective would read a CLOSE message
     * from his peer and then enter the handshake procedure to send his own CLOSE message as well.
     *
     * @param socketChannel - the socket channel that connects the two peers.
     * @param engine - the engine that will be used for encryption/decryption of the data exchanged with the other peer.
     * @return True if the connection handshake was successful or false if an error occurred.
     * @throws IOException - if an error occurs during read/write to the socket channel.
     */
    private <A> void doHandshake(A attachment, CompletionHandler<Void, A> handler) {

        LOG.log(Level.FINE, "About to do handshake...");

        // NioSslPeer's fields myAppData and peerAppData are supposed to be large enough to hold all message data the peer
        // will send and expects to receive from the other peer respectively. Since the messages to be exchanged will usually be less
        // than 16KB long the capacity of these fields should also be smaller. Here we initialize these two local buffers
        // to be used for the handshake, while keeping client's buffers at the same size.
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer throwAwayData = ByteBuffer.allocate(appBufferSize);

        doHandshake(attachment, handler, myAppData, peerAppData, throwAwayData);

    }

	private <A> void doHandshake(A attachment, CompletionHandler<Void, A> handler, ByteBuffer myAppData,
			ByteBuffer peerAppData, ByteBuffer throwAwayData) {
		HandshakeStatus handshakeStatus = engine.getHandshakeStatus();
        if (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
        	handler.completed(null, attachment);
        }
        switch (handshakeStatus) {
        case NEED_UNWRAP:
        	wrapped.read(peerAppData, attachment, new CompletionHandler<Integer, A>() {

				@Override
				public void completed(Integer count, A attachment) {
					if (count < 0) {
	                    if (engine.isInboundDone() && engine.isOutboundDone()) {
	                        handler.failed(new TlsConnectionException("SSL connection incomplete"), attachment);
	                    } else {
		                    try {
		                        engine.closeInbound();
		                    } catch (SSLException e) {
		                    	LOG.log(Level.SEVERE, "This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.", e);
		                    }
		                    engine.closeOutbound();
		                    // After closeOutbound the engine will be set to WRAP state, in order to try to send a close message to the client.
	                    }
					} else {
						peerAppData.flip();
		                try {
		                    SSLEngineResult result = engine.unwrap(peerAppData, throwAwayData);
		                    peerAppData.compact();
		                    ByteBuffer newPeerAppData = peerAppData;
		                    switch (result.getStatus()) {
		                    case OK:
		                        break;
		                    case BUFFER_OVERFLOW:
		                        // Will occur when peerAppData's capacity is smaller than the data derived from peerNetData's unwrap.
		                    	newPeerAppData = enlargeApplicationBuffer(engine, peerAppData);
		                        break;
		                    case BUFFER_UNDERFLOW:
		                        // Will occur either when no data was read from the peer or when the peerNetData buffer was too small to hold all peer's data.
		                    	newPeerAppData = handleBufferUnderflow(engine, peerAppData);
		                        break;
		                    case CLOSED:
		                        if (engine.isOutboundDone()) {
			                        handler.failed(new TlsConnectionException("SSL connection incomplete"), attachment);
		                        } else {
		                            engine.closeOutbound();
		                        }
	                            break;
		                    default:
		                        handler.failed(new TlsConnectionException("Invalid SSL status: " + result.getStatus()), attachment);
		                    }
		                    doHandshake(attachment, handler, myAppData, newPeerAppData, throwAwayData);
		                } catch (SSLException sslException) {
		                    LOG.log(Level.SEVERE, "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", sslException);
		                    engine.closeOutbound();
		                }
					}
				}

				@Override
				public void failed(Throwable exc, A attachment) {
					handler.failed(exc, attachment);
				}
			});
        case NEED_WRAP:
            myAppData.clear();
            try {
            	throwAwayData.clear();
                SSLEngineResult result = engine.wrap(throwAwayData, myAppData);
                handshakeStatus = result.getHandshakeStatus();
                switch (result.getStatus()) {
                case OK :
                    myAppData.flip();
                    AIOUtils.flipAndFlushBuffer(myAppData, wrapped, new CompletionHandler<Void, Void>() {

						@Override
						public void completed(Void result, Void localAttachment) {
		                    doHandshake(attachment, handler, myAppData, peerAppData, throwAwayData);
						}

						@Override
						public void failed(Throwable exc, Void localAttachment) {
							handler.failed(exc, attachment);
						}
					});
                    break;
                case BUFFER_OVERFLOW:
                    // Will occur if there is not enough space in myNetData buffer to write all the data that would be generated by the method wrap.
                    // Since myNetData is set to session's packet size we should not get to this point because SSLEngine is supposed
                    // to produce messages smaller or equal to that, but a general handling would be the following:
                    ByteBuffer newMyAppData = enlargePacketBuffer(engine, myAppData);
                    doHandshake(attachment, handler, newMyAppData, peerAppData, throwAwayData);
                    break;
                case BUFFER_UNDERFLOW:
                    handler.failed(new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here."), attachment);
                case CLOSED:
                	AIOUtils.flipAndFlushBuffer(myAppData, wrapped, new CompletionHandler<Void, Void>() {

						@Override
						public void completed(Void result, Void localAttachment) {
	                        handler.failed(new TlsConnectionException("SSL connection closed"), attachment);
	                        peerAppData.clear();
						}

						@Override
						public void failed(Throwable exc, Void localAttachment) {
	                        LOG.log(Level.SEVERE, "Failed to send server's CLOSE message due to socket channel's failure.", exc);
						}
					});
                    break;
                default:
                    handler.failed(new TlsConnectionException("Invalid SSL status: " + result.getStatus()), attachment);
                }
            } catch (SSLException sslException) {
                LOG.log(Level.SEVERE, "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", sslException);
                engine.closeOutbound();
                handshakeStatus = engine.getHandshakeStatus();
                break;
            }
            break;
        case NEED_TASK:
            Runnable task;
            while ((task = engine.getDelegatedTask()) != null) {
                task.run();
            }
            handshakeStatus = engine.getHandshakeStatus();
            break;
        case FINISHED:
            break;
        case NOT_HANDSHAKING:
            break;
        default:
            handler.failed(new TlsConnectionException("Invalid SSL status: " + handshakeStatus), attachment);
        }
	}

    private ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    private ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    /**
     * Compares <code>sessionProposedCapacity<code> with buffer's capacity. If buffer's capacity is smaller,
     * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
     * with capacity twice the size of the initial one.
     *
     * @param buffer - the buffer to be enlarged.
     * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by {@link SSLSession}.
     * @return A new buffer with a larger capacity.
     */
    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    /**
     * Handles {@link SSLEngineResult.Status#BUFFER_UNDERFLOW}. Will check if the buffer is already filled, and if there is no space problem
     * will return the same buffer, so the client tries to read again. If the buffer is already filled will try to enlarge the buffer either to
     * session's proposed size or to a larger capacity. A buffer underflow can happen only after an unwrap, so the buffer will always be a
     * peerNetData buffer.
     *
     * @param buffer - will always be peerNetData buffer.
     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
     * @throws Exception
     */
    private ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }
}
