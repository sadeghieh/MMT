package eu.modernmt.cli;

import eu.modernmt.cli.log4j.Log4jConfiguration;
import eu.modernmt.facade.ModernMT;
import eu.modernmt.lang.Language;
import eu.modernmt.lang.LanguageIndex;
import eu.modernmt.lang.LanguagePair;
import eu.modernmt.model.corpus.Corpora;
import eu.modernmt.model.corpus.MultilingualCorpus;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davide on 17/12/15.
 */
public class ReducingCorporaMain {

    private static class Args {

        private static final Options cliOptions;

        static {
            Option sourceLanguage = Option.builder("s").hasArg().required().build();
            Option targetLanguage = Option.builder("t").hasArg().required().build();
            Option inputPath = Option.builder().longOpt("input").hasArgs().required().build();
            Option outputPath = Option.builder().longOpt("output").hasArg().required().build();
            Option maxWordCount = Option.builder().longOpt("words").hasArg().required().build();

            cliOptions = new Options();
            cliOptions.addOption(sourceLanguage);
            cliOptions.addOption(targetLanguage);
            cliOptions.addOption(inputPath);
            cliOptions.addOption(outputPath);
            cliOptions.addOption(maxWordCount);
        }

        public final Language sourceLanguage;
        public final Language targetLanguage;
        public final int maxWordCount;
        public final File[] inputRoots;
        public final File outputRoot;

        public Args(String[] args) throws ParseException {
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(cliOptions, args);

            sourceLanguage = Language.fromString(cli.getOptionValue('s'));
            targetLanguage = Language.fromString(cli.getOptionValue('t'));

            String[] roots = cli.getOptionValues("input");
            inputRoots = new File[roots.length];
            for (int i = 0; i < roots.length; i++)
                inputRoots[i] = new File(roots[i]);

            outputRoot = new File(cli.getOptionValue("output"));
            maxWordCount = Integer.parseInt(cli.getOptionValue("words"));
        }

    }

    public static void main(String[] _args) throws Throwable {
        Log4jConfiguration.setup(Level.INFO);

        Args args = new Args(_args);

        List<MultilingualCorpus> corpora = new ArrayList<>();
        Corpora.list(null, true, corpora, args.sourceLanguage, args.targetLanguage, args.inputRoots);

        if (corpora.isEmpty())
            throw new ParseException("Input path does not contains valid bilingual data");

        LanguagePair language = new LanguagePair(args.sourceLanguage, args.targetLanguage);
        ModernMT.training.reduce(language, corpora, args.outputRoot, args.maxWordCount);
    }

}
