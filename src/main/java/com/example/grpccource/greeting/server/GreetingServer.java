package com.example.grpccource.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello GRPC");

        //plaintext server
//        Server server1 = plaintextServer();

        //secure server
        Server server = secureServer();

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

    private static Server plaintextServer() {
        return ServerBuilder.forPort(50051)
                .addService(new GreetingServiceImpl())
                .build();
    }

    private static Server secureServer() throws IOException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetingServiceImpl())
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")
                )
                .build();
        return server;
    }
}
