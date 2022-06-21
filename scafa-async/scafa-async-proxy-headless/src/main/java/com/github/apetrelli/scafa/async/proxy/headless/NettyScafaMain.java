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
package com.github.apetrelli.scafa.async.proxy.headless;

import java.util.logging.Level;

import com.github.apetrelli.scafa.async.proto.netty.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.async.proto.netty.NettyAsyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.async.proxy.AsyncScafaLauncher;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.java.Log;

@Log
public class NettyScafaMain {

    public static void main(String[] args) {
        String profile = null;
        if (args.length > 0) {
            profile = args[0];
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.AUTO_READ, true).childOption(ChannelOption.AUTO_CLOSE, false);
		Bootstrap clientBootstrap = new Bootstrap();
		clientBootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				// Do nothing
			}
		}).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.AUTO_READ, true).option(ChannelOption.AUTO_CLOSE, false);
        AsyncScafaLauncher launcher = new AsyncScafaLauncher(new DirectClientAsyncSocketFactory(clientBootstrap), new NettyAsyncServerSocketFactoryFactory(serverBootstrap));
        launcher.initialize();
        launcher.launch(profile);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                launcher.stop();
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });

        while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.log(Level.INFO, "Main thread interrupted", e);
            }
        }
    }
}
