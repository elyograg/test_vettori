package org.elyograg.test.vettori;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Command(name = "test_vettori", sortOptions = false, scope = ScopeType.INHERIT, description = ""
    + "test_vettori: A quick program to make an http2 request to a Solr server.", footer = StaticStuff.USAGE_OPTION_SEPARATOR_TEXT)
public final class Main implements Runnable {
  /**
   * A logger object. Gets the fully qualified class name so this can be used
   * as-is for any class.
   */
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** Verbose option. */
  @Option(names = { "-v" }, arity = "0", scope = ScopeType.INHERIT, description = ""
      + "Log any available debug messages.")
  private static boolean verbose;

  /**
   * An argument group in which one of the options is required.
   */
  @ArgGroup(multiplicity = "1")
  private static RequiredOpts requiredOpts;

  private static final class RequiredOpts {
    @Option(names = { "-u", "--url" }, arity = "1", scope = ScopeType.INHERIT, description = ""
        + "A Solr base URL. Example:\n\"http://192.168.1.200:8983/solr\"")
    private static String url;

    /** A hidden --exit option used by the shell script. */
    @Option(names = {
        "--exit" }, arity = "0", hidden = true, scope = ScopeType.INHERIT, description = ""
            + "Exit the program as soon as it starts.")
    private static boolean exitFlag;

    /** Help option. */
    @Option(names = {
        "-h" }, arity = "0", usageHelp = true, scope = ScopeType.INHERIT, description = ""
            + "Display this command usage.")
    private static boolean help;
  }

  @Option(names = { "-c",
      "--collection" }, arity = "1", defaultValue = "collection1", scope = ScopeType.INHERIT, description = ""
          + "The collection to query.  Default: '${DEFAULT-VALUE}'")
  private static String collection;

  @Option(names = { "-q",
      "--query" }, arity = "1", defaultValue = "*:*", scope = ScopeType.INHERIT, description = ""
          + "The query string to use.  Default: '${DEFAULT-VALUE}'")
  private static String query;

  public static final void main(final String[] args) {
    final CommandLine cmd = new CommandLine(new Main());
    cmd.setHelpFactory(StaticStuff.createLeftAlignedUsageHelp());
    final int exitCode = cmd.execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    log.info("Program starting");

    if (RequiredOpts.exitFlag) {
      System.exit(0);
    }

    final Http2SolrClient.Builder cb = new Http2SolrClient.Builder(RequiredOpts.url)
        .withConnectionTimeout(5, TimeUnit.SECONDS).withIdleTimeout(60, TimeUnit.SECONDS)
        .withRequestTimeout(60, TimeUnit.SECONDS);
    log.info("Testing\nURL {}\nCollection {}\nQuery: {}", RequiredOpts.url, collection, query);
    try (final SolrClient client = cb.build()) {
      final SolrQuery q = new SolrQuery(query);
      final QueryResponse r = client.query(collection, q);
      final long count = r.getResults().getNumFound();
      log.info("numFound: {}", count);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
