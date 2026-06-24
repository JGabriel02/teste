
# Etapa 1: compila o projeto usando Maven e Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia primeiro o pom para aproveitar o cache das dependências
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Executa os testes e gera o arquivo .jar
RUN mvn clean package -B


# Etapa 2: imagem menor, usada para executar a aplicação
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

