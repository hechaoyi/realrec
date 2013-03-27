package realrec.common.protocol.reply;

import io.netty.buffer.ByteBuf;

public interface Reply<T> {

	T data();

	void writeTo(ByteBuf out);

}
