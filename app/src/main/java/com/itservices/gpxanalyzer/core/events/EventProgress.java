package com.itservices.gpxanalyzer.core.events;

/**
 * Represents an event indicating the progress of an operation, typically expressed as a percentage.
 * This event includes the source of the progress update and the current percentage value.
 *
 * @param percentageUpdateEventSourceType The source type indicating which operation's progress is being reported.
 *                                        See {@link PercentageUpdateEventSourceType}.
 * @param percentage                      The current progress percentage (0-100).
 */
public record EventProgress(PercentageUpdateEventSourceType percentageUpdateEventSourceType,
                            int percentage) {
    /**
     * Factory method to create an {@link EventProgress} instance from a source class, current value, and desired value.
     * Calculates the percentage progress based on the provided values.
     *
     * @param sourceClass  The class representing the source of the progress update.
     * @param currentValue The current value of the progress.
     * @param desiredValue The maximum or target value for the progress.
     * @return A new {@link EventProgress} instance.
     */
    public static EventProgress create(Class<?> sourceClass, int currentValue, int desiredValue) {
        return create(
                PercentageUpdateEventSourceType.create(sourceClass),
                computePercentageProgress(currentValue, desiredValue)
        );
    }

    /**
     * Factory method to create an {@link EventProgress} instance from a source type, current value, and desired value.
     * Calculates the percentage progress based on the provided values.
     *
     * @param percentageUpdateEventSourceType The source type of the progress update.
     * @param currentValue                   The current value of the progress.
     * @param desiredValue                   The maximum or target value for the progress.
     * @return A new {@link EventProgress} instance.
     */
    public static EventProgress create(PercentageUpdateEventSourceType percentageUpdateEventSourceType, int currentValue, int desiredValue) {
        return create(
                percentageUpdateEventSourceType,
                computePercentageProgress(currentValue, desiredValue)
        );
    }

    /**
     * Factory method to create an {@link EventProgress} instance directly from a source type and a pre-calculated progress percentage.
     *
     * @param percentageUpdateEventSourceType The source type of the progress update.
     * @param progress                       The progress percentage (0-100).
     * @return A new {@link EventProgress} instance.
     */
    public static EventProgress create(PercentageUpdateEventSourceType percentageUpdateEventSourceType, int progress) {
        return new EventProgress(
                percentageUpdateEventSourceType,
                progress
        );
    }

    /**
     * Computes the percentage progress given a current value and a maximum value.
     *
     * @param value    The current value.
     * @param maxValue The maximum value.
     * @return The calculated percentage progress (0-100).
     */
    private static int computePercentageProgress(float value, float maxValue) {
        return (int) (100.0f * ((value / maxValue)));
    }
}
