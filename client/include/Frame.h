#ifndef FRAME_H
#define FRAME_H

#include <string>
#include <unordered_map>
#include <vector>
#include <atomic>

class StompProtocol;

class Frame {
private:
    std::string command;
    std::unordered_map<std::string, std::string> headers;
    std::string body;
    StompProtocol& protocol;
 
public:
    // Constructors
    Frame(StompProtocol& protocol); // Default constructor
    Frame(const std::string& rawFrame, StompProtocol& protocol); // Parse a raw frame
    Frame(const std::string& command, const std::unordered_map<std::string, std::string>& headers, const std::string& body, StompProtocol& protocol); // Construct a frame
   
    // Getters
    std::string getCommand() const;
    std::string getHeader(const std::string& key) const;
    std::string getBody() const;

    // Convert to string (for sending frames)
    std::string toString() const;

    // Frame operations
    void handleConnect(class ConnectionHandler& connectionHandler, const std::string& hostPort, const std::string& username, const std::string& password, std::atomic<bool>& connectionActive);
    void handleSubscribe(class ConnectionHandler& connectionHandler, const std::string& channelName);
    void handleUnsubscribe(class ConnectionHandler& connectionHandler, const std::string& channelName);
    void handleReport(class ConnectionHandler& connectionHandler, std::string json_path);
    void handleDisconnect(class ConnectionHandler& connectionHandler);
    void handleSummary(const std::string& channelName, const std::string& user, const std::string& filePath);
    std::string epochToDate(const int time);

};


#endif // FRAME_H
