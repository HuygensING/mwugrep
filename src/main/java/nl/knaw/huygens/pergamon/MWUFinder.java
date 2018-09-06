package nl.knaw.huygens.pergamon;

import nl.knaw.huygens.algomas.concurrent.TransientLazy;
import nl.knaw.huygens.algomas.nlp.NGrams;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * An MWUFinder looks for multi-word units in text.
 */
public class MWUFinder {
  private TransientLazy<TokenizerModel> tokModel = new TransientLazy<>(() -> {
    try {
      return new TokenizerModel(MWUFinder.class.getResourceAsStream("/nl-token.bin"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  });

  private final Set<List<CharSequence>> needles;
  private final int longest;

  /**
   * Constructs a new finder for a set of patterns.
   *
   * @param needles Multi-word patterns to look for.
   */
  public MWUFinder(Stream<List<String>> needles) {
    this.needles = needles.map(needle -> new ArrayList<CharSequence>(needle)) // defensive copy
                          .map(Collections::unmodifiableList)
                          .collect(Collectors.toSet());
    longest = this.needles.stream().mapToInt(List::size).max().orElse(0);
  }

  public static class Hit {
    public final List<? extends CharSequence> pattern;
    public final Span span;

    Hit(List<? extends CharSequence> pattern, Span span) {
      this.pattern = pattern;
      this.span = span;
    }
  }

  public Stream<Hit> find(String haystack) {
    Tokenizer tok = new TokenizerME(tokModel.get());
    return NGrams.generate(1, longest, asList(tok.tokenizePos(haystack)))
                 .map(ngram -> {
                   List<? extends CharSequence> tokens = ngram.stream().map(span -> span.getCoveredText(haystack))
                                                              .collect(Collectors.toList());
                   if (!needles.contains(tokens)) {
                     return null;
                   }
                   int start = ngram.get(0).getStart();
                   int end = ngram.get(ngram.size() - 1).getEnd();
                   Span span = new Span(start, end);
                   return new Hit(tokens, span);
                 })
                 .filter(hit -> hit != null);
  }
}
