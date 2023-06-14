SCHEMA_OUTPUT_PATH=../system-graphql-schema/schema.graphql
schema:
	./mvnw clean spring-boot:run -pl graphql-api -Dspring-boot.run.arguments="_schemaOutputPath:${SCHEMA_OUTPUT_PATH}" -Dspring-boot.run.profiles=print-graphql-schema