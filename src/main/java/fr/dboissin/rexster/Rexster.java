package fr.dboissin.rexster;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;

public class Rexster extends BusModBase implements Handler<Message<JsonObject>> {

	private RestApi restApi;

	@Override
	public void start() {
		super.start();
		try {
			restApi = new RestApi(vertx, new URI("http://localhost:8182"));
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		eb.registerHandler("rexster", this);
	}

	@Override
	public void handle(Message<JsonObject> message) {
		String action = message.body().getString("action", "");
		switch (action) {
			case "get-graphs" :
				restApi.getGraphs(replyHandler(message));
				break;
			case "get-graph" :
				restApi.getGraph(message.body().getString("graph"), replyHandler(message));
				break;
			default:
				sendError(message, "");
		}
	}

	private Handler<JsonObject> replyHandler(final Message<JsonObject> message) {
		return new Handler<JsonObject>() {
			@Override
			public void handle(JsonObject object) {
				message.reply(object);
			}
		};
	}

}
