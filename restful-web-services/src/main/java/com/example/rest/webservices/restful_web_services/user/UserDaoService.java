package com.example.rest.webservices.restful_web_services.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserDaoService {

    private static List<User> users=new ArrayList<>();
    static{
        users.add(new User(1, "Radhe", LocalDate.now().minusYears(30)));
        users.add(new User(2, "Shyam", LocalDate.now().minusYears(25)));
        users.add(new User(3, "Krishna", LocalDate.now().minusYears(20)));
    }
    
    public List<User> findAllUsers() {
        return users;
    }
}
