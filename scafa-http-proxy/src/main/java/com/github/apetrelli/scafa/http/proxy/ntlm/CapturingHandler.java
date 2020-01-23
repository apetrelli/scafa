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
package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.HttpHandlerSupport;

public class CapturingHandler extends HttpHandlerSupport {

    protected HttpResponse response;

    private boolean finished = false;

    @Override
    public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        this.response = response;
        handler.completed(null, null);
    }

    @Override
    public void onEnd(CompletionHandler<Void, Void> handler) {
        finished = true;
        handler.completed(null, null);
    }

    public boolean isFinished() {
        return finished;
    }

    public void reset() {
        finished = false;
        response = null;
    }

    public HttpResponse getResponse() {
        return response;
    }
}