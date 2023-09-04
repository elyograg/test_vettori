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

@Command(name = "changeme", sortOptions = false, scope = ScopeType.INHERIT, description = ""
    + "changeme: A sample program.", footer = StaticStuff.USAGE_OPTION_SEPARATOR_TEXT)
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
   * An argument group in which one of the options is required. The way the shell
   * script is set up, only one required is allowed. The "multiplicity" parameter
   * on the annotation is what makes one of the options in this group required. If
   * multiple required options are required, the "trial run" in the shell script
   * will need to be changed. TODO: Deal with that requirement.
   */
  @ArgGroup(multiplicity = "1")
  private static RequiredOpts requiredOpts;

  private static final class RequiredOpts {
    @Option(names = { "-u", "--url" }, arity = "1", scope = ScopeType.INHERIT, description = ""
        + "A URL.  Best to surround the value with quotes. "
        + "Example: \"http://192.168.1.200:8983/solr\"")
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
    final SolrClient client = cb.build();
    final SolrQuery q = new SolrQuery("*:*");
    final QueryResponse r;
    log.info("Testing URL {} collection {}", RequiredOpts.url, collection);
    try {
      r = client.query(collection, q);
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }

    final long count = r.getResults().getNumFound();
    log.info("numFound: {}", count);
  }
}
