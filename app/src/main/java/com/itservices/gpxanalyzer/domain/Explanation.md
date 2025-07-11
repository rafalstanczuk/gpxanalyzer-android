# GPX Analyzer Domain Layer - Technical Explanation

## Deep Dive into Domain Architecture

### Signal Processing Foundation

The GPX Analyzer domain layer is built upon advanced signal processing principles specifically adapted for GPS track data analysis. The core challenge addressed is extracting meaningful patterns from noisy, irregularly sampled GPS data while preserving important features like peaks, valleys, and trend changes.

### Mathematical Foundations

#### 1. Wavelet-Based Analysis

The `WaveletLagDataSmoother` implements a hybrid approach combining discrete wavelet transforms with FFT analysis:

**Energy Scale Computation**:
```java
// Compute wavelet energy across multiple scales
for (int scale = 1; scale < N / 2; scale++) {
    double sumEnergy = 0;
    for (int i = 0; i < N - scale; i++) {
        double diff = dataEntities.get(i).getValue() - dataEntities.get(i + scale).getValue();
        sumEnergy += diff * diff;
    }
    waveletScales[scale] = sumEnergy / (N - scale);
}
```

**Adaptive Smoothing Factor**:
The algorithm reduces smoothing when signal variance is high:
```java
double smoothingFactor = 1.0 - Math.min(0.8, stdDev / (10.0 + stdDev));
optimalLag = (int) (optimalLag * smoothingFactor);
```

#### 2. FFT-Based Noise Analysis

The system uses Fast Fourier Transform to analyze frequency content and determine noise characteristics:

**Noise Threshold Calculation**:
```java
private static double computeNoiseThreshold(double[] fftData, double stdDev) {
    double noiseEnergy = 0.0;
    // High-frequency components (above N/4) are considered noise
    for (int i = N / 4; i < N / 2; i++) {
        noiseEnergy += fftData[i];
    }
    double noiseRatio = noiseEnergy / (totalEnergy + 1e-10);
    return Math.min(noiseRatio * stdDev, stdDev * 0.1);
}
```

#### 3. Derivative-Based Extrema Detection

The extrema detection uses discrete derivatives with time normalization:

**Time-Normalized Derivative**:
```java
for (int i = 1; i < n; i++) {
    PrimitiveDataEntity prev = smoothed.get(i - 1);
    PrimitiveDataEntity current = smoothed.get(i);
    double dt = (double)Math.abs(current.getTimestamp() - prev.getTimestamp()) / 1000.0;
    derivative[i] = (current.getValue() - prev.getValue()) / dt;
}
```

**Sign Change Detection**:
```java
double prevSign = signWithEpsilon(derivative[i - 1], EPSILON);
double currSign = signWithEpsilon(derivative[i], EPSILON);

// Local minimum: negative to positive slope
if (prevSign < 0 && currSign > 0) {
    result.add(new Extremum(i, ExtremaType.MIN));
}
// Local maximum: positive to negative slope  
else if (prevSign > 0 && currSign < 0) {
    result.add(new Extremum(i, ExtremaType.MAX));
}
```

## Data Processing Pipeline Detailed

### Stage 1: Data Preprocessing

**Accuracy Filtering**: Raw GPS data often contains measurements with poor accuracy. The system filters out unreliable data points:

```java
public static Vector<PrimitiveDataEntity> preProcessPrimitiveDataEntity(
        Vector<PrimitiveDataEntity> data,
        float maxValueAccuracy) {
    Vector<PrimitiveDataEntity> result = new Vector<>();
    for (PrimitiveDataEntity entity : data) {
        if (entity.hasAccuracy() && entity.getAccuracy() <= maxValueAccuracy) {
            result.add(entity);
        }
    }
    return result;
}
```

**Default Accuracy Threshold**: `DEFAULT_MAX_VALUE_ACCURACY = 50.0f` meters

### Stage 2: Adaptive Smoothing

The smoothing algorithm adapts to local signal characteristics:

**Window Function Selection**:
- **Triangular**: Best for preserving sharp features
- **Hanning**: Balanced approach for most GPS data  
- **Gaussian**: Superior noise reduction for high-variance signals

