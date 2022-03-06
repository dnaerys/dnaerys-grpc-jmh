package org.dnaerys.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.dnaerys.cluster.grpc.*;

public class SelectVariants {

    @State(Scope.Thread)
    public static class GrpcChannel {

        DnaerysServiceGrpc.DnaerysServiceBlockingStub blockingStub;
        ManagedChannel channel;

//        @Setup(Level.Iteration)
        @Setup(Level.Invocation) // memory leak in io.grpc.netty forces this mode
        public void startUp() {
//            String target = "192.168.8.141:8001";
            String target = "172.19.0.2:8000";
//            System.out.println("Starting up a client and opening a channel on " + target);
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
//            System.out.println("Shutting down client...");
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
    public static Variant snpInRegion(GrpcChannel grpc) {
        final Chromosome chr = Chromosome.CHR_2;
        final int start = 179391075;
        final int end = 179391075;
        final boolean selectHom = true;
        final boolean selectHet = true;
        RefAssembly assembly = RefAssembly.GRCh37;
        Iterator<AllelesResponse> response;
        AllelesInRegionRequest request =
            AllelesInRegionRequest
                .newBuilder()
                .setChr(chr)
                .setStart(start)
                .setEnd(end)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAssembly(assembly)
                .build();

        response = grpc.blockingStub.selectVariantsInRegion(request);
        AllelesResponse allelesResponse = response.next();
        Variant variant = allelesResponse.getAlleles(0);
        return variant;
    }

    // --- Select variants in regions ----------------------------------------------------------------------------------

//    @Benchmark
//    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int homP53orBRCA2(GrpcChannel grpc) {
        final Chromosome chrP53 = Chromosome.CHR_17;
        final int startP53 = 7565097;
        final int endP53 = 7590856;
        final Chromosome chrBRCA2 = Chromosome.CHR_13;
        final int startBRCA2 = 32889611;
        final int endBRCA2 = 32973805;
        final int limit = 100; // per node
        List<Chromosome> chr = Arrays.asList(chrP53, chrBRCA2);
        List<Integer> starts = Arrays.asList(startP53, startBRCA2);
        List<Integer> ends = Arrays.asList(endP53, endBRCA2);
        final boolean selectHom = true;
        final boolean selectHet = false;
        RefAssembly assembly = RefAssembly.GRCh37;
        AllelesInMultiRegionsRequest request =
            AllelesInMultiRegionsRequest
                .newBuilder()
                .addAllChr(chr)
                .addAllStart(starts)
                .addAllEnd(ends)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAssembly(assembly)
                .setLimit(limit)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInMultiRegions(request);
        ArrayList<Variant> variants = new ArrayList<>(limit*10);
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int homP53orBRCA2Unlim(GrpcChannel grpc) {
        final Chromosome chrP53 = Chromosome.CHR_17;
        final int startP53 = 7565097;
        final int endP53 = 7590856;
        final Chromosome chrBRCA2 = Chromosome.CHR_13;
        final int startBRCA2 = 32889611;
        final int endBRCA2 = 32973805;
        final int limit = 0; // unlim
        List<Chromosome> chr = Arrays.asList(chrP53, chrBRCA2);
        List<Integer> starts = Arrays.asList(startP53, startBRCA2);
        List<Integer> ends = Arrays.asList(endP53, endBRCA2);
        final boolean selectHom = true;
        final boolean selectHet = false;
        RefAssembly assembly = RefAssembly.GRCh37;
        AllelesInMultiRegionsRequest request =
            AllelesInMultiRegionsRequest
                .newBuilder()
                .addAllChr(chr)
                .addAllStart(starts)
                .addAllEnd(ends)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAssembly(assembly)
                .setLimit(limit)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInMultiRegions(request);
        ArrayList<Variant> variants = new ArrayList<>(limit*10);
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int hetP53orBRCA2Unlim(GrpcChannel grpc) {
        final Chromosome chrP53 = Chromosome.CHR_17;
        final int startP53 = 7565097;
        final int endP53 = 7590856;
        final Chromosome chrBRCA2 = Chromosome.CHR_13;
        final int startBRCA2 = 32889611;
        final int endBRCA2 = 32973805;
        final int limit = 0; // unlim
        List<Chromosome> chr = Arrays.asList(chrP53, chrBRCA2);
        List<Integer> starts = Arrays.asList(startP53, startBRCA2);
        List<Integer> ends = Arrays.asList(endP53, endBRCA2);
        final boolean selectHom = false;
        final boolean selectHet = true;
        RefAssembly assembly = RefAssembly.GRCh37;
        AllelesInMultiRegionsRequest request =
            AllelesInMultiRegionsRequest
                .newBuilder()
                .addAllChr(chr)
                .addAllStart(starts)
                .addAllEnd(ends)
                .setHom(selectHom)
                .setHet(selectHet)
                .setAssembly(assembly)
                .setLimit(limit)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInMultiRegions(request);
        ArrayList<Variant> variants = new ArrayList<>(limit*10);
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }

    // --- Select variants in region in samples ------------------------------------------------------------------------

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int homP53InSamples(GrpcChannel grpc) {
        final Chromosome chr = Chromosome.CHR_17;
        final int start = 7565097;
        final int end = 7590856;
        final boolean selectHom = true;
        final boolean selectHet = false;
        final int limit = 0; // unlim
        List<String> samples = Arrays.asList("SRR1291070", "SRR1298980");
        RefAssembly assembly = RefAssembly.GRCh37;
        AllelesInRegionInVCRequest request =
            AllelesInRegionInVCRequest
                .newBuilder()
                .setChr(chr)
                .setStart(start)
                .setEnd(end)
                .setHom(selectHom)
                .setHet(selectHet)
                .addAllSamples(samples)
                .setAssembly(assembly)
                .setLimit(limit)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInRegionInVirtualCohort(request);
        ArrayList<Variant> variants = new ArrayList<>(limit*10);
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int hetP53InSamples(GrpcChannel grpc) {
        final Chromosome chr = Chromosome.CHR_17;
        final int start = 7565097;
        final int end = 7590856;
        final boolean selectHom = false;
        final boolean selectHet = true;
        final int limit = 0; // unlim
        List<String> samples = Arrays.asList("SRR1291070", "SRR1298980");
        RefAssembly assembly = RefAssembly.GRCh37;
        AllelesInRegionInVCRequest request =
            AllelesInRegionInVCRequest
                .newBuilder()
                .setChr(chr)
                .setStart(start)
                .setEnd(end)
                .setHom(selectHom)
                .setHet(selectHet)
                .addAllSamples(samples)
                .setAssembly(assembly)
                .setLimit(limit)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInRegionInVirtualCohort(request);
        ArrayList<Variant> variants = new ArrayList<>(limit*10);
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }

    // --- Mito 11 Panel -----------------------------------------------------------------------------------------------

//    @Benchmark
//    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int mitoLiverPanel(GrpcChannel grpc) {
        String panel = "mito11";
        AllelesInPanelRequest request =
            AllelesInPanelRequest
                .newBuilder()
                .setPanel(panel)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInPanel(request);
        ArrayList<Variant> variants = new ArrayList<>();
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }

    // --- Cancer Panel ------------------------------------------------------------------------------------------------

//    @Benchmark
//    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static int cancerPanel(GrpcChannel grpc) {
        String panel = "cancer104";
        AllelesInPanelRequest request =
            AllelesInPanelRequest
                .newBuilder()
                .setPanel(panel)
                .build();

        Iterator<AllelesResponse> response = grpc.blockingStub.selectVariantsInPanel(request);
        ArrayList<Variant> variants = new ArrayList<>();
        while (response.hasNext()) {
            variants.addAll(response.next().getAllelesList());
        }
        return variants.size();
    }

    // --- cli grpc call  ----------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        GrpcChannel channel = new GrpcChannel();
        channel.startUp();

        System.out.print("SelectVariantsInRegion: a single SNP: ");
        Variant variant = snpInRegion(channel);
        System.out.println(variant.getChr() + ":" + variant.getStart() + "-" + variant.getEnd() + "-" + variant.getRef() + "-" + variant.getAlt());

        int total = homP53orBRCA2Unlim(channel);
        System.out.println("SelectVariantsInRegions: homozygous p53 & BRCA2 variants (unlim): " + total);

        total = hetP53orBRCA2Unlim(channel);
        System.out.println("SelectVariantsInRegions: Het p53 & BRCA2 variants (unlim): " + total);

        total = homP53InSamples(channel);
        System.out.println("SelectVariantsInRegionInSamples: homozygous p53 variants in 2 samples: " + total);

        total = hetP53InSamples(channel);
        System.out.println("SelectVariantsInRegionInSamples: Het p53 variants in 2 samples: " + total);

        channel.tearDown();
    }
}