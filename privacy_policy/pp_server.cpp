#include <chrono>
#include <ctime>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <thread>
#include <unordered_map>
#include <utility>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <ctime>
#include <arpa/inet.h>
#include <errno.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <unistd.h>

/* Common */
// ----------------------------------------------------------------------------
bool isFileAccessible(const std::string& filename) {
  struct stat buffer;
  return (stat(filename.c_str(), &buffer) == 0);
}

std::string readFileToString(const std::string& filename) {
  if (!isFileAccessible(filename)) {
    printf("File is not accessible: %s", filename.c_str());
    return "";
  }
  std::ifstream fin(filename, std::fstream::in);
  fin.seekg(0, std::ios::end);
  size_t size = fin.tellg();
  std::string buffer(size, ' ');
  fin.seekg(0);
  fin.read(&buffer[0], size);
  fin.close();
  return buffer;
}

/* Server */
// ----------------------------------------------------------------------------
struct ServerException {};

class Server {
public:
  Server(int port);
  ~Server();

  void run();

private:
  int m_socket;  // file descriptor to refer to that endpoint
  sockaddr_in m_address_structure;  // structure to handle connection addresses
};

// ----------------------------------------------
Server::Server(int port)
  : m_socket(socket(PF_INET, SOCK_STREAM, 0)) {  // create an endpoint for communication

  if (m_socket < 0) {
    printf("Failed to open socket");
    throw ServerException();
  }
  memset(&m_address_structure, 0, sizeof(sockaddr_in));
  m_address_structure.sin_family = AF_INET;
  m_address_structure.sin_addr.s_addr = htonl(INADDR_ANY);
  m_address_structure.sin_port = htons(port);

  if (bind(m_socket, reinterpret_cast<sockaddr*>(&m_address_structure), sizeof(m_address_structure)) < 0) {
    printf("Failed to bind socket to the address");
    throw ServerException();
  }
}

Server::~Server() {
  close(m_socket);
}

void Server::run() {
  // set the "LINGER" timeout to zero, to close the listen socket
  // immediately at program termination.
  linger linger_opt = { 1, 0 };  // Linger active, timeout 0
  setsockopt(m_socket, SOL_SOCKET, SO_LINGER, &linger_opt, sizeof(linger_opt));

  listen(m_socket, 20);  // listen for incoming connections, up to 20 pending connection in a queue

  while (true) {  // server loop
    sockaddr_in client_address_structure;
    socklen_t client_address_structure_size = sizeof(client_address_structure);

    // accept one pending connection, waits until a new connection comes
    int data_transfer_socket = accept(m_socket, reinterpret_cast<sockaddr*>(&client_address_structure), &client_address_structure_size);
    if (data_transfer_socket < 0) {
      printf("Failed to open new socket for data transfer");
      continue;  // skip failed connection
    }

    std::ostringstream oss, text;
    text << readFileToString("privacy_policy.html");

    oss << "HTTP/1.1 200 OK\r\n"
        << "Content-length: " << text.str().length() << "\r\n"
        << "Content-Type: text/html\r\n"
        << "Connection: Closed\r\n\r\n"
        << text.str() << "\r\n";
    send(data_transfer_socket, oss.str().c_str(), oss.str().length(), 0);
  }
}

/* Main */
// ----------------------------------------------------------------------------
int main(int argc, char** argv) {
  int port = 9001;
  if (argc >= 2) {
    port = std::atoi(argv[1]);
  }
  Server server(port);
  server.run();
  return 0;
}

