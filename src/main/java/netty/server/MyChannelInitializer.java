package netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


/**
 * 初始化连接时候的各个组件
 * @author liuyazhuang
 *
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
	private boolean isBrowser;
	public MyChannelInitializer(boolean isBrowser){
		this.isBrowser = isBrowser;
	}
	@Override
	protected void initChannel(SocketChannel e) throws Exception {

		ChannelPipeline pipeline = e.pipeline();

		if(isBrowser) {
			//用于浏览器的websocket场景
			pipeline.addLast("http-codec", new HttpServerCodec());//HttpServerCodec：将请求和应答消息解码为HTTP消息
			pipeline.addLast("aggregator", new HttpObjectAggregator(65536));//把Http消息组成完整地HTTP消息
			pipeline.addLast("http-chunked", new ChunkedWriteHandler());//向客户端发送HTML5文件
		}
		pipeline.addLast(new IdleStateHandler(7, 7, 7, TimeUnit.SECONDS));//设置7秒没有读到数据，则触发一个READER_IDLE事件
		if(isBrowser) {
			pipeline.addLast("handler", new ServerSocketHandlerBrowser());
		} else {
			pipeline.addLast("decoder", new StringDecoder());
			pipeline.addLast("encoder", new StringEncoder());
			pipeline.addLast("handler", new ServerSocketHandlerNIO());
		}

		//e.pipeline().addLast(new ReadTimeoutHandler(15));//设置连接最长时间，时间一到连接断开。
	}

}