**Adaptive Window Sizing**:
```java
// Limit window size based on data characteristics
int maxAllowedLag = Math.max(3, dataSize / 200);
if (optimalLag > maxAllowedLag) {
    optimalLag = maxAllowedLag;
}
```

### Stage 3: Extrema Identification

**Multi-Pass Detection**:
1. **Primary Pass**: Detect obvious extrema using derivative sign changes
2. **Boundary Pass**: Handle missing start/end extrema
3. **Gap Filling**: Add intermediate segments for complete coverage

**Missing Extrema Handling**:
```java
private static void findMissingEndigExtremum(Vector<Extremum> result, int n) {
    Extremum lastOne = result.get(result.size() - 1);
    Extremum beforeLastOne = result.get(result.size() - 2);
    
    if (lastOne.index < n - 1) {
        // Add missing ending extremum based on pattern
        ExtremaType missingType = (beforeLastOne.type == ExtremaType.MIN) 
            ? ExtremaType.MAX : ExtremaType.MIN;
        result.add(new Extremum(n - 1, missingType));
    }
}
```

### Stage 4: Segment Formation

**Segment Creation Algorithm**:
Converts detected extrema into coherent trend segments with proper classification:

```java
private static Segment getMissingSegment(Segment segment, Segment prevSegment, 
                                       SegmentThresholds segmentThresholds) {
    double deltaValMissing = segment.startVal() - prevSegment.endVal();
    
    SegmentTrendType segmentTrendType = SegmentTrendType.CONSTANT;
    
    if (Math.abs(deltaValMissing) > segmentThresholds.deviationThreshold()) {
        segmentTrendType = (deltaValMissing > 0.0) 
            ? SegmentTrendType.UP : SegmentTrendType.DOWN;
    }
    
    return new Segment(prevSegment.endIndex(), segment.startIndex(),
                      prevSegment.endTime(), segment.startTime(),
                      prevSegment.endVal(), segment.startVal(),
                      segmentTrendType);
}
```

## Cumulative Statistics Deep Dive

### Two-Mode Calculation System

#### Mode 1: Segment-Based Cumulative (`FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE`)

Values accumulate within each trend segment but reset at segment boundaries:

```java
// Segment-based accumulation
if (processedDataType == FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE) {
    // Reset accumulation at segment start
    sumCumulativeAbsDeltaVal = 0.0f;
    for (DataEntity entity : segmentDataEntityVector) {
        // Accumulate within segment only
        sumCumulativeAbsDeltaVal += calculateDelta(entity, previousEntity);
        entity.setCumulativeStatistics(new CumulativeStatistics(
            sumCumulativeAbsDeltaVal, accuracy, unit));
    }
}
```

#### Mode 2: Track-Wide Cumulative (`ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE`)

Values accumulate continuously across the entire track:

```java
// Track-wide accumulation
if (processedDataType == ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE) {
    // Continue accumulation from previous segments
    float globalSum = (prevTrendBoundaryDataEntity != null) 
        ? prevTrendBoundaryDataEntity.getGlobalCumulativeSum() : 0.0f;
        
    for (DataEntity entity : segmentDataEntityVector) {
        globalSum += calculateDelta(entity, previousEntity);
        entity.setCumulativeStatistics(new CumulativeStatistics(
            globalSum, accuracy, unit));
    }
}
```

### Statistical Accuracy Tracking

**Accuracy Propagation**: The system tracks measurement accuracy through the processing pipeline:

```java
public final class CumulativeStatistics {
    private Float value = 0.0f;
    private Float valueAccuracy = 0.0f;  // Accumulated accuracy degradation
    private String unit = "";           // Measurement unit preservation
}
```

**Error Accumulation Model**:
- Linear accumulation for uncorrelated errors
- Quadratic accumulation for correlated errors  
- Unit preservation through transformations

## Advanced Algorithm Details

### Window Function Generation

The system supports multiple window functions optimized for different signal characteristics:

**Gaussian Window with Adaptive Sigma**:
```java
case GAUSSIAN:
    double sigma = 0.2 + (0.05 * (size / 25.0));  // Adaptive width
    for (int n = 0; n < size; n++) {
        double x = (n - center) / (sigma * center);
        window[n] = Math.exp(-0.5 * x * x) * noiseFactor;
    }
```

