package ca.retrylife.marketcap.util;

import org.bukkit.Server;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;

public class SentryUtil {

    public static void configureSentry(String endpoint, Server server) {
        // Connect to sentry for debugging information
        Sentry.init(options -> {
            options.setDsn(endpoint);
            options.setTracesSampleRate(0.5);
        });

        // Configure the scope
        Sentry.configureScope((scope) -> {

            // Set up the user
            User user = new User();
            scope.setUser(user);
            scope.setTag("server_implementation", server.getName());
            scope.setTag("server_version", server.getVersion());
            scope.setContexts("max_players", server.getMaxPlayers());
            scope.setContexts("server_ip", server.getIp());
            scope.setContexts("server_port", server.getPort());
            scope.setContexts("current_players", server.getOnlinePlayers().size());
            scope.setContexts("total_players", server.getOnlinePlayers().size() + server.getOfflinePlayers().length);

        });
    }

    public static <T> void breadcrumb(Class<T> clazz, String message) {
        breadcrumb(clazz, message, SentryLevel.INFO);
    }

    public static <T> void breadcrumb(Class<T> clazz, String message, SentryLevel level) {
        Breadcrumb crumb = new Breadcrumb();
        crumb.setMessage("Stopping Redis");
        crumb.setLevel(SentryLevel.INFO);
        crumb.setCategory(clazz.getName());
        Sentry.addBreadcrumb(crumb);
    }

}