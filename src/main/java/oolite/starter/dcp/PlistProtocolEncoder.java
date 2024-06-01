/*
 */
package oolite.starter.dcp;

import com.dd.plist.NSObject;
import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 *
 * @author hiran
 */
public class PlistProtocolEncoder implements ProtocolEncoder {
    private static final Logger log = LogManager.getLogger();
    
    /** 
     * Creates a new ProtocolEncoder.
     */
    public PlistProtocolEncoder() {
        log.debug("PlistProtocolEncoder()");
    }

    /**
     * Serializes the message (some NSObject) into GNUstep Plist format.
     * 
     * @param session
     * @param message
     * @param out
     * @throws Exception something went wrong
     */
    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        log.debug("encode({}, {}, {})", session, message, out);
        
        NSObject plist = (NSObject)message;
        String s = plist.toXMLPropertyList();
        byte[] octets = s.getBytes(Charset.forName("UTF-8"));
        
        IoBuffer buffer = IoBuffer.allocate(octets.length + 4);
        buffer.putInt(octets.length);
        buffer.put(octets);
        buffer.flip();
        out.write(buffer);
    }

    /**
     * Cleans up the ProtocolEncoder.
     * 
     * @param is
     * @throws Exception something went wrong
     */
    @Override
    public void dispose(IoSession is) throws Exception {
        log.debug("dispose(...)");
    }
    
}
