package extractor;

/**
 * Created by kaspar on 29.04.18.
 */
@FunctionalInterface
public interface MessageExtractor {

    String extract(String message);
}
