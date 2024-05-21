package com.hibob.anyim.netty.utils;

import com.google.protobuf.MessageLite;
import com.hibob.anyim.netty.protobuf.Body;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class MsgUtil {
    public static Msg getMsg(
            int magic,
            int version,
            int msgType,
            boolean isExtension,
            int fromId,
            int fromDev,
            int toId,
            int toDev,
            int seq,
            String content
    ) {
        Header header = Header.newBuilder()
                .setMagic(magic)
                .setVersion(version)
                .setMsgType(msgType)
                .setIsExtension(isExtension).build();
        Body body = Body.newBuilder()
                .setFromId(fromId)
                .setFromDev(fromDev)
                .setToId(toId)
                .setToDev(toDev)
                .setSeq(seq)
                .setContent(content).build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();
        return msg;
    }

    public static ByteBuf getMsgByteBuf(
            int magic,
            int version,
            int msgType,
            boolean isExtension,
            int fromId,
            int fromDev,
            int toId,
            int toDev,
            int seq,
            String content
    ) {
        Header header = Header.newBuilder()
                .setMagic(magic)
                .setVersion(version)
                .setMsgType(msgType)
                .setIsExtension(isExtension).build();
        Body body = Body.newBuilder()
                .setFromId(fromId)
                .setFromDev(fromDev)
                .setToId(toId)
                .setToDev(toDev)
                .setSeq(seq)
                .setContent(content).build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();

        ByteBuf encode = encode(wrappedBuffer(((MessageLite) msg).toByteArray()));
        return encode;
    }

    public static String getMsgHex(
            int magic,
            int version,
            int msgType,
            boolean isExtension,
            int fromId,
            int fromDev,
            int toId,
            int toDev,
            int seq,
            String content
    ) {
        Header header = Header.newBuilder()
                .setMagic(magic)
                .setVersion(version)
                .setMsgType(msgType)
                .setIsExtension(isExtension).build();
        Body body = Body.newBuilder()
                .setFromId(fromId)
                .setFromDev(fromDev)
                .setToId(toId)
                .setToDev(toDev)
                .setSeq(seq)
                .setContent(content).build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();

        ByteBuf encode = encode(wrappedBuffer(((MessageLite) msg).toByteArray()));
        //把encode转成16进制字符串
        StringBuffer sb = new StringBuffer();
        sb.append("0x");
        // 循环读取encode，把每个byte写入sb
        while (encode.isReadable()) {
            byte b = encode.readByte();
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }

        return sb.toString();
    }


    private static ByteBuf encode(ByteBuf msg) {
        ByteBuf out = ByteBufAllocator.DEFAULT.heapBuffer();
        int bodyLen = msg.readableBytes();
        int headerLen = computeRawVarint32Size(bodyLen);
        out.ensureWritable(headerLen + bodyLen);
        writeRawVarint32(out, bodyLen);
        out.writeBytes(msg, msg.readerIndex(), bodyLen);
        return out;
    }

    /**
     * Writes protobuf varint32 to (@link ByteBuf).
     * @param out to be written to
     * @param value to be written
     */
    private static void writeRawVarint32(ByteBuf out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            } else {
                out.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    /**
     * Computes size of protobuf varint32 after encoding.
     * @param value which is to be encoded.
     * @return size of value encoded as protobuf varint32.
     */
    private static int computeRawVarint32Size(final int value) {
        if ((value & (0xffffffff <<  7)) == 0) {
            return 1;
        }
        if ((value & (0xffffffff << 14)) == 0) {
            return 2;
        }
        if ((value & (0xffffffff << 21)) == 0) {
            return 3;
        }
        if ((value & (0xffffffff << 28)) == 0) {
            return 4;
        }
        return 5;
    }


    public static void main(String[] args) {
        String msgHex = getMsgHex(0x12345678, 0, 0, false, 1, 1, 2, 2, 1, "你好呀！");
        System.out.println(msgHex);

    }
}
