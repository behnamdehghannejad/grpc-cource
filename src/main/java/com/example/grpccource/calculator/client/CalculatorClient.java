package com.example.grpccource.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();


//        unarySumResponse(channel);
//        streamServerPrimeNumberDecompositionResponse(channel);
//        streamClientComputeAverageResponse(channel);
//        BiDIFindMaximumResponse(channel);
        ErrorHandlingSquareRootResponse(channel);
    }

    private static void unarySumResponse(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub client = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();

        CalculatorResponse response = client.sum(request);

        System.out.println(request.getFirstNumber() + " + " + request.getSecondNumber() + " = " + response.getResult());
    }

    private static void streamServerPrimeNumberDecompositionResponse(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub client = CalculatorServiceGrpc.newBlockingStub(channel);

        PrimeNumberDecompositionRequest request = PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(848484848L)
                .build();

        client.primeNumberDecomposition(request).forEachRemaining(
                primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeNumber());
                }
        );

    }

    private static void streamClientComputeAverageResponse(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub client = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> requestObserver = client.computeAverage(new StreamObserver<>() {
            @Override
            public void onNext(ComputeAverageResponse computeAverageResponse) {
                System.out.println("Receive a response from thr server");
                System.out.println(computeAverageResponse.getResult());
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us data");
            }
        });

        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(1)
                .build());
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(2)
                .build());
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(3)
                .build());
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(4)
                .build());

        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void BiDIFindMaximumResponse(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub client = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<FindMaximumRequest> requestObserver = client.findMaximum(new StreamObserver<>() {
            @Override
            public void onNext(FindMaximumResponse findMaximumResponse) {
                System.out.println("maximum is= " + findMaximumResponse.getResult());
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us data");
            }
        });

        Arrays.asList(3, 5, 13, 9, 17, 16, 30, 22, 5).forEach(
                number -> {
                    System.out.println("Sending number is: " + number);
                    requestObserver.onNext(
                            FindMaximumRequest.newBuilder()
                                    .setNumber(number)
                                    .build()
                    );
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void ErrorHandlingSquareRootResponse(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub client = CalculatorServiceGrpc.newBlockingStub(channel);

        int number = -1;
        SquareRootRequest request = SquareRootRequest.newBuilder()
                .setNumber(number)
                .build();

        try {
            client.squareRoot(request);
        } catch (StatusRuntimeException e) {
            System.out.println("Got an exception for square root");
        }
    }
}
