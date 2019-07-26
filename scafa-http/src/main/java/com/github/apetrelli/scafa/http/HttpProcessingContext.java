package com.github.apetrelli.scafa.http;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.processor.ProcessingContext;

public class HttpProcessingContext extends ProcessingContext<HttpInput, HttpByteSink> {

	private static final Logger LOG = Logger.getLogger(HttpProcessingContext.class.getName());

    private StringBuilder lineBuilder = new StringBuilder();

    private HttpRequest request;

    private HttpResponse response;

    private HeaderHolder holder;

	public HttpProcessingContext(HttpStatus status, HttpInput input) {
		super(status, input);
	}

	@Override
	public HttpStatus getStatus() {
		return (HttpStatus) super.getStatus();
	}

    public void appendToLine(byte currentByte) {
        lineBuilder.append((char) currentByte);
    }

    public void evaluateRequestLine() throws IOException {
        String requestLine = lineBuilder.toString();
        String[] pieces = requestLine.split("\\s+");
        if (pieces.length >= 3) {
            if (pieces[0].startsWith("HTTP/")) {
                String httpVersion = pieces[0];
                int responseCode;
                try {
                    responseCode = Integer.parseInt(pieces[1]);
                } catch (NumberFormatException e) {
                    responseCode = 500;
                    LOG.log(Level.SEVERE, "The response code is not a number: " + pieces[1], e);
                }
                StringBuilder builder = new StringBuilder();
                builder.append(pieces[2]);
                for (int i = 3; i < pieces.length; i++) {
                    builder.append(" ").append(pieces[i]);
                }
                String responseMessage = builder.toString();
                response = new HttpResponse(httpVersion, responseCode, responseMessage);
                holder = response;
            } else if (pieces.length == 3) {
                request = new HttpRequest(pieces[0], pieces[1], pieces[2]);
                holder = request;
            } else {
                throw new IOException("The request or response line is invalid: " + requestLine);
            }
        } else {
            throw new IOException("The request or response line is invalid: " + requestLine);
        }
    }

    public void addHeaderLine() {
        String header = lineBuilder.toString();
        clearLineBuilder();
        int pos = header.indexOf(": ");
        if (pos > 0) {
            String key = header.substring(0, pos);
            String value = header.substring(pos + 2, header.length());
            holder.addHeader(key, value);
        } else {
            LOG.severe("The header is invalid: " + header);
        }
    }

    public void evaluateBodyMode() {
    	HttpInput input = getInput();
        input.setBodyMode(HttpBodyMode.EMPTY);
        String lengthString = holder.getHeader("CONTENT-LENGTH");
        if (lengthString != null) {
            try {
                long length = Long.parseLong(lengthString.trim());
                if (length > 0L) {
                    input.setBodyMode(HttpBodyMode.BODY);
                    input.setBodySize(length);
                }
            } catch (NumberFormatException e) {
                LOG.log(Level.SEVERE, "The provided length is not an integer: " + lengthString, e);
            }
        } else { // Check chunked transfer
            String encoding = holder.getHeader("TRANSFER-ENCODING");
            if ("chunked".equals(encoding)) {
                input.setBodyMode(HttpBodyMode.CHUNKED);
            }
        }
    }

    public void evaluateChunkLength() throws IOException {
        if (lineBuilder.length() > 0) {
            String chunkCountHex = lineBuilder.toString();
            clearLineBuilder();
            try {
                long chunkCount = Long.parseLong(chunkCountHex, 16);
                LOG.log(Level.FINEST, "Preparing to read {0} bytes of a chunk", chunkCount);
                getInput().setChunkLength(chunkCount);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid chunk count " + chunkCountHex, e);
            }
        } else {
            throw new IOException("Chunk count as empty string, invalid");
        }
    }

    private void clearLineBuilder() {
        lineBuilder.delete(0, lineBuilder.length());
    }

}
