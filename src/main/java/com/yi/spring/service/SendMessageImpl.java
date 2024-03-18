package com.yi.spring.service;

import com.yi.spring.controller.StompRabbitController;
import com.yi.spring.entity.ChatDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SendMessageImpl implements SendMessage {
    private final RabbitTemplate template;
    public SendMessageImpl(RabbitTemplate template) {
        this.template = template;
    }

    @Override
    public void Send(HttpServletRequest request, String userId, ChatDTO chatDto ) throws AmqpException {
        ServletContext servletContext = request.getServletContext();
        String chatRoomId = (String) servletContext.getAttribute( "user" + userId );
        template.convertAndSend( StompRabbitController.CHAT_EXCHANGE_NAME, "room." + chatRoomId, chatDto );
    }
}
