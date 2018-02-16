/**
 * Created by nicolabertoldi on 16/02/18.
 */

package eu.modernmt.cli;

import eu.modernmt.cli.log4j.Log4jConfiguration;
import eu.modernmt.decoder.neural.memory.ScoreEntry;
import eu.modernmt.decoder.neural.memory.lucene.LuceneTranslationMemory;
import eu.modernmt.lang.Language;
import eu.modernmt.lang.LanguageIndex;
import eu.modernmt.lang.LanguagePair;
import eu.modernmt.model.Sentence;
import eu.modernmt.model.Word;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class QueryTranslationMemoryMain {

    private static class Args {

        private static final Options cliOptions;

        static {
            Option index = Option.builder("m").longOpt("model").hasArg().required().build();
            Option source = Option.builder("s").longOpt("source").hasArg().required().build();
            Option target = Option.builder("t").longOpt("target").hasArg().required().build();

            cliOptions = new Options();
            cliOptions.addOption(index);
            cliOptions.addOption(source);
            cliOptions.addOption(target);
        }

        public final File modelPath;
        public final Language sourceLanguage;
        public final Language targetLanguage;

        public Args(String[] args) throws ParseException {
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(cliOptions, args);

            sourceLanguage = Language.fromString(cli.getOptionValue('s'));
            targetLanguage = Language.fromString(cli.getOptionValue('t'));
            modelPath = new File(cli.getOptionValue('m'));
        }

    }


    public static void main(String[] _args) throws Throwable {
        Log4jConfiguration.setup(Level.INFO);

        Args args = new Args(_args);
        LanguagePair lp = new LanguagePair(args.sourceLanguage, args.targetLanguage);
        LanguageIndex language = new LanguageIndex(lp);
        LuceneTranslationMemory memory = null;
        BufferedReader in = null;

        try {
            memory = new LuceneTranslationMemory(language, args.modelPath, 10);

            in = new BufferedReader(new InputStreamReader(System.in));
            String inputLine;
            List<Word> words = new ArrayList<>();

            //loop forever an stream interruption is caught
            // in case of empty line it returns an empty line
            while ((inputLine = in.readLine()) != null) {
                String[] strings = inputLine.trim().split("\t");
                String srcText = strings[0];
                String trgText = strings[1];
                for (String s : srcText.split(" ")) {
                    words.add(new Word(s));
                }


        /*build the Sentence based on the words and tags lists */
                Sentence source = new Sentence(words.toArray(new Word[words.size()]));
                ScoreEntry[] suggestions = memory.search(lp, source, 10);
                System.err.println("SOURCE:" + srcText);
                System.err.println("TARGET:" + trgText);
                int i = 0;
                for (ScoreEntry suggestion : suggestions) {
                    System.err.println("SUGG[" + i++ + "]:" + suggestion.toString());
                }
            }
        } finally {
            IOUtils.closeQuietly(memory);
            IOUtils.closeQuietly(in);
        }
    }
}
