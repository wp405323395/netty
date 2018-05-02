package netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 接收/处理/响应客户端websocket请求的核心业务处理类
 * @author liuyazhuang
 *
 */
public class ServerSocketHandlerNIO extends SimpleChannelInboundHandler<Object> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
		try {
			System.out.println("Client say : " + o.toString());
			//返回客户端消息 - 我已经接收到了你的消息
			channelHandlerContext.writeAndFlush("Received your message : " + o.toString());
		} catch (Exception e) {
			System.out.println("解析不出来啊哈哈");
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
		System.out.println(msg);
	}

	private int loss_connect_time = 0;
    private int retryTime = 8;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                System.out.println("5 秒没有接收到客户端的信息了");
                if (loss_connect_time > retryTime-1) {
                    StringBuffer bf = new StringBuffer("关闭这个不活跃的channel").append("(因为连续检测了").append(retryTime).append("次读状态都是空闲的)");
                    System.out.println(bf.toString());
                    ctx.channel().close();
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private WebSocketServerHandshaker handshaker;
	private static final String WEB_SOCKET_URL = "ws://localhost:8888/websocket";
	//客户端与服务端创建连接的时候调用
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		NettyConfig.group.add(ctx.channel());
		System.out.println("客户端与服务端连接开启...");
        super.channelActive(ctx);
	}

	//客户端与服务端断开连接的时候调用
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		NettyConfig.group.remove(ctx.channel());
		System.out.println("客户端与服务端连接关闭...");
	}

	//服务端接收客户端发送过来的数据结束之后调用
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
	}

	//工程出现异常的时候调用
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
