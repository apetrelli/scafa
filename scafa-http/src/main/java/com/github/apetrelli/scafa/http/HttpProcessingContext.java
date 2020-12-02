package com.github.apetrelli.scafa.http;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.processor.ProcessingContext;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpProcessingContext extends ProcessingContext<HttpStatus> {

	private static final Logger LOG = Logger.getLogger(HttpProcessingContext.class.getName());

    private HttpBodyMode bodyMode = HttpBodyMode.EMPTY;

    private long countdown = 0L;

    private long bodySize;

    private long bodyOffset;

    private long totalChunkedTransferLength;

    private long chunkOffset;

    private long chunkLength;

    private boolean httpConnected = false;

    private StringBuilder lineBuilder = new StringBuilder();

    private HttpRequest request;

    private HttpResponse response;

    private HeaderHolder holder;

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

    public void appendToLine(byte currentByte) {
        lineBuilder.append((char) currentByte);
    }

    public void evaluateRequestLine() {
        String requestLine = lineBuilder.toString();
        clearLineBuilder();
        String[] pieces = requestLine.split("\\s+");
        if (pieces.length >= 2) {
            if (pieces[0].startsWith("HTTP/")) {
                String httpVersion = pieces[0];
                int responseCode;
                try {
                    responseCode = Integer.parseInt(pieces[1]);
                } catch (NumberFormatException e) {
                    responseCode = 500;
                    LOG.log(Level.SEVERE, e, () -> "The response code is not a number: " + pieces[1]);
                }
                String responseMessage = null;
                if (pieces.length > 2) {
	                StringBuilder builder = new StringBuilder();
	                builder.append(pieces[2]);
	                for (int i = 3; i < pieces.length; i++) {
	                    builder.append(" ").append(pieces[i]);
	                }
	                responseMessage = builder.toString();
                }
                response = new HttpResponse(httpVersion, responseCode, responseMessage);
                holder = response;
            } else if (pieces.length == 3) {
                request = new HttpRequest(pieces[0], pieces[1], pieces[2]);
                holder = request;
            } else {
                throw new HttpException("The request or response line is invalid: " + requestLine);
            }
        } else {
            throw new HttpException("The request or response line is invalid: " + requestLine);
        }
    }

    public void addHeaderLine() {
        String header = lineBuilder.toString();
        clearLineBuilder();
        int pos = header.indexOf(": ");
        if (pos > 0) {
            String key = header.substring(0, pos).trim();
            String value = header.substring(pos + 2, header.length()).trim();
            holder.addHeader(new HeaderName(key), new AsciiString(value));
        } else {
            LOG.severe("The header is invalid: " + header);
        }
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

    public void evaluateChunkLength() {
        if (lineBuilder.length() > 0) {
            String chunkCountHex = lineBuilder.toString();
            clearLineBuilder();
            try {
                long chunkCount = Long.parseLong(chunkCountHex, 16);
                LOG.log(Level.FINEST, "Preparing to read {0} bytes of a chunk", chunkCount);
                setChunkLength(chunkCount);
            } catch (NumberFormatException e) {
                throw new HttpException("Invalid chunk count " + chunkCountHex, e);
            }
        } else {
            throw new HttpException("Chunk count as empty string, invalid");
        }
    }
    public void reset() {
        bodyMode = HttpBodyMode.EMPTY;
        countdown = 0L;
        bodySize = 0L;
        bodyOffset = 0L;
        totalChunkedTransferLength = 0L;
        chunkOffset = 0L;
        chunkLength = 0L;
        clearLineBuilder();
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

    private void clearLineBuilder() {
        lineBuilder.delete(0, lineBuilder.length());
    }

}
