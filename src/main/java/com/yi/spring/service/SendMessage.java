package com.yi.spring.service;

import com.yi.spring.entity.ChatDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.AmqpException;

public interface SendMessage {
    void Send(HttpServletRequest request, String userId, ChatDTO chatDto ) throws AmqpException;
}
