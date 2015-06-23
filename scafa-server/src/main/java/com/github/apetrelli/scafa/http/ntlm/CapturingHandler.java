/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.http.ntlm;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.impl.HttpHandlerSupport;

public class CapturingHandler extends HttpHandlerSupport {
    
    private boolean finished = false;
    
    private String httpVersion;
    
    private int responseCode;
    
    private String responseMessage;
    
    private Map<String, List<String>> headers;
    
    @Override
    public void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
        this.httpVersion = httpVersion;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.headers = headers;
    }
    
    @Override
    public void onEnd() throws IOException {
        finished = true;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public void reset() {
        finished = false;
        responseCode = 0;
        responseMessage = null;
        headers = null;
    }
    
    public String getHttpVersion() {
        return httpVersion;
    }
    
    public int getResponseCode() {
        return responseCode;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}