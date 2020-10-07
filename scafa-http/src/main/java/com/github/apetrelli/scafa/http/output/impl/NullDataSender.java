package com.github.apetrelli.scafa.http.output.impl;

import com.github.apetrelli.scafa.http.output.DataSender;

public class NullDataSender implements DataSender {

    @Override
    public void send(byte[] b, int off, int len) {
    	// Does nothing
    }

    @Override
    public void end() {
    	// Does nothing
    }

}
