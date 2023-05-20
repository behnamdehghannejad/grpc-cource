package com.example.grpccource.calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class CalculatorService {

    public static void main(String[] args) throws IOException, InterruptedException {
//        Server server = service();

        Server server = serverByReflection();
        server.start();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println("Receive Shutdown Request");
                    server.shutdown();
                    System.out.println("Successfully stop the server");
                } )
        );

        server.awaitTermination();
    }

    private static Server server() {
        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .build();
        return server;
    }

    private static Server serverByReflection() {
        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build();
        return server;
    }
}
