package com.dongnao.jack.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyClientInHandler extends ChannelInboundHandlerAdapter {

    /**
     * 服务端返回的消息
     */
    public StringBuffer message;

    /**
     * 客户端发送给服务端的消息
     */
    public String sendMsg;

    public NettyClientInHandler(StringBuffer message, String sendMsg) {
        this.message = message;
        this.sendMsg = sendMsg;
    }

    /**
     * 客户端发送的消息
     * <p>
     * 当我们连接成功以后会触发这个方法
     * 在这个方法里面完成消息的发送
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--------------channelActive-------------");
        ByteBuf encoded = ctx.alloc().buffer(4 * sendMsg.length());
        encoded.writeBytes(sendMsg.getBytes());
        ctx.write(encoded);
        ctx.flush();
    }


    /**
     * 接受服务端的消息
     * <p>
     * 一旦服务端有消息过来，这个方法会触发
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("------------------channelRead--------------------");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        System.out.println("server response msg：" + new String(result1));
        message.append(new String(result1));
        result.release();
    }

}
