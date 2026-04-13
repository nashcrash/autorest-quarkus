package io.github.nashcrash.autorest.common.util;

import io.github.nashcrash.autorest.common.context.ContextManager;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;


public class MessagesUtils {
    /**
     * Retrieves and formats a message based on the provided code and locale from context.
     *
     * @param code   The unique identifier for the message
     * @param params The dynamic arguments for the message placeholders
     * @param defaultMessage The message returned if the code is missing
     * @return The formatted message string or a fallback if the key is missing
     */
    public static String getMessage(String code, List<String> params, String defaultMessage) {
        Locale locale = getLocaleFromHeader();

        try {
            ResourceBundle messageBundle = ResourceBundle.getBundle("messages", locale);
            String key = String.valueOf(code);
            String pattern = messageBundle.getString(key);
            String[] arguments = (params != null) ? params.toArray(new String[0]) : new String[0];
            arguments = (String[]) Arrays.stream(arguments).map(e->getMessage(e, null, e)).toArray();
            return MessageFormat.format(pattern, (Object[]) arguments);
        } catch (MissingResourceException e) {
            return defaultMessage;
        }
    }

    public static Locale getLocaleFromHeader() {
        return getLocaleFromHeader(Locale.getDefault()); //Default
    }

    public static Locale getLocaleFromHeader(Locale defaultLocale) {
        Locale locale = defaultLocale;
        String acceptLanguage = ContextManager.getParameter(HttpHeaders.ACCEPT_LANGUAGE);
        if (StringUtils.isNotBlank(acceptLanguage)) {
            try {
                List<Locale.LanguageRange> ranges = java.util.Locale.LanguageRange.parse(acceptLanguage);
                if (!ranges.isEmpty()) {
                    Locale found = java.util.Locale.lookup(ranges, List.of(Locale.getAvailableLocales()));
                    locale = (found!=null) ? found : locale;
                }
            } catch (IllegalArgumentException ignore) {}
        }
        return locale;
    }
}
