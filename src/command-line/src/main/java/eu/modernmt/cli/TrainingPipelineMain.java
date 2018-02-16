package eu.modernmt.cli;

import eu.modernmt.cli.log4j.Log4jConfiguration;
import eu.modernmt.facade.ModernMT;
import eu.modernmt.facade.TrainingFacade;
import eu.modernmt.lang.Language;
import eu.modernmt.lang.LanguageIndex;
import eu.modernmt.lang.LanguagePair;
import eu.modernmt.model.corpus.Corpora;
import eu.modernmt.model.corpus.Corpus;
import eu.modernmt.model.corpus.MultilingualCorpus;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by davide on 17/12/15.
 */
public class TrainingPipelineMain {

    private static class Args {

        private static final Options cliOptions;

        static {
            Option sourceLanguage = Option.builder("s").hasArg().required().build();
            Option targetLanguage = Option.builder("t").hasArg().required().build();
            Option vocabularyPath = Option.builder("v").hasArgs().required(false).build();
            Option inputPath = Option.builder().longOpt("input").hasArgs().required().build();
            Option outputPath = Option.builder().longOpt("output").hasArg().required().build();
            Option devPath = Option.builder().longOpt("dev").hasArg().required(false).build();
            Option testPath = Option.builder().longOpt("test").hasArg().required(false).build();

            cliOptions = new Options();
            cliOptions.addOption(sourceLanguage);
            cliOptions.addOption(targetLanguage);
            cliOptions.addOption(vocabularyPath);
            cliOptions.addOption(inputPath);
            cliOptions.addOption(outputPath);
            cliOptions.addOption(devPath);
            cliOptions.addOption(testPath);
        }

        public final Language sourceLanguage;
        public final Language targetLanguage;
        public final File vocabulary;
        public final File[] inputRoots;
        public final File outputRoot;
        public final File devRoot;
        public final File testRoot;

        public Args(String[] args) throws ParseException {
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(cliOptions, args);

            sourceLanguage = Language.fromString(cli.getOptionValue('s'));
            targetLanguage = Language.fromString(cli.getOptionValue('t'));
            vocabulary = cli.hasOption('v') ? new File(cli.getOptionValue('v')) : null;

            String[] roots = cli.getOptionValues("input");
            inputRoots = new File[roots.length];
            for (int i = 0; i < roots.length; i++)
                inputRoots[i] = new File(roots[i]);

            outputRoot = new File(cli.getOptionValue("output"));

            devRoot = cli.hasOption("dev") ? new File(cli.getOptionValue("dev")) : null;
            testRoot = cli.hasOption("test") ? new File(cli.getOptionValue("test")) : null;
        }

    }

    public static void main(String[] _args) throws Throwable {
        Log4jConfiguration.setup(Level.INFO);

        Args args = new Args(_args);
        LanguagePair language = new LanguagePair(args.sourceLanguage, args.targetLanguage);

        ArrayList<Corpus> monolingualCorpora = new ArrayList<>();
        ArrayList<MultilingualCorpus> bilingualCorpora = new ArrayList<>();

        Corpora.list(monolingualCorpora, true, bilingualCorpora, args.sourceLanguage, args.targetLanguage, args.inputRoots);

        if (bilingualCorpora.isEmpty())
            throw new ParseException("Input path does not contains valid bilingual data");

        TrainingFacade.TrainingOptions options = new TrainingFacade.TrainingOptions();

        if (args.devRoot != null)
            options.developmentPartition = args.devRoot;

        if (args.testRoot != null)
            options.testPartition = args.testRoot;

        if (args.vocabulary != null)
            options.vocabulary = args.vocabulary;


        ModernMT.training.preprocess(language, bilingualCorpora, monolingualCorpora, args.outputRoot, options);
    }

}
