package io.github.nashcrash.autorest.common.util;

import io.github.nashcrash.autorest.common.context.ContextManager;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class MessagesUtils {
    /**
     * Retrieves and formats a message based on the provided code and locale from context.
     *
     * @param code   The unique identifier for the message
     * @param params The dynamic arguments for the message placeholders
     * @param defaultMessage The message returned if the code is missing
     * @return The formatted message string or a fallback if the key is missing
     */
    public static String getMessage(Integer code, List<String> params, String defaultMessage) {
        Locale locale = Locale.ITALY; //Default
        String acceptLanguage = ContextManager.getParameter(HttpHeaders.ACCEPT_LANGUAGE);
        if (StringUtils.isNotBlank(acceptLanguage)) {
            try {
                List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage);
                if (!ranges.isEmpty()) {
                    Locale found = Locale.lookup(ranges, List.of(Locale.getAvailableLocales()));
                    locale = (found!=null) ? found : locale;
                }
            } catch (IllegalArgumentException ignore) {}
        }

        try {
            ResourceBundle messageBundle = ResourceBundle.getBundle("messages", locale);
            String key = String.valueOf(code);
            String pattern = messageBundle.getString(key);
            Object[] arguments = (params != null) ? params.toArray() : new Object[0];
            return MessageFormat.format(pattern, arguments);
        } catch (MissingResourceException e) {
            return defaultMessage;
        }
    }
}
