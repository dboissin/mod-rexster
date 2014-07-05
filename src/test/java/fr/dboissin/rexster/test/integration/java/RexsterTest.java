package fr.dboissin.rexster.test.integration.java;

import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.server.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

public class RexsterTest extends TestVerticle {

	private RexsterServer httpRexsterServer;
	private EventBus eb;

	@Override
	public void start() {
		Reader rexsterXmlReader = new InputStreamReader(
				RexsterTest.class.getClassLoader().getResourceAsStream("rexster.xml"));
		final XMLConfiguration properties = new XMLConfiguration();
		try {
			properties.load(rexsterXmlReader);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
		final List<HierarchicalConfiguration> graphConfigs = properties.configurationsAt(Tokens.REXSTER_GRAPH_PATH);

		final RexsterApplication ra = new XmlRexsterApplication(graphConfigs);
		httpRexsterServer = new HttpRexsterServer(properties);
		try {
			httpRexsterServer.start(ra);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		container.deployModule(System.getProperty("vertx.modulename"), new Handler<AsyncResult<String>>() {
			@Override
			public void handle(AsyncResult<String> ar) {
				if (ar.succeeded()) {
					eb = vertx.eventBus();
					RexsterTest.super.start();
				} else {
					ar.cause().printStackTrace();
				}
			}
		});
	}

	@Override
	public void stop() {
		super.stop();
		if (httpRexsterServer != null) {
			try {
				httpRexsterServer.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testGraphs() {
		eb.send("rexster", new JsonObject().putString("action", "get-graphs"), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				assertEquals("ok", message.body().getString("status"));
				testComplete();
			}
		});
	}

	@Test
	public void testGraph() {
		eb.send("rexster", new JsonObject().putString("action", "get-graph").putString("graph", "tinkergraph"),
				new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				assertEquals("ok", message.body().getString("status"));
				testComplete();
			}
		});
	}

	@Test
	public void testUnknowGraph() {
		eb.send("rexster", new JsonObject().putString("action", "get-graph").putString("graph", "bla"),
				new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> message) {
						assertEquals("error", message.body().getString("status"));
						testComplete();
					}
				});
	}

	@Test
	public void testNullGraph() {
		eb.send("rexster", new JsonObject().putString("action", "get-graph"),
				new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> message) {
						assertEquals("error", message.body().getString("status"));
						testComplete();
					}
				});
	}

}
