/*
 */
package oolite.starter.util;

import com.chaudhuri.plist.PlistLexer;
import com.chaudhuri.plist.PlistParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class PlistUtil {
    private static final Logger log = LogManager.getLogger();
    
    private static class ThrowingErrorListener extends BaseErrorListener {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            log.error("syntaxError({}, {}, {}, {}, {}, {})", recognizer, offendingSymbol, line, charPositionInLine, msg, e);

            String symbol = "";
            if (offendingSymbol instanceof CommonToken token) {
                //ModelParser.VOCABULARY
                symbol = recognizer.getVocabulary().getSymbolicName(token.getType());
            }

            if (offendingSymbol != null) {
                log.error("Offending symbol: {} {} {}", offendingSymbol.getClass().getName(), symbol, offendingSymbol);
            }

            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + offendingSymbol + " " + msg, e);
        }

    }

    /**
     * Ensure we have no instances of this class.
     */
    private PlistUtil() {
    }
    
    /**
     * Parses a list in Plist format.
     * 
     * @param in the input stream to read
     * @param sourceName name of the source for the input stream
     * @return the list
     * @throws IOException something went wrong
     */
    public static PlistParser.ListContext parsePListList(InputStream in, String sourceName) throws IOException {
        log.debug("parsePListList({}, {})", in, sourceName);
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }
        
        ThrowingErrorListener errorListener = new ThrowingErrorListener();
        
        ReadableByteChannel channel = Channels.newChannel(in);
        CharStream charStream = CharStreams.fromChannel(
                channel, 
                StandardCharsets.UTF_8, 
                4096, 
                CodingErrorAction.REPLACE, 
                sourceName,
                -1);
        PlistLexer lexer = new PlistLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PlistParser parser = new PlistParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return parser.list();
    }
    
    /**
     * Parses a dictionary in Plist format.
     * 
     * @param in the input stream to read
     * @param sourceName name of the source for the input stream
     * @return the dictionary
     * @throws IOException something went wrong
     */
    public static PlistParser.DictionaryContext parsePListDict(InputStream in, String sourceName) throws IOException {
        log.debug("parsePListDict({}, {})", in, sourceName);
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }
        
        ThrowingErrorListener errorListener = new ThrowingErrorListener();
        
        ReadableByteChannel channel = Channels.newChannel(in);
        CharStream charStream = CharStreams.fromChannel(
                channel, 
                StandardCharsets.UTF_8, 
                4096, 
                CodingErrorAction.REPLACE, 
                sourceName,
                -1);
        PlistLexer lexer = new PlistLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PlistParser parser = new PlistParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return parser.dictionary();
    }
}
