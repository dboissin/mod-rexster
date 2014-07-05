package fr.dboissin.rexster;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;

import java.net.URI;

public class RestApi {

	private final HttpClient httpClient;

	public RestApi(Vertx vertx, URI rexsterUrl) {
		httpClient = vertx.createHttpClient()
				.setHost(rexsterUrl.getHost())
				.setPort(rexsterUrl.getPort())
				.setSSL("https".equals(rexsterUrl.getScheme()))
				.setMaxPoolSize(16);
				//.setKeepAlive(false);
	}

	public void getGraphs(Handler<JsonObject> handler) {
		get("/graphs", handler);
	}

	public void getGraph(String graph, Handler<JsonObject> handler) {
		get("/graphs/" + graph, handler);
	}

	private void get(String path, Handler<JsonObject> handler) {
		HttpClientRequest req = httpClient.get(path, responseHandler(handler));
		req.headers().add("Accept", "application/json");
		req.end();
	}

	private Handler<HttpClientResponse> responseHandler(final Handler<JsonObject> handler) {
		return new Handler<HttpClientResponse>() {
			@Override
			public void handle(final HttpClientResponse response) {
				response.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer buffer) {
						JsonObject r = new JsonObject(buffer.toString("UTF-8"));
						JsonObject j;
						if (response.statusCode() == 200) {
							j = new JsonObject();
							j.putString("status", "ok").putObject("result", r);
						} else {
							j = r;
							j.putString("status", "error");
						}
						handler.handle(j);
					}
				});
			}
		};
	}

}
