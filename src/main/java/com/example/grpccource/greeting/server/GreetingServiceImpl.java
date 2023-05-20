package com.example.grpccource.greeting.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetingResponse> responseObserver) {
        // extract the fields we need
        Greeting greet = request.getGreet();
        String firstname = greet.getFirstname();
        String lastName = greet.getLastName();

        // send the response
        GreetingResponse response = GreetingResponse.newBuilder()
                .setResult("Hello " + firstname)
                .build();

        // send the response
        responseObserver.onNext(response);

        // complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        String firstname = request.getGreet().getFirstname();

        try {
            for (int i = 0; i < 10; i++) {
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult("HELLO " + firstname + ",response number: " + i)
                        .build();

                responseObserver.onNext(response);

                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StreamObserver<LongGreetRequest> requestStreamObserver = new StreamObserver<>() {

            String result = "";
            @Override
            public void onNext(LongGreetRequest longGreetRequest) {
                //client send message
                result += ".HELLO " + longGreetRequest.getGreet().getFirstname() +"!  ";
            }

            @Override
            public void onError(Throwable throwable) {
                //client send error
            }

            @Override
            public void onCompleted() {
                //client is done
                responseObserver.onNext(
                        LongGreetResponse.newBuilder().setResult(result).build()
                );
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        StreamObserver<GreetEveryoneRequest> requestObserver = new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneRequest greetEveryoneRequest) {
                String result = ".HELLO " + greetEveryoneRequest.getGreet().getFirstname() +"!  ";
                GreetEveryoneResponse response = GreetEveryoneResponse.newBuilder()
                        .setResult(result)
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }

    @Override
    public void greetWithDeadLine(GreetWithDeadLineRequest request, StreamObserver<GreetWithDeadLineResponse> responseObserver) {
        String firstname = request.getGreet().getFirstname();
        try {
            for (int i =0; i < 3; i++) {
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GreetWithDeadLineResponse response = GreetWithDeadLineResponse.newBuilder()
                .setResult("HELLO, " + firstname)
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
