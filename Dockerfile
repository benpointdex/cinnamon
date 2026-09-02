FROM eclipse-temurin:21-jre
WORKDIR /app

# Download the lightweight 22MB Quantized MiniLM ONNX model & tokenizer
RUN mkdir -p /app/onnx && \
    apt-get update && apt-get install -y --no-install-recommends curl ca-certificates && \
    curl -L -o /app/onnx/tokenizer.json https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/tokenizer.json && \
    curl -L -o /app/onnx/model.onnx https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/onnx/model_quantized.onnx && \
    apt-get remove -y curl && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

# Copy the pre-built application JAR
COPY target/cinnamon-*.jar app.jar

# Point Spring AI to the pre-bundled local files
ENV SPRING_AI_EMBEDDING_TRANSFORMER_ONNX_MODEL_URI=file:/app/onnx/model.onnx
ENV SPRING_AI_EMBEDDING_TRANSFORMER_TOKENIZER_URI=file:/app/onnx/tokenizer.json

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx250m", "-Xms64m", "-XX:+UseSerialGC", "-Xss256k", "-jar", "app.jar"]
