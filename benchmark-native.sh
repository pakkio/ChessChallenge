#!/bin/bash

echo "=== Phase 4: GraalVM Native vs JVM Performance Benchmark ==="
echo

# Make sure we're using GraalVM
source /home/pakkio/.sdkman/bin/sdkman-init.sh

echo "--- JVM Version (Current Baseline) ---"
echo "Running JVM version..."
time sbt "runMain pakkio.chesschallenge.Test7x7"

echo
echo "--- Native Image Version ---" 
echo "Running native executable..."
time /home/pakkio/IdeaProjects/ChessChallenge/target/native-image/ChessChallenge

echo
echo "--- Startup Time Comparison ---"
echo "JVM startup time (including compilation):"
time -f "Real: %es, User: %Us, System: %Ss" sbt "runMain pakkio.chesschallenge.Test7x7" > /dev/null 2>&1

echo "Native startup time:"
time -f "Real: %es, User: %Us, System: %Ss" /home/pakkio/IdeaProjects/ChessChallenge/target/native-image/ChessChallenge > /dev/null 2>&1

echo
echo "=== Benchmark Complete ==="