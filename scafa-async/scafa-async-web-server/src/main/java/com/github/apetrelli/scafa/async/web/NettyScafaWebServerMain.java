package com.github.apetrelli.scafa.async.web;

import java.io.IOException;
import java.util.logging.Level;

import com.github.apetrelli.scafa.async.file.nio.NioPathBufferContextReaderFactory;
import com.github.apetrelli.scafa.async.proto.netty.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.async.proto.netty.NettyAsyncServerSocketFactoryFactory;

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
public class NettyScafaWebServerMain {

	public static void main(String[] args) throws IOException {
		
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
		ScafaWebServerLauncher launcher = new ScafaWebServerLauncher(
				new DirectClientAsyncSocketFactory(clientBootstrap),
				new NettyAsyncServerSocketFactoryFactory(serverBootstrap), new NioPathBufferContextReaderFactory());
		String rootDirectory;
		if (args.length > 0) {
			rootDirectory = args[0];
		} else {
			rootDirectory = System.getProperty("user.dir");
		}
		launcher.launch(rootDirectory);

		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                launcher.stop();
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
