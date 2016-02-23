package org.qcri.rheem.tests;

import org.qcri.rheem.basic.operators.*;
import org.qcri.rheem.core.function.TransformationDescriptor;
import org.qcri.rheem.core.plan.rheemplan.RheemPlan;
import org.qcri.rheem.core.types.DataSetType;
import org.qcri.rheem.core.types.DataUnitType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Provides plans that can be used for integration testing..
 */
public class RheemPlans {

    public static final URI FILE_SOME_LINES_TXT = createUri("/some-lines.txt");

    public static final URI FILE_OTHER_LINES_TXT = createUri("/other-lines.txt");

    public static URI createUri(String resourcePath) {
        try {
            return Thread.currentThread().getClass().getResource(resourcePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Illegal URI.", e);
        }

    }

    /**
     * Creates a {@link RheemPlan} consisting of a {@link TextFileSource} and a {@link LocalCallbackSink}.
     */
    public static RheemPlan readWrite(URI inputFileUri, List<String> collector) {
        TextFileSource textFileSource = new TextFileSource(inputFileUri.toString());
        LocalCallbackSink<String> sink = LocalCallbackSink.createCollectingSink(collector, String.class);
        textFileSource.connectTo(0, sink, 0);
        return new RheemPlan(sink);
    }

    /**
     * Creates a {@link RheemPlan} consisting of a {@link TextFileSource}, a {@link MapOperator} (performs
     * {@link String#toUpperCase()}), and a {@link LocalCallbackSink}.
     */
    public static RheemPlan readTransformWrite(URI inputFileUri) {
        TextFileSource textFileSource = new TextFileSource(inputFileUri.toString());
        MapOperator<String, String> reverseOperator = new MapOperator<>(
                String::toUpperCase, String.class, String.class
        );
        textFileSource.connectTo(0, reverseOperator, 0);
        LocalCallbackSink<String> stdoutSink = LocalCallbackSink.createStdoutSink(String.class);
        reverseOperator.connectTo(0, stdoutSink, 0);
        RheemPlan rheemPlan = new RheemPlan();
        rheemPlan.addSink(stdoutSink);
        return rheemPlan;
    }

    /**
     * Creates a {@link RheemPlan} with two {@link CollectionSource}s and two {@link LocalCallbackSink}s. Both sources
     * go into a {@link UnionAllOperator} and for the first {@link LocalCallbackSink}, the data quanta are routed
     * via a {@link MapOperator} that applies {@link String#toUpperCase()}.
     */
    public static RheemPlan multiSourceMultiSink(List<String> inputList1, List<String> inputList2,
                                                 List<String> collector1, List<String> collector2) {
        CollectionSource<String> source1 = new CollectionSource<>(inputList1, String.class);
        CollectionSource<String> source2 = new CollectionSource<>(inputList2, String.class);

        UnionAllOperator<String> coalesceOperator = new UnionAllOperator<>(String.class);
        source1.connectTo(0, coalesceOperator, 0);
        source2.connectTo(0, coalesceOperator, 1);

        MapOperator<String, String> uppercaseOperator = new MapOperator<>(
                String::toUpperCase, String.class, String.class
        );
        coalesceOperator.connectTo(0, uppercaseOperator, 0);

        LocalCallbackSink<String> sink1 = LocalCallbackSink.createCollectingSink(collector1, String.class);
        uppercaseOperator.connectTo(0, sink1, 0);

        LocalCallbackSink<String> sink2 = LocalCallbackSink.createCollectingSink(collector2, String.class);
        coalesceOperator.connectTo(0, sink2, 0);

        return new RheemPlan(sink1, sink2);
    }

    /**
     * Creates a {@link RheemPlan} with two {@link CollectionSource}s and two {@link LocalCallbackSink}s. Both sources
     * go into a {@link UnionAllOperator}. Then, the data flow diverges again and to the branches one {@link MapOperator}
     * is applied with {@link String#toUpperCase()} and {@link String#toLowerCase()}. Finally, the both branches
     * are united via another {@link UnionAllOperator}, which is in turn consumed by the two {@link LocalCallbackSink}s.
     */
    public static RheemPlan multiSourceHoleMultiSink(List<String> inputList1, List<String> inputList2,
                                                     List<String> collector1, List<String> collector2) {

        CollectionSource<String> source1 = new CollectionSource<>(inputList1, String.class);
        CollectionSource<String> source2 = new CollectionSource<>(inputList2, String.class);

        UnionAllOperator<String> coalesceOperator1 = new UnionAllOperator<>(String.class);
        source1.connectTo(0, coalesceOperator1, 0);
        source2.connectTo(0, coalesceOperator1, 1);

        MapOperator<String, String> lowerCaseOperator = new MapOperator<>(
                String::toLowerCase, String.class, String.class
        );
        coalesceOperator1.connectTo(0, lowerCaseOperator, 0);

        MapOperator<String, String> upperCaseOperator = new MapOperator<>(
                String::toUpperCase, String.class, String.class
        );
        coalesceOperator1.connectTo(0, upperCaseOperator, 0);

        UnionAllOperator<String> coalesceOperator2 = new UnionAllOperator<>(String.class);
        lowerCaseOperator.connectTo(0, coalesceOperator2, 0);
        upperCaseOperator.connectTo(0, coalesceOperator2, 1);


        LocalCallbackSink<String> sink1 = LocalCallbackSink.createCollectingSink(collector1, String.class);
        coalesceOperator2.connectTo(0, sink1, 0);

        LocalCallbackSink<String> sink2 = LocalCallbackSink.createCollectingSink(collector2, String.class);
        coalesceOperator2.connectTo(0, sink2, 0);

        return new RheemPlan(sink1, sink2);
    }

    /**
     * Creates a {@link RheemPlan} with a {@link TextFileSource}, a {@link SortOperator}, a {@link MapOperator},
     * a {@link DistinctOperator}, a {@link CountOperator}, and finally a {@link LocalCallbackSink} (stdout).
     */
    public static RheemPlan diverseScenario1(URI inputFileUri) {

        // Build a Rheem plan.
        TextFileSource textFileSource = new TextFileSource(inputFileUri.toString());
        SortOperator<String> sortOperator = new SortOperator<>(String.class);
        MapOperator<String, String> upperCaseOperator = new MapOperator<>(
                String::toUpperCase, String.class, String.class
        );
        DistinctOperator<String> distinctLinesOperator = new DistinctOperator<>(String.class);
        CountOperator<String> countLinesOperator = new CountOperator<>(String.class);
        LocalCallbackSink<Long> stdoutSink = LocalCallbackSink.createStdoutSink(Long.class);

        textFileSource.connectTo(0, sortOperator, 0);
        sortOperator.connectTo(0, upperCaseOperator, 0);
        upperCaseOperator.connectTo(0, distinctLinesOperator, 0);
        distinctLinesOperator.connectTo(0, countLinesOperator, 0);
        countLinesOperator.connectTo(0, stdoutSink, 0);

        return new RheemPlan(stdoutSink);
    }

    /**
     * Creates a {@link RheemPlan} with two {@link TextFileSource}s, of which the first goes through a {@link FilterOperator}
     * Then, they are unioned in a {@link UnionAllOperator}, go through a {@link SortOperator}, a {@link MapOperator}
     * (applies {@link String#toUpperCase()}), {@link DistinctOperator}, and finally a {@link LocalCallbackSink} (stdout).
     */
    public static RheemPlan diverseScenario2(URI inputFileUri1, URI inputFileUri2) throws URISyntaxException {
        // Build a Rheem plan.
        TextFileSource textFileSource1 = new TextFileSource(inputFileUri1.toString());
        TextFileSource textFileSource2 = new TextFileSource(inputFileUri2.toString());
        FilterOperator<String> noCommaOperator = new FilterOperator<>(s -> !s.contains(","), String.class);
        MapOperator<String, String> upperCaseOperator = new MapOperator<>(
                        String::toUpperCase, String.class, String.class
        );
        UnionAllOperator<String> unionOperator = new UnionAllOperator<>(String.class);
        SortOperator<String> sortOperator = new SortOperator<>(String.class);
        DistinctOperator<String> distinctLinesOperator = new DistinctOperator<>(String.class);
        LocalCallbackSink<String> stdoutSink = LocalCallbackSink.createStdoutSink(String.class);

        // Read from file 1, remove commas, union with file 2, sort, upper case, then remove duplicates and output.
        textFileSource1.connectTo(0, noCommaOperator, 0);
        textFileSource2.connectTo(0, unionOperator, 0);
        noCommaOperator.connectTo(0, unionOperator, 1);
        unionOperator.connectTo(0, sortOperator, 0);
        sortOperator.connectTo(0, upperCaseOperator, 0);
        upperCaseOperator.connectTo(0, distinctLinesOperator, 0);
        distinctLinesOperator.connectTo(0, stdoutSink, 0);

        return new RheemPlan(stdoutSink);
    }
}