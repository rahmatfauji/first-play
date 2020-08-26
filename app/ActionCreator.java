import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import helpers.JsonResponse;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class ActionCreator implements play.http.ActionCreator {

	@Override
	@SuppressWarnings("rawtypes")
	public Action createAction(Http.Request request, Method actionMethod) {
		return new Action.Simple() {
			@Override
			public CompletionStage<Result> call(Http.Context ctx) {
				Logger.info("\nREQUEST [" + request.method() + "] " + request.host() + request.uri());
				if (request.body().asJson() != null) {
					// Logger.info(request.body().asJson().toString());
				}

				String path = request.path();
				// if (MaintenanceHandler.checkMaintenanceMode() &&
				// !"/v1.0/web/maintenance".equals(path)
				// && !"/v1.0/web/admin/verify".equals(path)) {

				// return delegate.call(ctx);
				// /*
				
					return delegate.call(ctx);
				
				// */
			}
		};
	}

}
