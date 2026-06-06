FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN mvn clean install -P dev -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/XPayLabs/target/XPayLabs.jar .
COPY --from=build /build/XPayLabs-merchant/target/XPayLabs-merchant.jar .
COPY --from=build /build/XPayLabs-eth/target/XPayLabs-eth.jar .
COPY --from=build /build/XPayLabs-tron/target/XPayLabs-tron.jar .
COPY --from=build /build/XPayLabs-sui/target/XPayLabs-sui.jar .