**Noise-Adaptive Scaling**:
```java
double noiseFactor = 1.0 - Math.min(0.7, noiseThreshold);
// Reduces window effectiveness in high-noise conditions
```

### Threshold Determination

**Adaptive Threshold Calculation**:
```java
public class SegmentThresholds {
    // Standard deviation-based threshold
    public SegmentThresholds(double stdDev) {
        this.deviationThreshold = stdDev * 0.2;  // 20% of standard deviation
    }
}
```

**Dynamic Adjustment**:
- Thresholds adapt to local signal characteristics
- Higher thresholds for noisy data
- Lower thresholds for clean, high-resolution data

### Memory and Performance Optimizations

**Streaming Processing**:
```java
// Process data in chunks to manage memory
Vector<PrimitiveDataEntity> processChunk(Vector<DataEntity> chunk) {
    return chunk.stream()
        .filter(entity -> entity.hasAccuracy())
        .map(DataPrimitiveMapper::mapFrom)
        .collect(Collectors.toVector());
}
```

**Parallel Processing Opportunities**:
- FFT calculations can be parallelized
- Independent segment processing
- Cumulative statistics can be calculated in parallel with appropriate synchronization

## Error Handling and Edge Cases

### Data Quality Issues

**Missing Data Points**:
```java
// Handle gaps in data
if (timeDelta > MAX_ALLOWED_GAP) {
    // Insert interpolated points or mark as data gap
    handleDataGap(previousEntity, currentEntity);
}
```

**Accuracy Degradation**:
```java
// Monitor accuracy throughout processing
if (cumulativeAccuracy > ACCURACY_THRESHOLD) {
    // Flag results as potentially unreliable
    markAsLowConfidence(result);
}
```

### Boundary Conditions

**Start/End Segment Handling**:
- Ensure complete track coverage
- Handle tracks with insufficient data
- Manage single-point tracks gracefully

**Degenerate Cases**:
- Constant value tracks (zero derivative)
- Extremely noisy data (high variance)
- Very short tracks (< 3 points)

## Integration with Visualization

### Color and Style Mapping

**Trend Type Visualization**:
```java
public enum TrendType {
    UP(ColorUtil.rgb(0.0f, 0.96f, 0.0f), 255, R.drawable.ic_trending_up_fill0),
    CONSTANT(ColorUtil.rgb(0.96f, 0.96f, 0.96f), DEFAULT_FILL_COLOR_ALPHA, R.drawable.ic_trending_flat_fill0),
    DOWN(ColorUtil.rgb(0.96f, 0.0f, 0.0f), 255, R.drawable.ic_trending_down_fill0);
}
```

**Alpha Blending Strategy**:
- Full opacity (255) for significant trends
- Reduced opacity (30% = 76) for constant/flat sections
- Visual hierarchy based on trend significance

### Chart Integration

**Data Binding for Visualization**:
```java
// Convert domain objects to chart-ready format
List<Entry> chartEntries = trendBoundaries.stream()
    .flatMap(boundary -> boundary.getDataEntities().stream())
    .map(entity -> new Entry(entity.getTimestamp(), entity.getCumulativeValue()))
    .collect(Collectors.toList());
```

**Real-time Updates**:
- Observable pattern for live data updates
- Efficient incremental chart updates
- Memory-conscious data management for long tracks

## Testing and Validation

### Algorithm Validation

**Synthetic Test Cases**:
- Known extrema patterns (sine waves, step functions)
- Controlled noise injection
- Boundary condition testing

**Real-world Validation**:
- Comparison with manual track analysis
- Cross-validation with other GPS analysis tools
- Statistical validation of results

### Performance Benchmarks

**Computational Complexity**:
- FFT: O(N log N) for N data points
- Extrema Detection: O(N) with optimizations
- Cumulative Calculation: O(N) linear pass

**Memory Usage**:
- Streaming processing for large tracks
- Configurable buffer sizes
- Garbage collection optimization

This domain layer provides a robust, mathematically sound foundation for advanced GPS track analysis while maintaining flexibility for various use cases and visualization requirements. 