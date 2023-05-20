package com.example.grpccource.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws IOException {
        GreetingClient main = new GreetingClient();
        main.run();

        //==============default=============
//        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        // DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        // do something
    }

    public void run() throws IOException {
//        ManagedChannel channel = getPlaintextManagedChannel();
        ManagedChannel channel = getSecureManagedChannel();

        //==============Unary request and response===============
        unaryClientRequest(channel);

//        //==============Unary request and stream response===============
//        streamServerRequest(channel);
//
//        //==============Stream request and unary response===============
//        streamClientRequest(channel);
//
//        //==============Stream request and stream response===============
//        BiDiRequest(channel);
//
//        //==============Stream request and stream response===============
//        DeadlineGreetingRequest(channel);

        channel.shutdown();
    }

    private ManagedChannel getPlaintextManagedChannel() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .build();
        return channel;
    }

    private ManagedChannel getSecureManagedChannel() throws IOException {
        ChannelCredentials creds = TlsChannelCredentials.newBuilder()
                .trustManager(new File("ssl/ca.crt"))
                .build();
            return Grpc.newChannelBuilder("localhost:50051", creds)
                .build();
    }

    private static void unaryClientRequest(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstname("behnam")
                .setLastName("dehghan")
                .build();

        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreet(greeting)
                .build();

        GreetingResponse greet = client.greet(greetRequest);

        System.out.println(greet.getResult());
    }

    private static void streamServerRequest(ManagedChannel channel) {

        GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstname("behnam")
                .setLastName("dehghan")
                .build();

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreet(greeting)
                .build();

        client.greetManyTimes(request).forEachRemaining(
                greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                }
        );
    }

    private void streamClientRequest(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub client = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = client.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse longGreetResponse) {
                // we get a response from the server
                System.out.println("Receive a response from server");
                System.out.println(longGreetResponse.getResult());
                // onNext will be called only once
            }

            @Override
            public void onError(Throwable throwable) {
                // we get an error from the server
            }

            @Override
            public void onCompleted() {
                // the ser ver is done sending us data
                // onCompleted will be called right after onNext()
                System.out.println("server has completed sending us something");
                latch.countDown();
            }
        });

        // streaming message #1
        System.out.println("sending streaming message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreet(Greeting.newBuilder()
                        .setFirstname("behnam")
                        .build())
                .build());
        // streaming message #2
        System.out.println("sending streaming message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreet(Greeting.newBuilder()
                        .setFirstname("hasan")
                        .build())
                .build());
        // streaming message #3
        System.out.println("sending streaming message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreet(Greeting.newBuilder()
                        .setFirstname("salar")
                        .build())
                .build());

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void BiDiRequest(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub client = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<GreetEveryoneRequest> requestObserver = client.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneResponse greetEveryoneResponse) {
                System.out.println("Response from server: " + greetEveryoneResponse.getResult());
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("server is done sending data");
            }
        });

        Arrays.asList("behnam", "hasan", "hamed", "salar").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreet(
                                    Greeting.newBuilder()
                                    .setFirstname(name)
                                    .build()
                            ).build());
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void DeadlineGreetingRequest(ManagedChannel channel) {
        try {
            GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);

            Greeting behnam = Greeting.newBuilder().setFirstname("behnam").build();
            GreetWithDeadLineRequest request = GreetWithDeadLineRequest.newBuilder().setGreet(behnam).build();

            client.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadLine(request);
        } catch (StatusRuntimeException e) {
            if (Objects.equals(e.getStatus(), Status.DEADLINE_EXCEEDED)) {
                System.out.println("Deadline has been exceeded , we dont want the response");
            } else {
                e.printStackTrace();
            }
        }

        try {
            GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);

            Greeting behnam = Greeting.newBuilder().setFirstname("behnam").build();
            GreetWithDeadLineRequest request = GreetWithDeadLineRequest.newBuilder().setGreet(behnam).build();

            client.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadLine(request);
        } catch (StatusRuntimeException e) {
            if (Objects.equals(e.getStatus(), Status.DEADLINE_EXCEEDED)) {
                System.out.println("Deadline has been exceeded , we dont want the response");
            } else {
                e.printStackTrace();
            }
        }
    }
}
