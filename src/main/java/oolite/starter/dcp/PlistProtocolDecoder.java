/*
 */
package oolite.starter.dcp;

import com.dd.plist.PropertyListParser;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.HexDump;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/** Reads an Oolite Debug Protocol packet, parses it as XML
 * and forwards it as DOM.
 *
 * @author hiran
 */
public class PlistProtocolDecoder extends CumulativeProtocolDecoder {
    private static final Logger log = LogManager.getLogger();
    
    /**
     * Creates a new ProtocolDecoder.
     */
    public PlistProtocolDecoder() {
        super();
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        log.debug("doDecode({}, {}, {})", session, in, out);
        
        if(in.prefixedDataAvailable(4)) {
            log.debug("we have enough data");
            int length = in.getInt();
            byte[] data = new byte[length];
            in.get(data);
            
            if(log.isTraceEnabled()) {
                new HexDump().dump(data, 0, System.out, 0);
            }
            
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            Object plist = PropertyListParser.parse(bis);
            out.write(plist);
            
            return true;
        } else {
            log.debug("we wait for data");
            return false;
        }
    }
    
}
