all: clean compile link run

# Linking step
link:
	g++ -o bin/StompEMIClient bin/ConnectionHandler.o bin/event.o bin/StompClient.o bin/StompProtocol.o bin/Frame.o -lpthread

# Compilation step
compile:
	g++ -g -Wall -Weffc++ -std=c++11 -Iinclude -c -o bin/ConnectionHandler.o src/ConnectionHandler.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -Iinclude -c -o bin/event.o src/event.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -Iinclude -c -o bin/StompClient.o src/StompClient.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -Iinclude -c -o bin/StompProtocol.o src/StompProtocol.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -Iinclude -c -o bin/Frame.o src/Frame.cpp

# Cleaning step
clean:
	rm -f bin/*

# Run the compiled program
run:
	./bin/StompEMIClient

# Run the program with Valgrind
c: all
	valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes ./bin/simulation config_file.txt