package com.github.apetrelli.scafa.http;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.processor.ProcessingContext;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpProcessingContext extends ProcessingContext<HttpStatus> {
	
	public enum HttpMessageType {
		REQUEST, RESPONSE;
	}

	private static final Logger LOG = Logger.getLogger(HttpProcessingContext.class.getName());
	
	private static final AsciiString HTTP_RESPONSE_PREFIX = new AsciiString("HTTP/");
	
	private HttpMessageType messageType;

    private HttpBodyMode bodyMode = HttpBodyMode.EMPTY;

    private long countdown = 0L;

    private long bodySize;

    private long bodyOffset;

    private long totalChunkedTransferLength;

    private long chunkOffset;

    private long chunkLength;

    private boolean httpConnected = false;

    private HttpRequest request;

    private HttpResponse response;

    private HeaderHolder holder;
    
    private byte[] carry;
    
    private AsciiString method;
    
    private AsciiString resource;
    
    private AsciiString httpVersion;
    
    private Integer code;
    
    private AsciiString message;
    
    private HeaderName headerName;
    
    private AsciiString headerValue;

	public HttpProcessingContext(HttpStatus status) {
		super(status);
	}

    public HttpBodyMode getBodyMode() {
        return bodyMode;
    }

    public void setBodyMode(HttpBodyMode bodyMode) {
        this.bodyMode = bodyMode;
    }

    public long getCountdown() {
        return countdown;
    }

    public long getBodySize() {
        return bodySize;
    }

    public void setBodySize(long bodySize) {
        this.bodySize = bodySize;
        countdown = bodySize;
        bodyOffset = 0L;
    }

    public long getBodyOffset() {
        return bodyOffset;
    }

    public long getTotalChunkedTransferLength() {
        return totalChunkedTransferLength;
    }

    public long getChunkOffset() {
        return chunkOffset;
    }

    public long getChunkLength() {
        return chunkLength;
    }

    public void setChunkLength(long chunkLength) {
        this.chunkLength = chunkLength;
        countdown = chunkLength;
        totalChunkedTransferLength += chunkLength;
        chunkOffset = 0L;
    }

    public void reduceBody(int toSubtract) {
        countdown -= toSubtract;
        if (countdown < 0L) {
            countdown = 0L;
        }
        bodyOffset += toSubtract;
    }

    public void reduceChunk(long toSubtract) {
        countdown -= toSubtract;
        if (countdown < 0L) {
            countdown = 0L;
        }
        chunkOffset += toSubtract;
    }

    public boolean isHttpConnected() {
        return httpConnected;
    }

    public void setHttpConnected(boolean httpConnected) {
        this.httpConnected = httpConnected;
    }
    
    public void setCarry(byte[] carry) {
		this.carry = carry;
	}
    
    public void evaluateFirstToken(int from, int to) {
    	AsciiString string = new AsciiString(reconstructArray(from, to));
    	byte[] stringArray = string.getArray();
		if (stringArray.length >= HTTP_RESPONSE_PREFIX.length() && Arrays.equals(stringArray, 0,
				HTTP_RESPONSE_PREFIX.length(), HTTP_RESPONSE_PREFIX.getArray(), 0, HTTP_RESPONSE_PREFIX.length())) {
    		httpVersion = string;
    		messageType = HttpMessageType.RESPONSE;
    	} else {
    		method = string;
    		messageType = HttpMessageType.REQUEST;
    	}
    }
    
    public void evaluateSecondToken(int from, int to) {
    	AsciiString string = new AsciiString(reconstructArray(from, to));
    	switch (messageType) {
    	case REQUEST:
        	resource = string;
        	break;
    	case RESPONSE:
            try {
                code = Integer.decode(string.toString());
            } catch (NumberFormatException e) {
                code = 500;
                LOG.log(Level.SEVERE, e, () -> "The response code is not a number: " + string);
            }
        	break;
    	default:
    		throw new IllegalStateException("Not a request nor a response");
    	}
    }
    
    public void evaluateFinalContent(int from, int to) {
    	AsciiString string = new AsciiString(reconstructArray(from, to));
    	switch (messageType) {
    	case REQUEST:
        	httpVersion = string;
        	break;
    	case RESPONSE:
    		message = string;
        	break;
    	default:
    		throw new IllegalStateException("Not a request nor a response");
    	}
    }
    
    public void evaluateRequestLine() {
    	switch (messageType) {
    	case REQUEST:
        	request = new HttpRequest(method, resource, httpVersion);
        	holder = request;
        	break;
    	case RESPONSE:
        	response = new HttpResponse(httpVersion, code, message);
        	holder = response;
        	break;
    	default:
    		throw new IllegalStateException("Not a request nor a response");
    	}
    }
    
    public void evaluateHeaderName(int from, int to) {
    	headerName = new HeaderName(reconstructArray(from, to));
    }
    
    public void evaluateHeaderValue(int from, int to) {
    	headerValue = new AsciiString(reconstructArray(from, to));
    }

    public void addHeaderLine() {
    	holder.addHeader(headerName, headerValue);
    }

    public void evaluateBodyMode() {
        setBodyMode(HttpBodyMode.EMPTY);
        AsciiString lengthString = holder.getHeader(CONTENT_LENGTH);
        if (lengthString != null) {
            try {
                long length = Long.parseLong(lengthString.toString().trim());
                if (length > 0L) {
                    setBodyMode(HttpBodyMode.BODY);
                    setBodySize(length);
                }
            } catch (NumberFormatException e) {
                LOG.log(Level.SEVERE, "The provided length is not an integer: " + lengthString, e);
            }
        } else { // Check chunked transfer
            AsciiString encoding = holder.getHeader(TRANSFER_ENCODING);
            if (CHUNKED.equals(encoding)) {
                setBodyMode(HttpBodyMode.CHUNKED);
            }
        }
    }

    public void evaluateChunkLength(int from, int to) {
    	String chunkCountHex = new String(reconstructArray(from, to), StandardCharsets.US_ASCII);
        try {
            long chunkCount = Long.parseLong(chunkCountHex, 16);
            LOG.log(Level.FINEST, "Preparing to read {0} bytes of a chunk", chunkCount);
            setChunkLength(chunkCount);
        } catch (NumberFormatException e) {
            throw new HttpException("Invalid chunk count " + chunkCountHex, e);
        }
    }
    public void reset() {
    	messageType = null;
        bodyMode = HttpBodyMode.EMPTY;
        countdown = 0L;
        bodySize = 0L;
        bodyOffset = 0L;
        totalChunkedTransferLength = 0L;
        chunkOffset = 0L;
        chunkLength = 0L;
        carry = null;
        request = null;
        response = null;
        holder = null;
    }

    public HttpRequest getRequest() {
		return request;
	}

    public HttpResponse getResponse() {
		return response;
	}

	private byte[] reconstructArray(int from, int to) {
		byte[] array;
    	ByteBuffer buffer = getBuffer();
		int length = to - from;
		if (carry != null) {
    		array = new byte[carry.length + length];
    		System.arraycopy(carry, 0, array, 0, carry.length);
    		System.arraycopy(buffer.array(), buffer.arrayOffset() + from, array, carry.length, length);
    		carry = null;
    	} else {
    		array = new byte[length];
    		System.arraycopy(buffer.array(), buffer.arrayOffset() + from, array, 0, length);
    	}
		return array;
	}

}
