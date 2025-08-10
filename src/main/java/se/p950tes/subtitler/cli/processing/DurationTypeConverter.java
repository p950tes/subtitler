package se.p950tes.subtitler.cli.processing;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;

public class DurationTypeConverter implements ITypeConverter<Duration> {

	private static final int MILLIS_IN_SECOND = 1000;
	private static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
	private static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;

	private static final Pattern PATTERN = Pattern.compile("^(?<sign>[+-])?(?<amount>\\d+(?:\\.\\d+)?)(?<unit>ms|s|m|h)$");

    @Override
    public Duration convert(String value) {
        Matcher matcher = PATTERN.matcher(value.trim());
        if (! matcher.matches()) {
            throw new CommandLine.TypeConversionException("Invalid duration format: '" + value + "'. Expected formats like '5s', '-1500ms', '+2m'.");
        }

        String sign = matcher.group("sign");
        String unit = matcher.group("unit");

        double amount = Double.parseDouble(matcher.group("amount"));
        long amountInMillis = toMillis(amount, unit);
        
        Duration duration = Duration.ofMillis(amountInMillis);
        if ("-".equals(sign)) {
            duration = duration.negated();
        }
        return duration;
    }
    
    private long toMillis(double amount, String unit) {
        return switch (unit) {
	        case "ms" -> (long) amount;
	        case "s"  -> Math.round(amount * MILLIS_IN_SECOND);
	        case "m"  -> Math.round(amount * MILLIS_IN_MINUTE);
	        case "h"  -> Math.round(amount * MILLIS_IN_HOUR);
	        default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
	    };
    }
}