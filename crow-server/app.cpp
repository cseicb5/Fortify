

/*
 *
 * @file : /app.cpp
 *
 * @brief : Backend server for auth and handling job management
 *
 * @author : Norz Oman
 *
 */

#include <crow.h>
#include <crow/app.h>
#include <crow/common.h>
#include <crow/http_request.h>
#include <crow/http_response.h>
#include <crow/json.h>
#include "sqlite/sqlite_modern_cpp.h"
#include <cstdlib>
#include <ctime>
#include <exception>
#include <string>

void addSalt(std::string& s , int len);

int main(){
    crow::SimpleApp app;

    sqlite::database db("jobHandler.db");

    db << "create table if not exists users ( _id integer primary key autoincrement not null , username text unique , token text);";
    db << "create table if not exists job (_id integer primary key autoincrement not null, token text , type text , input_path text ,status text);";

    CROW_ROUTE(app,"/")([](){
        return crow::response(200,"Hello World");
    });

    CROW_ROUTE(app,"/getToken").methods(crow::HTTPMethod::Post)([&db](const crow::request& req){
        try{
            crow::json::rvalue json_body = crow::json::load(req.body);
            std::string username = json_body["username"].s();
            std::string user = username;
            addSalt(username ,9);
            crow::json::wvalue token;
            token["token"] = username;
            db << "insert into users (username,token) values(?,?);" << user << username;
            return crow::response(token);
        }
        catch(const std::exception& e){
            std::cerr << e.what() << std::endl;
            return crow::response(500,"Internal Server Error. Could arise if the username already exists in the system");
        }
    });

    CROW_ROUTE(app, "/scanApk").methods(crow::HTTPMethod::Post)([&db](const crow::request& req){
        std::string auth =  req.get_header_value("Authorization");
        if(auth.empty()) return crow::response(401,"No Authorization token was provided");
        std::string token = auth.substr(7);
        int count = 0;
        db << "select count(*) from users where token = ?;" << token >> count;
        if(count == 0) return crow::response(401,"Invalid token or in invalid format was supplied");
        return crow::response(200 , "Ready , send your files now");
    });

    app.port(5000).run();
}

void addSalt(std::string& s , int len){
    std::string A = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    std::srand(std::time(nullptr));
    std::string output;
    while(len > 0){
        s += A[rand() % A.size()];
        --len;
    }
}
