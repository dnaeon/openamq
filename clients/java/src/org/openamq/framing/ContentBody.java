package org.openamq.framing;

import org.apache.mina.common.ByteBuffer;

/**
 * @author Robert Greig (robert.j.greig@jpmorgan.com)
 */
public class ContentBody extends AMQBody
{
    public static final byte TYPE = 3;

    public ByteBuffer payload;

    protected byte getType()
    {
        return TYPE;
    }

    public int getSize()
    {
        return (payload == null?0:payload.limit());
    }

    public void writePayload(ByteBuffer buffer)
    {
        if (payload != null)
        {
            ByteBuffer copy = payload.duplicate();
            buffer.put(copy.rewind());
        }
    }

    protected void populateFromBuffer(ByteBuffer buffer, long size) throws AMQFrameDecodingException
    {
        if (size > 0)
        {
            payload = buffer.slice();
            payload.limit((int)size);
            buffer.skip((int)size);

            /**/
            /*byte[] data = new byte[(int) size];
            buffer.get(data);
            payload = ByteBuffer.allocate(data.length);
            payload.setPooled(false);
            //payload.acquire();
            payload.put(data);
            payload.flip();
            /**/
        }
    }

    public static AMQFrame createAMQFrame(int channelId, ContentBody body)
    {
        final AMQFrame frame = new AMQFrame();
        frame.channel = channelId;
        frame.bodyFrame = body;
        return frame;
    }
}