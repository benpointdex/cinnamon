FROM eclipse-temurin:21-jre
WORKDIR /app

# Download the lightweight 22MB Quantized MiniLM ONNX model & tokenizer
RUN mkdir -p /app/onnx && \
    apt-get update && apt-get install -y --no-install-recommends curl ca-certificates && \
    curl -L -o /app/onnx/tokenizer.json https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/tokenizer.json && \
    curl -L -o /app/onnx/model.onnx https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/onnx/model_quantized.onnx && \
    apt-get remove -y curl && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*


COPY target/cinnamon-*.jar app.jar

# Point Spring AI to the pre-bundled local files
ENV SPRING_AI_EMBEDDING_TRANSFORMER_ONNX_MODEL_URI=file:/app/onnx/model.onnx
ENV SPRING_AI_EMBEDDING_TRANSFORMER_TOKENIZER_URI=file:/app/onnx/tokenizer.json
ENV OMP_NUM_THREADS=1
ENV MKL_NUM_THREADS=1
ENV ORT_GLOBAL_THREAD_POOL_SIZE=1

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx180m", "-Xms64m", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=32m", "-XX:+UseSerialGC", "-Xss256k", "-XX:MaxDirectMemorySize=48m", "-jar", "app.jar"]
