package org.openamq.framing;

import org.apache.mina.common.ByteBuffer;

/**
 * @author Robert Greig (robert.j.greig@jpmorgan.com)
 */
public class CompositeAMQDataBlock extends AMQDataBlock implements EncodableAMQDataBlock
{
    private ByteBuffer _encodedBlock;

    private AMQDataBlock[] _blocks;

    public CompositeAMQDataBlock(AMQDataBlock[] blocks)
    {
        _blocks = blocks;
    }

    /**
     * The encoded block will be logically first before the AMQDataBlocks which are encoded
     * into the buffer afterwards.
     * @param encodedBlock already-encoded data
     * @param blocks some blocks to be encoded.
     */
    public CompositeAMQDataBlock(ByteBuffer encodedBlock, AMQDataBlock[] blocks)
    {
        this(blocks);
        _encodedBlock = encodedBlock;
    }

    public CompositeAMQDataBlock(AMQFrame[] blocks, ByteBuffer payload, int maxPayloadSize)
    {
        int
            dataSize = payload.limit(),
            frameLast = (dataSize % maxPayloadSize > 0) ? 1 : 0,
            bodies = dataSize / maxPayloadSize + frameLast,
            i;
        AMQFrame[]
            frames = new AMQFrame[blocks.length + bodies];

        for (i = 0; i < blocks.length; i++)
            frames[i] = blocks [i];

        for (i = 0; i < bodies; i++) {
            ContentBody
                cb = new ContentBody();

            payload.position(i * maxPayloadSize);
            payload.limit(payload.position() + Math.min(maxPayloadSize, dataSize - payload.position()));
            cb.payload = payload.slice();
            frames[blocks.length + i] = ContentBody.createAMQFrame(blocks[0].channel, cb);
        }

        _blocks = frames;
    }

    public AMQDataBlock[] getBlocks()
    {
        return _blocks;
    }

    public ByteBuffer getEncodedBlock()
    {
        return _encodedBlock;
    }

    public long getSize()
    {
        long frameSize = 0;
        for (int i = 0; i < _blocks.length; i++)
        {
            frameSize += _blocks[i].getSize();
        }
        if (_encodedBlock != null)
        {
            _encodedBlock.rewind();
            frameSize += _encodedBlock.remaining();
        }
        return frameSize;
    }

    public void writePayload(ByteBuffer buffer)
    {
        if (_encodedBlock != null)
        {
            buffer.put(_encodedBlock);
        }
        for (int i = 0; i < _blocks.length; i++)
        {
            _blocks[i].writePayload(buffer);
        }
    }

    public String toString()
    {
        if (_blocks == null)
        {
            return "No blocks contained in composite frame";
        }
        else
        {
            StringBuilder buf = new StringBuilder();
            for (int i = 0 ; i< _blocks.length; i++)
            {
                buf.append("Block ").append(i).append('\n').append(_blocks[i].toString());
            }
            return buf.toString();
        }
    }
}