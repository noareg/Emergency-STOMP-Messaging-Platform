#ifndef STOMPPROTOCOL_H
#define STOMPPROTOCOL_H

#include <string>
#include <map>              
#include <unordered_map>    
#include <vector>          
#include <mutex>
#include "ConnectionHandler.h"
#include "event.h"
#include <atomic>


class Frame;

struct summaryReport { //structure ment to keep track of events reported by User X for channel Y
    int totalReports = 0;                      // Total number of events reported
    int activeCount = 0;                       // Count of active events
    int forcesArrivalCount = 0;                // Count of events with forces arrival
    std::vector<Event> events;                 // List of all events for the user

    // Constructor to initialize members
    summaryReport()
        : totalReports(0), activeCount(0), forcesArrivalCount(0), events() {}

};

class StompProtocol {
private:
    std::atomic<bool> connectionActive;                // Indicates if the protocol should stop
    ConnectionHandler& connectionHandler; // Reference to the ConnectionHandler

    std::map<std::string, std::map<std::string, summaryReport>> reports; //outer -channel name, inner user name
    std::mutex reportsMutex; // locks reports map
    std::mutex receiptMutex;
    std::unordered_map<std::string, int> mapChannelID; // Map channel names to IDs
    std::unordered_map<int, std::string> mapReceiptID; // Map   

    int subscriptionId; // Keep track of subscription IDs
    int receiptUnsubscribe; // odd
    int receiptsubscribe; //even

    std::string login; //user name


public:
    StompProtocol(ConnectionHandler& connectionHandler); // Constructor

    void sendLoginFrame(const std::string& hostPort, const std::string& login, const std::string& passcode);

    void sendLogoutFrame();

    // Process user commands like `login`, `join`, `report`, etc.
    void processCommand(const std::string& command);

    // Process server responses like `MESSAGE`, `RECEIPT`, `ERROR`
    void processServerResponse(const std::string& response);


    //getters
     std::map<std::string, std::map<std::string, summaryReport>>& getReports() ; ;
     std::unordered_map<std::string, int>& getMapChannelID() ;
     std::unordered_map<int, std::string>& getMapReceiptID() ;
    int getandIncrementSubscriptionId();
    int getandIncrementReceiptUnsubscribe();
    int getandIncrementReceiptSubscribe();
    std::atomic<bool>& getconnectionActive() ;
    std::mutex& getReportsMutex();
    std::mutex& getReceiptMutex();

    const std::string getLogin() const;

};

#endif // STOMPPROTOCOL_H