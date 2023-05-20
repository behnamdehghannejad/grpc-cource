package com.example.grpccource.calculator.server;

import com.proto.calculator.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {


    @Override
    public void sum(SumRequest request, StreamObserver<CalculatorResponse> responseObserver) {
        int firstNumber = request.getFirstNumber();
        int secondNumber = request.getSecondNumber();
        int result = firstNumber + secondNumber;

        CalculatorResponse build = CalculatorResponse.newBuilder().setResult(result).build();
        responseObserver.onNext(build);

        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        long number = request.getNumber();

        long divisor = 2;

        while (number > 1) {
            if (number % divisor == 0) {
                number = number /divisor;
                PrimeNumberDecompositionResponse response = PrimeNumberDecompositionResponse.newBuilder()
                        .setPrimeNumber(divisor)
                        .build();
                responseObserver.onNext(response);
            } else {
                divisor ++;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        StreamObserver<ComputeAverageRequest> requestObserver = new StreamObserver<>() {

            int sum = 0;
            int count = 0;
            @Override
            public void onNext(ComputeAverageRequest computeAverageRequest) {
                sum += computeAverageRequest.getNumber();
                count++;
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                double value = (double) sum / count;
                responseObserver.onNext(
                        ComputeAverageResponse.newBuilder()
                                .setResult(value)
                                .build()
                );

                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {

            private int maximum = 0;
            @Override
            public void onNext(FindMaximumRequest findMaximumRequest) {
                if (findMaximumRequest.getNumber() > maximum) {
                    maximum = findMaximumRequest.getNumber();
                    responseObserver.onNext(
                            FindMaximumResponse.newBuilder()
                                    .setResult(maximum)
                                    .build()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        int number = request.getNumber();
        if (number > 0) {
            double squareNumber = Math.sqrt(number);
            responseObserver.onNext(
                    SquareRootResponse.newBuilder()
                            .setRootNumber(squareNumber)
                            .build()
            );
            responseObserver.onCompleted();
        } else {
            Status.INVALID_ARGUMENT
                    .withDescription("the number being sent is not positive")
                    .augmentDescription("NUmber sent: " + number)
                    .asRuntimeException();
            responseObserver.onCompleted();
        }
    }
}
