package com.datacanal.common.netty.handler;

import java.util.List;

import com.canal.serializer.impl.JSONSerializer;
import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.model.Command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 服务端的解码器
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月3日 下午2:35:32
 */
public class HeadWithBodyDecodeHandler extends ByteToMessageDecoder {
    
    private static final ISerializer<Command> SERIALIZER = new JSONSerializer<>();
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int canReadByteLength = in.readableBytes();
        while(canReadByteLength >= 4) { //这边的4是head的长度
            in.markReaderIndex(); //记录一下当前的readIndex
            int bodyLength = in.readInt();
            int fullMessageLegth = bodyLength + 4;
            if(canReadByteLength < fullMessageLegth) { //如果可读字节数不够全部message的长度,那么直接重置readIndex退出,等待下一次调用
                in.resetReaderIndex();
                return;
            }
            byte[] body = new byte[bodyLength];
            in.readBytes(body);
            Command command = SERIALIZER.decode(body, Command.class);
            out.add(command);
            canReadByteLength = in.readableBytes();
        }
    }
}
