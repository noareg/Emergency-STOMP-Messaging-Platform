#include <iostream>
#include <thread>
#include <atomic>
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include "../include/Frame.h"

void serverResponseHandler(ConnectionHandler& connectionHandler, StompProtocol& protocol, std::atomic<bool>&connectionActive) {
    while (connectionActive) { 
        std::string response;
        if (!connectionHandler.getLine(response)) { //check if the connection is still open
            std::cout << "Disconnected from server"<< std::endl;
            connectionActive = false;
            break;
        }
        protocol.processServerResponse(response);  //process the response
        if (!protocol.getconnectionActive()) {
            std::cout << "Disconnected from server"<< std::endl;
            connectionActive = false;
            break;
        }
    }
}
std::atomic<bool> programShouldEnd(false); 
std::atomic<bool> connectionActive(false);


int main() {
    std::string pendingCommand;
    while (!programShouldEnd) {
        std::string command;
        if(!pendingCommand.empty()){
        command=pendingCommand;
        pendingCommand.clear();
        }else{
        std::getline(std::cin, command);
        }
        std::istringstream iss(command); // allows going word by word
        std::string action;
        iss >> action; //first word

        if (action == "login") {
            if (connectionActive) {
                std::cout << "user already logedin" << std::endl;
                continue;
            }
            std::string hostPort, login, passcode;
            if (!(iss >> hostPort >> login >> passcode)) {
                std::cout << "login command needs 3 args: {host:port} {username} {password}"<< std::endl;
                continue;
            }
            
            size_t colonPos = hostPort.find(':');
            if (colonPos == std::string::npos) {
                std::cout << "host:port are illegal"<< std::endl;
                continue;
            }
            std::string host = hostPort.substr(0, colonPos);
            int port;
            try{
            port=std::stoi(hostPort.substr(colonPos + 1));
            }catch (const std::exception&){
                std::cout << "host:port are illegal"<< std::endl;
                continue;
            }
            ConnectionHandler connectionHandler(host, port);

            if (!connectionHandler.connect()) { //connect to the server
                std::cout <<"Cannot connect to " << host << ":" << port << " please try to login again"<< std::endl;
                continue;
            }
            connectionActive=true;

            StompProtocol protocol(connectionHandler);
            std::thread serverThread (serverResponseHandler, std::ref(connectionHandler), std::ref(protocol), std::ref(connectionActive));

            protocol.sendLoginFrame(hostPort, login, passcode);

            while (connectionActive) { 
                std::string subCommand;
                std::getline(std::cin, subCommand);
                std::istringstream iss(subCommand); // allows going word by word
                std::string action;
                iss >> action; //first word
                if (action == "logout") {
                    protocol.sendLogoutFrame(); 
                } else if(action != "login") {
                    protocol.processCommand(subCommand);
                    } else if(!connectionActive){
                        pendingCommand=subCommand;
                        }else{
                           std::cout << "user already logedin" << std::endl;
                           continue; 
                        }

            }
            if (serverThread.joinable()){
                serverThread.join();
            }

        } else {
            std::cout << "please login first" << std::endl;
            continue;
        }
    }


    return 0;
}