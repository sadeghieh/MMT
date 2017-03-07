package eu.modernmt.processing.chars;

import eu.modernmt.processing.LanguageNotSupportedException;
import eu.modernmt.processing.ProcessingException;
import eu.modernmt.processing.TextProcessor;
import eu.modernmt.processing.string.SentenceBuilder;

import java.util.Locale;
import java.util.Map;

/**
 * Created by davide on 19/02/16.
 */
public class RareCharsNormalizer extends TextProcessor<SentenceBuilder, SentenceBuilder> {

    public RareCharsNormalizer(Locale sourceLanguage, Locale targetLanguage) throws LanguageNotSupportedException {
        super(sourceLanguage, targetLanguage);
    }

    @Override
    public SentenceBuilder call(SentenceBuilder string, Map<String, Object> metadata) throws ProcessingException {
        char source[] = string.toCharArray();
        SentenceBuilder.Editor editor = string.edit();

        for (int i = 0; i < source.length; i++) {
            char c = source[i];

            char nc = normalized(c);
            if (nc != '\0')
                editor.replace(i, 1, Character.toString(nc));
        }

        return editor.commit();
    }

    private static char normalized(char c) {
        char nc = '\0';

        switch (c) {
            case '`':
            case '‘':
            case '’':
                nc = '\'';
                break;
            case '«':
            case '»':
            case '“':
            case '”':
            case '„':
                nc = '"';
                break;
            case '–':
            case '—':
                nc = '-';
                break;
            default:
                break;
        }

        return nc;
    }

}
