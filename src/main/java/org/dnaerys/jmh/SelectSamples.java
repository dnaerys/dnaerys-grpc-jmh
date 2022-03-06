package org.dnaerys.jmh;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.dnaerys.cluster.grpc.*;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SelectSamples {

    @State(Scope.Thread)
    public static class GrpcChannel {

        DnaerysServiceGrpc.DnaerysServiceBlockingStub blockingStub;
        ManagedChannel channel;

//        @Setup(Level.Iteration)
        @Setup(Level.Invocation) // memory leak in io.grpc.netty forces this mode
        public void startUp() {
//            String target = "192.168.8.141:8001";
//            String target = "localhost:8001";
            String target = "172.19.0.2:8000";
            // Creates a communication channel to the server, known as a Channel. Channels are thread-safe
            // and reusable. It is common to create channels at the beginning of your application and reuse
            // them until the application shuts down.
            channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For these tests TLS is disabled to avoid certificates.
                .usePlaintext()
                .build();
            blockingStub = DnaerysServiceGrpc.newBlockingStub(channel);
        }

//        @TearDown(Level.Iteration)
        @TearDown(Level.Invocation) // memory leak in io.grpc.netty forces this mode
        public void tearDown() throws Exception {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel
                .shutdownNow()
                .awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    // --- Select variants in a single region --------------------------------------------------------------------------

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int selectSamplesWithP53orBRCA2VariantsHet(GrpcChannel grpc) {
        final Chromosome chrP53 = Chromosome.CHR_17;
        final int startP53 = 7565097;
        final int endP53 = 7590856;
        final Chromosome chrBRCA2 = Chromosome.CHR_13;
        final int startBRCA2 = 32889611;
        final int endBRCA2 = 32973805;
        List<Chromosome> chr = Arrays.asList(chrP53, chrBRCA2);
        List<Integer> starts = Arrays.asList(startP53, startBRCA2);
        List<Integer> ends = Arrays.asList(endP53, endBRCA2);
        final boolean selectHom = false;
        final boolean selectHet = true;
        RefAssembly assembly = RefAssembly.GRCh37;
        SamplesInMultiRegionsRequest request =
            SamplesInMultiRegionsRequest
                .newBuilder()
                .addAllChr(chr)
                .addAllStart(starts)
                .addAllEnd(ends)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAssembly(assembly)
                .build();

        Iterator<SamplesResponse> response = grpc.blockingStub.selectSamplesInMultiRegions(request);
        HashSet<String> samples = new HashSet<>();
        while (response.hasNext()) {
            samples.addAll(response.next().getSamplesList());
        }
        return samples.size();
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int selectSamplesWithP53orBRCA2VariantsHom(GrpcChannel grpc) {
        final Chromosome chrP53 = Chromosome.CHR_17;
        final int startP53 = 7565097;
        final int endP53 = 7590856;
        final Chromosome chrBRCA2 = Chromosome.CHR_13;
        final int startBRCA2 = 32889611;
        final int endBRCA2 = 32973805;
        List<Chromosome> chr = Arrays.asList(chrP53, chrBRCA2);
        List<Integer> starts = Arrays.asList(startP53, startBRCA2);
        List<Integer> ends = Arrays.asList(endP53, endBRCA2);
        final boolean selectHom = true;
        final boolean selectHet = false;
        RefAssembly assembly = RefAssembly.GRCh37;
        SamplesInMultiRegionsRequest request =
            SamplesInMultiRegionsRequest
                .newBuilder()
                .addAllChr(chr)
                .addAllStart(starts)
                .addAllEnd(ends)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAssembly(assembly)
                .build();

        Iterator<SamplesResponse> response = grpc.blockingStub.selectSamplesInMultiRegions(request);
        HashSet<String> samples = new HashSet<>();
        while (response.hasNext()) {
            samples.addAll(response.next().getSamplesList());
        }
        return samples.size();
    }

    // --- cli grpc call  ----------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        GrpcChannel channel = new GrpcChannel();
        channel.startUp();

        int total = selectSamplesWithP53orBRCA2VariantsHet(channel);
        System.out.println("SelectSamplesInMultiRegions: with P53 or BRCA2 variants, Het: " + total);

        int total2 = selectSamplesWithP53orBRCA2VariantsHom(channel);
        System.out.println("SelectSamplesInMultiRegions: with P53 or BRCA2 variants, Hom: " + total2);

        channel.tearDown();
    }
}