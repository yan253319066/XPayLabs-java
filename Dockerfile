FROM eclipse-temurin:17-jre
WORKDIR /app
COPY XPayLabs/target/XPayLabs.jar .
COPY XPayLabs-merchant/target/XPayLabs-merchant.jar .
COPY XPayLabs-eth/target/XPayLabs-eth.jar .
COPY XPayLabs-tron/target/XPayLabs-tron.jar .
COPY XPayLabs-sui/target/XPayLabs-sui.jar .
