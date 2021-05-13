package com.movestar.graphql;

import com.google.gson.GsonBuilder;
import graphql.GraphQL;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeRuntimeWiring;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        var sdl = new String(Main.class.getResourceAsStream("/schema.graphqls").readAllBytes());
        var typeRegistry = new SchemaParser().parse(sdl);
        var wiring = RuntimeWiring.newRuntimeWiring()
            .type(TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("bookById", GraphQLDataFetchers.getBookByIdDataFetcher()))
            .type(TypeRuntimeWiring.newTypeWiring("Book")
                .dataFetcher("author", GraphQLDataFetchers.getAuthorDataFetcher()))
            .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        var graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
        var graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        var result = graphQL.execute("{ bookById(id: \"book-1\") { id name } }");

        result.getErrors().forEach(err -> {
            System.out.println(err.getMessage());
        });

        var data = result.<Map<String, String>>getData();
        var json = new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(data);

        System.out.println(json);

    }
}
